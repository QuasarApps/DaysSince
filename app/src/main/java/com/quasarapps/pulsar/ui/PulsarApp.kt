package com.quasarapps.pulsar.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.quasarapps.pulsar.data.ThemeMode
import com.quasarapps.pulsar.ui.detail.DetailScreen
import com.quasarapps.pulsar.ui.edit.EditMilestoneScreen
import com.quasarapps.pulsar.ui.home.HomeScreen
import com.quasarapps.pulsar.ui.settings.SettingsScreen
import com.quasarapps.pulsar.ui.settings.SettingsViewModel
import com.quasarapps.pulsar.ui.splash.SplashScreen
import com.quasarapps.pulsar.ui.theme.PulsarTheme
import java.time.LocalDateTime

private object Routes {
    const val HOME = "home"
    const val ADD = "add"
    const val EDIT = "edit/{id}"
    const val DETAIL = "detail/{id}"
    const val SETTINGS = "settings"

    fun edit(id: String) = "edit/$id"
    fun detail(id: String) = "detail/$id"
}

/**
 * A milestone-detail deep link delivered by a widget tap. [token] is a per-delivery nonce from the
 * host activity: it makes two taps of the same milestone distinct values so each re-navigates,
 * while leaving recomposition (same value) a no-op.
 */
data class DeepLinkTarget(val milestoneId: String, val token: Long)

@Composable
fun PulsarApp(deepLink: DeepLinkTarget? = null) {
    // Settings drive the theme, so they're read above PulsarTheme. Until the first DataStore value
    // arrives the default (follow-system) applies, then it reconciles to the stored choice.
    val settingsVm: SettingsViewModel = viewModel()
    val settings by settingsVm.settings.collectAsState()
    val darkTheme = when (settings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    PulsarTheme(darkTheme = darkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            val vm: MilestonesViewModel = viewModel()
            val milestones by vm.milestones.collectAsState()

            // Splash overlay state, declared before the content so it can gate the content's a11y.
            // Shown on a normal cold launch and skipped when opened via a widget deep-link (so the
            // tapped milestone appears immediately). rememberSaveable keeps a rotation mid-splash from
            // replaying it (and from re-showing after an onNewIntent, when it's already dismissed).
            var splashDone by rememberSaveable { mutableStateOf(deepLink != null) }

            // Deep-link from a widget tap: jump straight to that milestone's detail.
            //
            // Keyed on [deepLink] (a new value per delivery), so it fires for a cold-start tap and
            // again for each later onNewIntent tap — even of the same milestone. It does NOT re-fire
            // on recomposition or Activity recreation: the host only delivers a deep link on a fresh
            // start or onNewIntent (never on a rotation recreate), so after a rotation [deepLink] is
            // null here and the NavController restores its own back stack untouched. launchSingleTop
            // avoids stacking a duplicate detail when the same milestone is tapped twice in a row.
            LaunchedEffect(deepLink) {
                if (deepLink != null) {
                    // Dismiss the splash so it can't linger over the deep-linked detail — covers a
                    // tap arriving via onNewIntent while a launcher cold-start's splash is still up.
                    // (On a cold-start deep link splashDone is already true, so this is a no-op.)
                    splashDone = true
                    navController.navigate(Routes.detail(deepLink.milestoneId)) {
                        launchSingleTop = true
                    }
                }
            }

            // App content (the NavHost composes/warms underneath the splash). While the splash is up,
            // clear the semantics of everything behind it so a screen reader stays within the modal
            // splash instead of traversing Home during the launch moment.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (splashDone) Modifier else Modifier.clearAndSetSemantics {}),
            ) {
                NavHost(navController = navController, startDestination = Routes.HOME) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            milestones = milestones,
                            onAdd = { navController.navigate(Routes.ADD) },
                            onOpen = { id -> navController.navigate(Routes.detail(id)) },
                            onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                        )
                    }

                    composable(Routes.ADD) {
                        EditMilestoneScreen(
                            existing = null,
                            onSave = { title, date, time, accent ->
                                vm.addMilestone(title, date, time, accent)
                                navController.popBackStack()
                            },
                            onCancel = { navController.popBackStack() },
                        )
                    }

                    composable(
                        route = Routes.EDIT,
                        arguments = listOf(navArgument("id") { type = NavType.StringType }),
                    ) { entry ->
                        val id = entry.arguments?.getString("id")
                        val existing = milestones.firstOrNull { it.id == id }
                        EditMilestoneScreen(
                            existing = existing,
                            onSave = { title, date, time, accent ->
                                // Only update — never create — from the edit route. If `existing` is
                                // null here the milestone is gone (deleted, or a stale/invalid id), so
                                // just dismiss rather than silently spawning a new one. (New milestones
                                // come exclusively from the ADD route.)
                                if (existing != null) {
                                    vm.updateMilestone(
                                        existing.copy(title = title, date = date, time = time, accent = accent),
                                    )
                                }
                                navController.popBackStack()
                            },
                            onCancel = { navController.popBackStack() },
                        )
                    }

                    composable(
                        route = Routes.DETAIL,
                        arguments = listOf(navArgument("id") { type = NavType.StringType }),
                    ) { entry ->
                        val id = entry.arguments?.getString("id")
                        val milestone = milestones.firstOrNull { it.id == id }
                        DetailScreen(
                            milestone = milestone,
                            showUnits = settings.showUnits,
                            onBack = { navController.popBackStack() },
                            onEdit = { if (id != null) navController.navigate(Routes.edit(id)) },
                            onReset = {
                                if (milestone != null) {
                                    // Capture a single instant so the date and time can't straddle midnight.
                                    val now = LocalDateTime.now()
                                    vm.updateMilestone(
                                        milestone.copy(date = now.toLocalDate(), time = now.toLocalTime().withNano(0)),
                                    )
                                }
                            },
                            onDelete = {
                                if (id != null) vm.deleteMilestone(id)
                                navController.popBackStack()
                            },
                        )
                    }

                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            settings = settings,
                            onSetThemeMode = settingsVm::setThemeMode,
                            onToggleUnits = settingsVm::setShowUnits,
                            onSetBackup = settingsVm::setBackupEnabled,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = !splashDone,
                enter = EnterTransition.None,
                exit = fadeOut(animationSpec = tween(250)),
                modifier = Modifier.fillMaxSize(),
            ) {
                SplashScreen(onFinished = { splashDone = true })
            }
        }
    }
}
