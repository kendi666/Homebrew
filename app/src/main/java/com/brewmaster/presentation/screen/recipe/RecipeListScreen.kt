package com.brewmaster.presentation.screen.recipe

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.Context
import android.content.Intent
import com.brewmaster.domain.model.GrindSize
import com.brewmaster.domain.model.PersonalRecipe
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    onNavigateBack: () -> Unit,
    onRecipeSelected: (PersonalRecipe) -> Unit,
    viewModel: RecipeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Recipes",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            state.recipes.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "\u2615",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No saved recipes yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.recipes,
                        key = { it.id }
                    ) { recipe ->
                        val techniqueLabel = state.techniqueNames[recipe.techniqueId]
                            ?: prettifyId(recipe.techniqueId)
                        val processLabel = state.processNames[recipe.processId]
                            ?: if (recipe.processId == 0) "No process" else "Process #${recipe.processId}"
                        val grinderName = recipe.grinderId?.let { state.grinderNames[it] }
                        RecipeSwipeItem(
                            recipe = recipe,
                            techniqueLabel = techniqueLabel,
                            processLabel = processLabel,
                            grinderName = grinderName,
                            onDelete = { viewModel.requestDeleteRecipe(recipe) },
                            onShare = { shareRecipe(context, recipe, techniqueLabel, grinderName) },
                            onClick = { viewModel.openRecipe(recipe) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }

        val open = state.recipeOpen
        if (open != null) {
            RecipeDetailDialog(
                recipe = open,
                techniqueLabel = state.techniqueNames[open.techniqueId] ?: prettifyId(open.techniqueId),
                processLabel = state.processNames[open.processId]
                    ?: if (open.processId == 0) "No process" else "Process #${open.processId}",
                grinderName = open.grinderId?.let { state.grinderNames[it] },
                onEdit = {
                    viewModel.closeRecipe()
                    onRecipeSelected(open)
                },
                onDelete = {
                    viewModel.closeRecipe()
                    viewModel.requestDeleteRecipe(open)
                },
                onDismiss = { viewModel.closeRecipe() }
            )
        }

        val pending = state.recipePendingDeletion
        if (pending != null) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelDeleteRecipe() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text(text = "Delete recipe?") },
                text = {
                    Text(
                        text = "\"${pending.beanName}\" will be permanently removed. " +
                            "This action cannot be undone."
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmDeleteRecipe() }) {
                        Text(
                            text = "Delete",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelDeleteRecipe() }) {
                        Text(text = "Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeSwipeItem(
    recipe: PersonalRecipe,
    techniqueLabel: String,
    processLabel: String,
    grinderName: String?,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onClick: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            // Don't delete on the swipe itself — ask for confirmation first and let
            // the row snap back (return false) so nothing is removed by accident.
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
            }
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.surface
                },
                label = "swipe_bg"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        RecipeCard(
            recipe = recipe,
            techniqueLabel = techniqueLabel,
            processLabel = processLabel,
            onShare = onShare,
            onClick = onClick,
            grinderName = grinderName
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeCard(
    recipe: PersonalRecipe,
    techniqueLabel: String,
    processLabel: String,
    grinderName: String?,
    onShare: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recipe.beanName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatDate(recipe.createdAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share recipe",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoChip(label = techniqueLabel)
                InfoChip(label = processLabel)
            }

            Spacer(modifier = Modifier.height(8.dp))

            RecipeFacts(
                recipe = recipe,
                grinderName = grinderName
            )

            if (!recipe.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = recipe.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun InfoChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/** Builds a human-readable message and opens the Android share sheet. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeFacts(
    recipe: PersonalRecipe,
    grinderName: String?
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Fact("${formatAmount(recipe.coffeeWeight)}g coffee")
        Fact("1:${formatAmount(recipe.ratio)}")
        Fact(grindLabel(recipe.grindSize))
        grinderName?.let { Fact(it) }
        recipe.grinderSetting?.let { Fact("dial $it") }
        if (recipe.tempMin != null && recipe.tempMax != null) {
            Fact("${recipe.tempMin}–${recipe.tempMax}°C")
        }
        if (recipe.isIce) {
            Text(
                text = "Iced",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun Fact(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        softWrap = false
    )
}

@Composable
private fun RecipeDetailDialog(
    recipe: PersonalRecipe,
    techniqueLabel: String,
    processLabel: String,
    grinderName: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = recipe.beanName) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = formatDate(recipe.createdAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$techniqueLabel · $processLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                RecipeFacts(recipe = recipe, grinderName = grinderName)
                if (!recipe.notes.isNullOrBlank()) {
                    Text(
                        text = recipe.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onEdit) {
                Text(text = "Edit")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) {
                    Text(text = "Close")
                }
                TextButton(onClick = onDelete) {
                    Text(text = "Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}

private fun shareRecipe(context: Context, recipe: PersonalRecipe, techniqueLabel: String, grinderName: String?) {
    val message = buildString {
        appendLine("☕ ${recipe.beanName} — Homebrew recipe")
        appendLine("Technique: $techniqueLabel")
        appendLine("Dose ${formatAmount(recipe.coffeeWeight)}g · 1:${formatAmount(recipe.ratio)} · ${grindLabel(recipe.grindSize)}${grinderName?.let { " · $it" }.orEmpty()}${recipe.grinderSetting?.let { " · dial $it" }.orEmpty()}${if (recipe.tempMin != null && recipe.tempMax != null) " · ${recipe.tempMin}–${recipe.tempMax}°C" else ""}${if (recipe.isIce) " · Iced" else ""}")
        if (!recipe.notes.isNullOrBlank()) appendLine("Notes: ${recipe.notes}")
    }
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Homebrew recipe: ${recipe.beanName}")
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share recipe"))
}

private fun grindLabel(size: GrindSize): String = "${size.label} ${size.microns}μm"

private fun formatAmount(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
    }
}

/** Fallback for technique ids with no registered name, e.g. "hybrid_immersion" -> "Hybrid Immersion". */
private fun prettifyId(id: String): String =
    id.split('_')
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercaseChar() }
        }
