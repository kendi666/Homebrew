package com.brewmaster

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.brewmaster.domain.share.RecipeShareCodec
import com.brewmaster.presentation.navigation.BrewMasterNavGraph
import com.brewmaster.presentation.screen.brew.BrewSession
import com.brewmaster.presentation.theme.BrewMasterTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // A shared recipe link may have launched the app — prefill it on the dashboard.
        handleRecipeDeepLink(intent)
        enableEdgeToEdge()
        setContent {
            BrewMasterTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BrewMasterNavGraph(navController = navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleRecipeDeepLink(intent)
    }

    private fun handleRecipeDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        if (intent.action != Intent.ACTION_VIEW) return
        RecipeShareCodec.decode(data.toString())?.let { recipe ->
            BrewSession.selectedRecipe = recipe
        }
    }
}
