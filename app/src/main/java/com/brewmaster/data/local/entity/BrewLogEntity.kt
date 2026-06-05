package com.brewmaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "brew_logs")
data class BrewLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "bean_name") val beanName: String,
    @ColumnInfo(name = "technique_id") val techniqueId: String,
    @ColumnInfo(name = "process_id") val processId: Int,
    @ColumnInfo(name = "grind_size") val grindSize: String,
    @ColumnInfo(name = "ratio") val ratio: Double,
    @ColumnInfo(name = "coffee_weight") val coffeeWeight: Double,
    @ColumnInfo(name = "is_ice") val isIce: Boolean,
    @ColumnInfo(name = "temp_used") val tempUsed: Int?,
    @ColumnInfo(name = "rating") val rating: Int,
    @ColumnInfo(name = "notes") val notes: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long
)
