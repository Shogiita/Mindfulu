package com.example.mindfulu

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("mood")
data class MoodEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "mood") val mood:String
)
