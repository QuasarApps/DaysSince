package com.quasarapps.pulsar.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

@Composable
fun PulsarApp(initialMilestoneId: String? = null) {
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
        val navController = rememberNavController()
        val vm: MilestonesViewModel = viewModel()
        val milestones by vm.milestones.collectAsState()

        // Deep-link from a widget tap: jump straight to that milestone's detail.
        //
        // Guard with a saveable flag so this fires exactly once for a given launch.
        // The flag is restored across Activity recreation (e.g. a device rotation),
        // so the effect won't re-run and re-navigate to the detail screen after the
        // user has already navigated elsewhere — otherwise rotating anywhere in the
        // app would snap back to this milestone's detail. The NavController restores
        // its own back stack across rotation, so honoring it is all we need to do.
        var deepLinkHandled by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(initialMilestoneId) {
            if (initialMilestoneId != null && !deepLinkHandled) {
                deepLinkHandled = true
                navController.navigate(Routes.detail(initialMilestoneId))
            }
        }

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
                        if (existing != null) {
                            vm.updateMilestone(
                                existing.copy(title = title, date = date, time = time, accent = accent),
                            )
                        } else {
                            vm.addMilestone(title, date, time, accent)
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
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
