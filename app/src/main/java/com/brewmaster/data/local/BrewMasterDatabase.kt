package com.brewmaster.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.brewmaster.data.local.dao.BrewLogDao
import com.brewmaster.data.local.dao.CoffeeBeanDao
import com.brewmaster.data.local.dao.CoffeeProcessDao
import com.brewmaster.data.local.dao.PersonalRecipeDao
import com.brewmaster.data.local.entity.BrewLogEntity
import com.brewmaster.data.local.entity.CoffeeBeanEntity
import com.brewmaster.data.local.entity.CoffeeProcessEntity
import com.brewmaster.data.local.entity.PersonalRecipeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        CoffeeProcessEntity::class,
        PersonalRecipeEntity::class,
        CoffeeBeanEntity::class,
        BrewLogEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class BrewMasterDatabase : RoomDatabase() {

    abstract fun coffeeProcessDao(): CoffeeProcessDao
    abstract fun personalRecipeDao(): PersonalRecipeDao
    abstract fun coffeeBeanDao(): CoffeeBeanDao
    abstract fun brewLogDao(): BrewLogDao

    companion object {

        /**
         * Non-destructive upgrade from v4 to v5: adds the new "Infused" coffee
         * process + bean without dropping the user's saved personal_recipes.
         * INSERT OR IGNORE keeps it safe to run even if the row already exists.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "INSERT OR IGNORE INTO coffee_processes " +
                        "(id, process_name, temp_min, temp_max, grind_recommendation, extraction_note, resting_days, ratio_min) " +
                        "VALUES (8, 'Infused', 87, 90, 'MEDIUM', 'Aromatic & Floral - brew cooler, pour gently', 25, 16.5)"
                )
                db.execSQL(
                    "INSERT OR IGNORE INTO coffee_beans " +
                        "(id, name, origin, process_id, process_name, roast_level, notes, resting_days) " +
                        "VALUES (8, 'Infused', 'Process', 8, 'Infused', 'Light', " +
                        "'Intense added aromatics - lychee/floral/spice, gentle & cool brew', NULL)"
                )
            }
        }

        /**
         * Upgrade from v5 to v6: adds the brew_logs table for the Brew Journal.
         * Column types/nullability mirror BrewLogEntity exactly so Room's schema
         * validation passes. No data loss.
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `brew_logs` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`bean_name` TEXT NOT NULL, " +
                        "`technique_id` TEXT NOT NULL, " +
                        "`process_id` INTEGER NOT NULL, " +
                        "`grind_size` TEXT NOT NULL, " +
                        "`ratio` REAL NOT NULL, " +
                        "`coffee_weight` REAL NOT NULL, " +
                        "`is_ice` INTEGER NOT NULL, " +
                        "`temp_used` INTEGER, " +
                        "`rating` INTEGER NOT NULL, " +
                        "`notes` TEXT, " +
                        "`created_at` INTEGER NOT NULL)"
                )
            }
        }

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
            ),
            CoffeeProcessEntity(
                id = 8,
                processName = "Infused",
                // Infused / co-fermented lots carry intense added aromatics (fruit,
                // floral, spice). Brew COOLER so the volatile aromatics are preserved
                // and the ferment does not turn harsh or boozy.
                tempMin = 87,
                tempMax = 90,
                // Medium grind + gentle agitation avoids over-extracting the already
                // amplified flavour compounds.
                grindRecommendation = "MEDIUM",
                extractionNote = "Aromatic & Floral - brew cooler, pour gently",
                // Often anaerobic-style and very gassy, so they need a longer rest.
                restingDays = 25,
                // Slightly higher ratio (lighter) keeps the intense aromatics clean
                // rather than cloying.
                ratioMin = 16.5
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
            ),
            CoffeeBeanEntity(
                id = 8,
                name = "Infused",
                origin = "Process",
                processId = 8,
                processName = "Infused",
                roastLevel = "Light",
                notes = "Intense added aromatics - lychee/floral/spice, gentle & cool brew"
            )
        )
    }
}
