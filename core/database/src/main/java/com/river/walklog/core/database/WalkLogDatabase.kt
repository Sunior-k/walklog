package com.river.walklog.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.river.walklog.core.database.dao.DailyStepDao
import com.river.walklog.core.database.dao.PointsLedgerDao
import com.river.walklog.core.database.dao.RewardRedemptionDao
import com.river.walklog.core.database.entity.DailyStepEntity
import com.river.walklog.core.database.entity.PointsLedgerEntity
import com.river.walklog.core.database.entity.RewardRedemptionEntity

@Database(
    entities = [DailyStepEntity::class, RewardRedemptionEntity::class, PointsLedgerEntity::class],
    version = 4,
    exportSchema = true,
)
abstract class WalkLogDatabase : RoomDatabase() {
    abstract fun dailyStepDao(): DailyStepDao
    abstract fun rewardRedemptionDao(): RewardRedemptionDao
    abstract fun pointsLedgerDao(): PointsLedgerDao

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

        /**
         * v2 → v3: 리워드 스토어 실제 교환 로직 지원.
         * - reward_redemptions: 상품 교환 기록(뱃지 보유, 쿠폰 코드, 기부 누적 계산용)
         * - points_ledger: 포인트 적립/차감 내역
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `reward_redemptions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `rewardId` TEXT NOT NULL,
                        `pointsCost` INTEGER NOT NULL,
                        `redeemedAtEpochMillis` INTEGER NOT NULL,
                        `couponCode` TEXT
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `points_ledger` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `deltaPoints` INTEGER NOT NULL,
                        `reason` TEXT NOT NULL,
                        `createdAtEpochMillis` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }

        /**
         * v3 → v4: 뱃지/테마/기부/포인트 내역 Firestore 백업.
         * - reward_redemptions, points_ledger에 remoteId 추가 (Firestore 문서 ID, 업로드 성공 후 채워짐)
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `reward_redemptions` ADD COLUMN `remoteId` TEXT")
                db.execSQL("ALTER TABLE `points_ledger` ADD COLUMN `remoteId` TEXT")
            }
        }
    }
}
