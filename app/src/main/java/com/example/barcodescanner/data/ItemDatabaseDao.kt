package com.example.barcodescanner.data

import androidx.room.*

/**
 * Defines methods for using the SleepNight class with Room.
 */
@Dao
interface ItemDatabaseDao {

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: Item)

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items :List<Item>)

    /**
     * When updating a row with a value already set in a column,
     * replaces the old value with the new one.
     * @param item new value to write
     */
    @Update
    fun update(item: Item)

    /**
     * Deletes all values from the table.
     * This does not delete the table, only its contents.
     */
    @Query("DELETE FROM items_table")
    fun clear()

    /**
     * Selects and returns all rows in the table,
     * sorted by start time in descending order.
     */
    @Query("SELECT * FROM items_table ORDER BY name ASC")
    fun getAllItems(): List<Item>

    @Query("SELECT count(code) FROM items_table ")
    fun countItems() : Int?

    @Query("SELECT * FROM items_table WHERE barcode1= :barcode")
    fun getBarcode1(barcode: String): List<Item>?

    @Query("SELECT * FROM items_table WHERE barcode2= :barcode")
    fun getBarcode2(barcode: String): List<Item>?

}