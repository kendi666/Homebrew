package com.brewmaster.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.brewmaster.data.local.dao.CoffeeBeanDao
import com.brewmaster.data.local.dao.CoffeeProcessDao
import com.brewmaster.data.local.dao.PersonalRecipeDao
import com.brewmaster.data.local.entity.CoffeeBeanEntity
import com.brewmaster.data.local.entity.CoffeeProcessEntity
import com.brewmaster.data.local.entity.PersonalRecipeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [CoffeeProcessEntity::class, PersonalRecipeEntity::class, CoffeeBeanEntity::class],
    version = 4,
    exportSchema = false
)
abstract class BrewMasterDatabase : RoomDatabase() {

    abstract fun coffeeProcessDao(): CoffeeProcessDao
    abstract fun personalRecipeDao(): PersonalRecipeDao
    abstract fun coffeeBeanDao(): CoffeeBeanDao

    companion object {

        fun prepopulateCallback(scope: CoroutineScope, provider: () -> BrewMasterDatabase): Callback {
            return object : Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    scope.launch {
                        val database = provider()
                        val processDao = database.coffeeProcessDao()
                        val beanDao = database.coffeeBeanDao()
                        if (processDao.getCount() == 0) {
                            processDao.insertAll(PREPOPULATED_PROCESSES)
                        }
                        if (beanDao.getCount() == 0) {
                            beanDao.insertAll(PREPOPULATED_BEANS)
                        }
                    }
                }
            }
        }

        private val PREPOPULATED_PROCESSES = listOf(
            CoffeeProcessEntity(
                id = 1,
                processName = "Washed",
                tempMin = 93,
                tempMax = 95,
                grindRecommendation = "MEDIUM_FINE",
                extractionNote = "Clean & Bright",
                restingDays = 15,
                ratioMin = 17.0
            ),
            CoffeeProcessEntity(
                id = 2,
                processName = "Honey",
                tempMin = 90,
                tempMax = 92,
                grindRecommendation = "MEDIUM",
                extractionNote = "High Sweetness",
                restingDays = 15,
                ratioMin = 16.0
            ),
            CoffeeProcessEntity(
                id = 3,
                processName = "Natural",
                tempMin = 90,
                tempMax = 92,
                grindRecommendation = "MEDIUM",
                extractionNote = "Fruity & Balanced",
                restingDays = 20,
                ratioMin = 16.0
            ),
            CoffeeProcessEntity(
                id = 4,
                processName = "Anaerobic Washed",
                tempMin = 93,
                tempMax = 95,
                grindRecommendation = "MEDIUM",
                extractionNote = "Funky & Clean",
                restingDays = 20,
                ratioMin = 16.0
            ),
            CoffeeProcessEntity(
                id = 5,
                processName = "Anaerobic Natural",
                tempMin = 90,
                tempMax = 92,
                grindRecommendation = "MEDIUM",
                extractionNote = "Fruity & Winey",
                restingDays = 20,
                ratioMin = 16.0
            ),
            CoffeeProcessEntity(
                id = 6,
                processName = "Wet Hulled",
                tempMin = 93,
                tempMax = 95,
                grindRecommendation = "MEDIUM_FINE",
                extractionNote = "Earthy, herbal, syrupy body",
                restingDays = 15,
                ratioMin = 16.0
            ),
            CoffeeProcessEntity(
                id = 7,
                processName = "Dark Roast (Indo)",
                tempMin = 85,
                tempMax = 88,
                grindRecommendation = "MEDIUM_COARSE",
                extractionNote = "Heavy Body",
                restingDays = 10,
                ratioMin = 15.0
            )
        )

        private val PREPOPULATED_BEANS = listOf(
            CoffeeBeanEntity(
                id = 1,
                name = "Washed",
                origin = "Process",
                processId = 1,
                processName = "Washed",
                roastLevel = "Light",
                notes = "Clean & bright, high acidity"
            ),
            CoffeeBeanEntity(
                id = 2,
                name = "Honey",
                origin = "Process",
                processId = 2,
                processName = "Honey",
                roastLevel = "Medium",
                notes = "Sweet, smooth body"
            ),
            CoffeeBeanEntity(
                id = 3,
                name = "Natural",
                origin = "Process",
                processId = 3,
                processName = "Natural",
                roastLevel = "Medium",
                notes = "Fruity, wine-like, full body"
            ),
            CoffeeBeanEntity(
                id = 4,
                name = "Anaerobic Washed",
                origin = "Process",
                processId = 4,
                processName = "Anaerobic Washed",
                roastLevel = "Medium",
                notes = "Funky, fermented fruit, clean finish"
            ),
            CoffeeBeanEntity(
                id = 5,
                name = "Anaerobic Natural",
                origin = "Process",
                processId = 5,
                processName = "Anaerobic Natural",
                roastLevel = "Medium",
                notes = "Winey, boozy, tropical fruit"
            ),
            CoffeeBeanEntity(
                id = 6,
                name = "Wet Hulled",
                origin = "Process",
                processId = 6,
                processName = "Wet Hulled",
                roastLevel = "Medium",
                notes = "Earthy, herbal, spice notes, syrupy body"
            ),
            CoffeeBeanEntity(
                id = 7,
                name = "Dark Roast",
                origin = "Process",
                processId = 7,
                processName = "Dark Roast (Indo)",
                roastLevel = "Dark",
                notes = "Earthy, full body, low acidity"
            )
        )
    }
}
