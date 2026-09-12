package com.brewmaster.presentation.screen.brew

import com.brewmaster.domain.model.BrewCalculation
import com.brewmaster.domain.model.PersonalRecipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object BrewSession {
    var currentCalculation: BrewCalculation? = null

    private val _pendingRecipe = MutableStateFlow<PersonalRecipe?>(null)
    val pendingRecipe: StateFlow<PersonalRecipe?> = _pendingRecipe.asStateFlow()

    // B4: setter still works from MainActivity / recipe list; Dashboard collects the flow.
    var selectedRecipe: PersonalRecipe?
        get() = _pendingRecipe.value
        set(value) { _pendingRecipe.value = value }
}
