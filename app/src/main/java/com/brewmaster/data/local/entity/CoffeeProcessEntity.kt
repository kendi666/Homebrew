package com.brewmaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coffee_processes")
data class CoffeeProcessEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "process_name") val processName: String,
    @ColumnInfo(name = "temp_min") val tempMin: Int,
    @ColumnInfo(name = "temp_max") val tempMax: Int,
    @ColumnInfo(name = "grind_recommendation") val grindRecommendation: String,
    @ColumnInfo(name = "extraction_note") val extractionNote: String,
    @ColumnInfo(name = "resting_days") val restingDays: Int = 15,
    @ColumnInfo(name = "ratio_min") val ratioMin: Double = 16.0
)
