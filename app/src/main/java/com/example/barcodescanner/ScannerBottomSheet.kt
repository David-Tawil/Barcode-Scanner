package com.example.barcodescanner

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.barcodescanner.data.Item
import com.example.barcodescanner.data.ItemDatabase
import com.example.barcodescanner.data.ItemDatabaseDao
import com.example.barcodescanner.utilities.TAG
import com.example.barcodescanner.utilities.UtilTools
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.item_details_bottom_sheet.*
import java.text.DecimalFormat

class ScannerBottomSheet(private val item: Item) : BottomSheetDialogFragment() {

    private lateinit var database: ItemDatabaseDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        (dialog as? BottomSheetDialog)?.let {
            it.behavior.peekHeight = 1200
        }
        val rootView = inflater.inflate(R.layout.item_details_bottom_sheet, container, false)
        database = ItemDatabase.getInstance(requireContext()).itemDatabaseDao
        /*val bottomSheetBehavior = BottomSheetBehavior.from(rootView as View)

        bottomSheetBehavior.setPeekHeight(700,true)
*/
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        code_item_bottom_sheet.text = item.code
        name_bottom_sheet.text = item.name
        cost_bottom_sheet.text = resources.getString(
            R.string.cost_item_bottom_sheet,
            DecimalFormat("0.00").format(item.cost ?: 0.0)
        )
        price_bottom_sheet.text = resources.getString(
            R.string.price_item_bottom_sheet,
            DecimalFormat("0.00").format(item.price ?: 0.0)
        )
        profit_bottom_sheet.text = resources.getString(
            R.string.profit_item_bottom_sheet,
            DecimalFormat("0.00").format(UtilTools.getProfit(item.cost, item.price) * 100)
        )
        vendor_bottom_sheet.text = item.vendor

        store_inventory_bottom_sheet.text = database.getInventory(item.code, 1).toString()
        warehouse_bottom_sheet.text = database.getInventory(item.code, 2).toString()
        vendor_inventory_bottom_sheet.text = database.getInventory(item.code, 3).toString()


        val barcodes =
            database.getBarcode1sList(item.code).plus(database.getBarcodes2List(item.code))
                .filterNotNull()
        Log.d(TAG, "barcodes size : ${barcodes.size} text: ${barcodes.joinToString()} ")
        barcodes_bottom_sheet.adapter =
            ArrayAdapter(requireContext(), R.layout.row_list_barcodes, barcodes)
    }

}