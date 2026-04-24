package com.river.walklog.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.river.walklog.core.database.dao.DailyStepDao
import com.river.walklog.core.database.entity.DailyStepEntity

@Database(
    entities = [DailyStepEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class WalkLogDatabase : RoomDatabase() {
    abstract fun dailyStepDao(): DailyStepDao

    companion object {
        const val DATABASE_NAME = "walklog.db"

        /**
         * v1 → v2: Health Connect 마이그레이션.
         * - step_events 테이블 제거 (원시 센서 로그, HC 가 대체)
         * - daily_steps 에서 baselineSensorSteps, latestSensorSteps 컬럼 제거
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `step_events`")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `daily_steps_new` (
                        `dateEpochDay` INTEGER NOT NULL,
                        `totalSteps` INTEGER NOT NULL,
                        `targetSteps` INTEGER NOT NULL DEFAULT 6000,
                        `lastUpdatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`dateEpochDay`)
                    )
                    """.trimIndent(),
                )

                db.execSQL(
                    """
                    INSERT INTO `daily_steps_new` (dateEpochDay, totalSteps, targetSteps, lastUpdatedAt)
                    SELECT dateEpochDay, totalSteps, targetSteps, lastUpdatedAt FROM `daily_steps`
                    """.trimIndent(),
                )

                db.execSQL("DROP TABLE `daily_steps`")
                db.execSQL("ALTER TABLE `daily_steps_new` RENAME TO `daily_steps`")
            }
        }
    }
}
