package com.brewmaster.di

import android.content.Context
import androidx.room.Room
import com.brewmaster.data.local.BrewMasterDatabase
import com.brewmaster.data.local.dao.BrewLogDao
import com.brewmaster.data.local.dao.CoffeeBeanDao
import com.brewmaster.data.local.dao.CoffeeProcessDao
import com.brewmaster.data.local.dao.PersonalRecipeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BrewMasterDatabase {
        var instance: BrewMasterDatabase? = null
        val scope = CoroutineScope(SupervisorJob())
        return Room.databaseBuilder(
            context,
            BrewMasterDatabase::class.java,
            "brewmaster.db"
        )
            .addMigrations(BrewMasterDatabase.MIGRATION_4_5, BrewMasterDatabase.MIGRATION_5_6)
            .fallbackToDestructiveMigration()
            .addCallback(BrewMasterDatabase.prepopulateCallback(scope) { instance!! })
            .build()
            .also { instance = it }
    }

    @Provides
    fun provideCoffeeProcessDao(database: BrewMasterDatabase): CoffeeProcessDao {
        return database.coffeeProcessDao()
    }

    @Provides
    fun providePersonalRecipeDao(database: BrewMasterDatabase): PersonalRecipeDao {
        return database.personalRecipeDao()
    }

    @Provides
    fun provideCoffeeBeanDao(database: BrewMasterDatabase): CoffeeBeanDao {
        return database.coffeeBeanDao()
    }

    @Provides
    fun provideBrewLogDao(database: BrewMasterDatabase): BrewLogDao {
        return database.brewLogDao()
    }
}
