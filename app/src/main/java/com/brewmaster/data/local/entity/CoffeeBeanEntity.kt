package com.brewmaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coffee_beans")
data class CoffeeBeanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "origin") val origin: String,
    @ColumnInfo(name = "process_id") val processId: Int,
    @ColumnInfo(name = "process_name") val processName: String,
    @ColumnInfo(name = "roast_level") val roastLevel: String,
    @ColumnInfo(name = "notes") val notes: String? = null,
    @ColumnInfo(name = "resting_days") val restingDays: Int? = null
)
