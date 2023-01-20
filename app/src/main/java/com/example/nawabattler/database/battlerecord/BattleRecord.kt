package com.example.nawabattler.database.battlerecord

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BattleRecord(
    @PrimaryKey val id: Int,
    @NonNull @ColumnInfo(name = "my_score") val myScore: Int,
    @NonNull @ColumnInfo(name = "opponent_score") val opponentScore: Int,
    @NonNull @ColumnInfo(name = "opponent_image") val opponentImage: Int
)