package com.brewmaster.di

import com.brewmaster.data.repository.CoffeeBeanRepositoryImpl
import com.brewmaster.data.repository.CoffeeProcessRepositoryImpl
import com.brewmaster.data.repository.RecipeRepositoryImpl
import com.brewmaster.domain.repository.CoffeeBeanRepository
import com.brewmaster.domain.repository.CoffeeProcessRepository
import com.brewmaster.domain.repository.RecipeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindCoffeeProcessRepository(
        impl: CoffeeProcessRepositoryImpl
    ): CoffeeProcessRepository

    @Binds
    abstract fun bindRecipeRepository(
        impl: RecipeRepositoryImpl
    ): RecipeRepository

    @Binds
    abstract fun bindCoffeeBeanRepository(
        impl: CoffeeBeanRepositoryImpl
    ): CoffeeBeanRepository
}
