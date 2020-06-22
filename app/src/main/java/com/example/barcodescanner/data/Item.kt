package com.example.barcodescanner.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items_table")
data class Item (
    @PrimaryKey (autoGenerate = false)
    val code : String,

    @ColumnInfo(name = "barcode1")
    val barcode1 : String? ="",

    @ColumnInfo(name = "barcode2")
    val barcode2 : String? ="",

    @ColumnInfo(name = "name")
    val name : String? ="ללא שם",

    @ColumnInfo(name = "vendor")
    val vendor : String? ="לא משויך לספק",

    @ColumnInfo(name = "cost")
    val cost : Double? = 0.0,

    @ColumnInfo(name = "price")
    val price : Double? = 0.0
)

