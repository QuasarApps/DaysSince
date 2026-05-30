package com.quasarapps.dayssince.ui

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
import com.quasarapps.dayssince.ui.detail.DetailScreen
import com.quasarapps.dayssince.ui.edit.EditMilestoneScreen
import com.quasarapps.dayssince.ui.home.HomeScreen
import com.quasarapps.dayssince.ui.theme.DaysSinceTheme

private object Routes {
    const val HOME = "home"
    const val ADD = "add"
    const val EDIT = "edit/{id}"
    const val DETAIL = "detail/{id}"

    fun edit(id: String) = "edit/$id"
    fun detail(id: String) = "detail/$id"
}

@Composable
fun DaysSinceApp(initialMilestoneId: String? = null) {
    DaysSinceTheme {
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
                    onBack = { navController.popBackStack() },
                    onEdit = { if (id != null) navController.navigate(Routes.edit(id)) },
                    onDelete = {
                        if (id != null) vm.deleteMilestone(id)
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}
