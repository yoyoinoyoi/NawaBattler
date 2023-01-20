package com.example.nawabattler.database.battlerecord

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface BattleRecordDao {

    @Query("SELECT * FROM battleRecord ORDER BY id ASC")
    fun getAll(): Flow<List<BattleRecord>>

    @Query("SELECT * FROM battleRecord ORDER BY id DESC LIMIT 10")
    fun getRecentResult(): Flow<List<BattleRecord>>

}