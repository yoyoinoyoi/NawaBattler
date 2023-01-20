package com.example.nawabattler.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.nawabattler.database.battlerecord.BattleRecord
import com.example.nawabattler.database.battlerecord.BattleRecordDao


@Database(entities = [BattleRecord::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun battleDao(): BattleRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "app_database")
                    .createFromAsset("database/battle_record.db")
                    .build()
                INSTANCE = instance

                instance
            }
        }
    }
}