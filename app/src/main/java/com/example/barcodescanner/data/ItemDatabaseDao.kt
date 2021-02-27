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
     */
    @Query("SELECT * FROM items_table group by code")
    fun getAllItems(): List<Item>

    @Query("SELECT count(code) FROM items_table ")
    fun countItems(): Int?

    @Query("SELECT * FROM items_table WHERE barcode1= :barcode")
    fun getItemBarcode1(barcode: String): List<Item>

    @Query("SELECT * FROM items_table WHERE barcode2= :barcode")
    fun getItemBarcode2(barcode: String): List<Item>


    @Query("SELECT vendor FROM ITEMS_TABLE GROUP BY vendor order by vendor desc")
    fun getVendorsList(): List<String?>

    @Query(
        """
            select * from items_table 
            where vendor LIKE  '%'||:vendor||'%' and 
                (name LIKE '%'||:searchKeyStr||'%' or
                code LIKE '%'||:searchKeyStr||'%' or 
                barcode1  LIKE '%'||:searchKeyStr||'%' or
                barcode2 LIKE '%'||:searchKeyStr||'%') 
            group by code"""
    )
    fun getItems(vendor: String = "", searchKeyStr: String = ""): List<Item>

    @Query(
        """
            select * from items_table 
            where vendor LIKE  '%'||:vendor||'%' and 
                (name LIKE '%'||:searchKeyStr||'%' or
                code LIKE '%'||:searchKeyStr||'%' or 
                barcode1  LIKE '%'||:searchKeyStr||'%' or
                barcode2 LIKE '%'||:searchKeyStr||'%') and
                storage= :storage and quantity > 0
            group by code"""
    )
    fun getItemsInStock(vendor: String = "", searchKeyStr: String = "", storage: Int): List<Item>

    @Query("select quantity from items_table where code= :itemCode and storage= :storage ")
    fun getInventory(itemCode: String, storage: Int): Int

    @Query("select barcode1 from items_table where code= :itemCode group by barcode1 ")
    fun getBarcode1sList(itemCode: String): List<String?>

    @Query("select barcode2 from items_table where code= :itemCode group by barcode2 ")
    fun getBarcodes2List(itemCode: String): List<String?>

    @Query("select * from items_table where  storage= :storage and quantity > 0 group by code")
    fun getAllItemsInStock(storage: Int): List<Item>

}