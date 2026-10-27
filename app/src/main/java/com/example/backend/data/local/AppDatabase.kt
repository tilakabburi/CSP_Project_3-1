package com.example.backend.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.backend.data.local.dao.*
import com.example.backend.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        NutritionTargetEntity::class,
        FoodEntity::class,
        FoodPoolEntity::class,
        DailyPlanEntity::class,
        MealLogEntity::class,
        WaterLogEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun nutritionTargetDao(): NutritionTargetDao
    abstract fun foodDao(): FoodDao
    abstract fun foodPoolDao(): FoodPoolDao
    abstract fun dailyPlanDao(): DailyPlanDao
    abstract fun mealLogDao(): MealLogDao
    abstract fun waterLogDao(): WaterLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Explicit forward migration preserving existing user profiles, meal plans, and logs.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_meal_logs_userId_date` ON `meal_logs` (`userId`, `date`)"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutripath_database"
                )
                    .addMigrations(MIGRATION_4_5)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
