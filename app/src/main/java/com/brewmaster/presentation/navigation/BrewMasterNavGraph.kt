package com.brewmaster.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.brewmaster.presentation.screen.brew.BrewSession
import com.brewmaster.presentation.screen.cheatsheet.CheatSheetScreen
import com.brewmaster.presentation.screen.dashboard.DashboardScreen
import com.brewmaster.presentation.screen.brew.BrewTimerScreen
import com.brewmaster.presentation.screen.journal.BrewJournalScreen
import com.brewmaster.presentation.screen.recipe.RecipeListScreen
import com.brewmaster.presentation.screen.convert.GrindConvertScreen
import com.brewmaster.presentation.screen.troubleshoot.TroubleshootScreen

@Composable
fun BrewMasterNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") { entry ->
            DashboardScreen(
                onStartBrewing = { calculation ->
                    entry.ifResumed {
                        BrewSession.currentCalculation = calculation
                        navController.navigate("brew_timer") { launchSingleTop = true }
                    }
                },
                onNavigateToRecipes = {
                    entry.ifResumed { navController.navigate("recipes") }
                },
                onNavigateToCheatSheet = {
                    entry.ifResumed { navController.navigate("cheatsheet") }
                },
                onNavigateToJournal = {
                    entry.ifResumed { navController.navigate("journal") }
                },
                onNavigateToTroubleshoot = {
                    entry.ifResumed { navController.navigate("troubleshoot") }
                },
                onNavigateToGrindConvert = {
                    entry.ifResumed { navController.navigate("grind_convert") }
                }
            )
        }
        composable("brew_timer") { entry ->
            BrewTimerScreen(
                onNavigateBack = { entry.ifResumed { navController.popBackStack() } }
            )
        }
        composable("recipes") { entry ->
            RecipeListScreen(
                onNavigateBack = { entry.ifResumed { navController.popBackStack() } },
                onRecipeSelected = { recipe ->
                    entry.ifResumed {
                        BrewSession.selectedRecipe = recipe
                        navController.popBackStack("dashboard", inclusive = false)
                    }
                }
            )
        }
        composable("cheatsheet") { entry ->
            CheatSheetScreen(
                onNavigateBack = { entry.ifResumed { navController.popBackStack() } }
            )
        }
        composable("journal") { entry ->
            BrewJournalScreen(
                onNavigateBack = { entry.ifResumed { navController.popBackStack() } }
            )
        }
        composable("troubleshoot") { entry ->
            TroubleshootScreen(
                onNavigateBack = { entry.ifResumed { navController.popBackStack() } }
            )
        }
        composable("grind_convert") { entry ->
            GrindConvertScreen(
                onNavigateBack = { entry.ifResumed { navController.popBackStack() } }
            )
        }
    }
}

/**
 * Ignores taps once this screen is leaving. A double tap on START or Back
 * would otherwise open two timers or pop the dashboard and leave a blank screen.
 */
private inline fun NavBackStackEntry.ifResumed(block: () -> Unit) {
    if (lifecycle.currentState == Lifecycle.State.RESUMED) block()
}
