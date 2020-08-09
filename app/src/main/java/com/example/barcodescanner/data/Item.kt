package com.example.barcodescanner.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items_table")
data class Item (

    @PrimaryKey(autoGenerate = true)
    val key: Long? = null,

    @ColumnInfo(name = "code")
    val code: String,

    @ColumnInfo(name = "barcode1")
    val barcode1: String? = "",

    @ColumnInfo(name = "barcode2", defaultValue = "")
    val barcode2: String?,

    @ColumnInfo(name = "name", defaultValue = "ללא שם")
    val name: String?,

    @ColumnInfo(name = "vendor", defaultValue = "לא משויך לספק")
    val vendor: String?,

    @ColumnInfo(name = "cost", defaultValue = "0.0")
    val cost: Double?,

    @ColumnInfo(name = "price", defaultValue = "0.0")
    val price: Double?,

    @ColumnInfo(name = "storage", defaultValue = "0")
    val storage: Int?,

    @ColumnInfo(name = "quantity", defaultValue = "0")
    val quantity: Int?
)



