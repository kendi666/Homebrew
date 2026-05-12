package com.brewmaster.presentation.screen.brew

import com.brewmaster.domain.model.BrewCalculation
import com.brewmaster.domain.model.PersonalRecipe

object BrewSession {
    var currentCalculation: BrewCalculation? = null
    var selectedRecipe: PersonalRecipe? = null
}
