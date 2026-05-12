package com.brewmaster.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.brewmaster.presentation.screen.brew.BrewSession
import com.brewmaster.presentation.screen.cheatsheet.CheatSheetScreen
import com.brewmaster.presentation.screen.dashboard.DashboardScreen
import com.brewmaster.presentation.screen.brew.BrewTimerScreen
import com.brewmaster.presentation.screen.recipe.RecipeListScreen

@Composable
fun BrewMasterNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            DashboardScreen(
                onStartBrewing = { calculation ->
                    BrewSession.currentCalculation = calculation
                    navController.navigate("brew_timer")
                },
                onNavigateToRecipes = {
                    navController.navigate("recipes")
                },
                onNavigateToCheatSheet = {
                    navController.navigate("cheatsheet")
                }
            )
        }
        composable("brew_timer") {
            BrewTimerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("recipes") {
            RecipeListScreen(
                onNavigateBack = { navController.popBackStack() },
                onRecipeSelected = { recipe ->
                    BrewSession.selectedRecipe = recipe
                    navController.popBackStack("dashboard", inclusive = false)
                }
            )
        }
        composable("cheatsheet") {
            CheatSheetScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
