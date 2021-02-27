package com.example.barcodescanner

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.barcodescanner.data.Item
import com.example.barcodescanner.data.ItemDatabase
import com.example.barcodescanner.data.ItemDatabaseDao
import com.example.barcodescanner.recyclerview.RecyclerViewAdapter
import com.example.barcodescanner.utilities.SortBy
import com.example.barcodescanner.utilities.UtilTools
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_search_result.*
import kotlinx.android.synthetic.main.item_details_bottom_sheet.*
import java.text.DecimalFormat

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SearchResultFragment : Fragment(), RecyclerViewAdapter.OnItemListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var items: List<Item>
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private lateinit var database: ItemDatabaseDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //setting the order by preference to default by price (3 value)
        requireActivity().getPreferences(Context.MODE_PRIVATE)
            .edit { putInt(resources.getString(R.string.sort_by_key), 3) }

        val vendor = SearchResultFragmentArgs.fromBundle(requireArguments()).vendor
        val searchKey: String = SearchResultFragmentArgs.fromBundle(requireArguments()).searchKey
        val onlyInStock: Boolean =
            SearchResultFragmentArgs.fromBundle(requireArguments()).onlyInStock


        database = ItemDatabase.getInstance(requireContext()).itemDatabaseDao

        items = if (onlyInStock)
            database.getItemsInStock(vendor, searchKey, 1)
        else
            database.getItems(vendor, searchKey)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_search_result, container, false)

        bottomSheetBehavior =
            BottomSheetBehavior.from(rootView.findViewById(R.id.bottom_sheet) as View)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.setPeekHeight(700, true)

        recyclerView = rootView.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerViewAdapter = RecyclerViewAdapter(items, this)
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )


        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                rootView.findViewById<TextView>(R.id.textview_item_sum_info).text =
                    resources.getString(R.string.sum_item, recyclerViewAdapter.itemCount)
                UtilTools.hideKeyboard(requireActivity())
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        })

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textview_item_sum_info.text =
            resources.getString(R.string.sum_item, recyclerViewAdapter.itemCount)
    }

    override fun onItemClick(position: Int) {
        setBottomSheetInfo(recyclerViewAdapter.itemsFilterResults[position])
        textview_item_sum_info.text =
            resources.getString(R.string.sum_item, recyclerViewAdapter.itemCount)
        UtilTools.hideKeyboard(requireActivity())
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun setBottomSheetInfo(item: Item) {
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
            database.getBarcodes2List(item.code).plus(database.getBarcode1sList(item.code))
                .filterNotNull()

        barcodes_bottom_sheet.adapter =
            ArrayAdapter(requireContext(), R.layout.row_list_barcodes, barcodes)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_resolt_fragment_menu, menu)

        val searchItem = menu.findItem(R.id.search_action_menu_item)
        val searchView: androidx.appcompat.widget.SearchView =
            searchItem.actionView as androidx.appcompat.widget.SearchView

        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.queryHint = resources.getString(R.string.search_hint_menu)

        searchView.setOnQueryTextFocusChangeListener { v, hasFocus ->
            textview_item_sum_info.text =
                resources.getString(R.string.sum_item, recyclerViewAdapter.itemCount)
        }

        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                textview_item_sum_info.text =
                    resources.getString(R.string.sum_item, recyclerViewAdapter.itemCount)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                recyclerViewAdapter.filter.filter(newText)
                return false
            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.sort_menu_item -> {
                showSortDialog()
                return true
            }
            R.id.order_menu_item -> {
                recyclerViewAdapter.swapOrder()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    private fun showSortDialog() {

        val items = resources.getStringArray(R.array.order_by_options)
        val lastSortOption = requireActivity().getPreferences(Context.MODE_PRIVATE)
            .getInt(resources.getString(R.string.sort_by_key), 0)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.sort))
            .setIcon(R.drawable.ic_sort_24px)
            .setSingleChoiceItems(items, lastSortOption) { dialog, which ->
                requireActivity().getPreferences(Context.MODE_PRIVATE)
                    .edit { putInt(resources.getString(R.string.sort_by_key), which) }
                val sortBy: SortBy = when (which) {
                    0 -> SortBy.NAME
                    1 -> SortBy.CODE
                    2 -> SortBy.COST
                    3 -> SortBy.PRICE
                    4 -> SortBy.STOCK
                    5 -> SortBy.PROFIT
                    else -> SortBy.NAME
                }
                recyclerViewAdapter.sortBy(sortBy)
                dialog.dismiss()
            }
            .show()
    }
}