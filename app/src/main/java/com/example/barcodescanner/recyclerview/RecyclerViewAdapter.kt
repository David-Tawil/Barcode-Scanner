package com.example.barcodescanner.recyclerview


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.barcodescanner.R
import com.example.barcodescanner.data.Item
import com.example.barcodescanner.data.ItemDatabase
import com.example.barcodescanner.utilities.SortBy
import com.example.barcodescanner.utilities.UtilTools
import java.text.DecimalFormat

/**
 * Provide views to RecyclerView with data from dataSet.
 *
 * Initialize the dataset of the Adapter.
 *
 * @param itemsList : List<Item> - containing the data to populate views to be used by RecyclerView.
 */


class RecyclerViewAdapter(
    private val itemsList: List<Item>,
    private val onItemClickListener: OnItemListener
) :
    RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(), Filterable {

    private var sortBy = SortBy.PRICE

    var itemsFilterResults: List<Item>
        private set

    private var itemsSorted: List<Item> = itemsList.sortedBy { it.price }

    private var ascending = true

    init {
        itemsFilterResults = itemsSorted
    }
    //copy of original at first and updated as needed with filter on original


    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    class ViewHolder(v: View, private val onItemListener: OnItemListener) :
        RecyclerView.ViewHolder(v), View.OnClickListener {
        val itemTitleText: TextView = v.findViewById(R.id.item_title_row_text_view)
        val itemPriceText: TextView = v.findViewById(R.id.item_price_row_text_view)
        val itemCodeText: TextView = v.findViewById(R.id.item_code_row_text_view)
        val itemStockText: TextView = v.findViewById(R.id.item_stock_row_text_view)

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            onItemListener.onItemClick(adapterPosition)
        }
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view.
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycler_view_row_item, viewGroup, false)

        return ViewHolder(v, onItemClickListener)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val database = ItemDatabase.getInstance(viewHolder.itemView.context).itemDatabaseDao
        val item = itemsFilterResults[position]
        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        viewHolder.itemTitleText.text = item.name
        val priceText =
            "₪${DecimalFormat("0.00").format(item.price ?: 0.0)}"
        viewHolder.itemPriceText.text = priceText
        viewHolder.itemStockText.text =
            database.getInventory(item.code, 1).toString().plus(" במלאי")
        viewHolder.itemCodeText.text = item.code
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = itemsFilterResults.size


    interface OnItemListener {
        fun onItemClick(position: Int)
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {

                return if (constraint == null || constraint.isEmpty()) {
                    FilterResults().also { it.values = itemsSorted }
                } else {
                    FilterResults().also { it.values = filterOriginalList(constraint) }
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                itemsFilterResults = results?.values as List<Item>
                notifyDataSetChanged()

            }
        }
    }

    private fun filterOriginalList(constraint: CharSequence): List<Item> {
        return itemsSorted.filter {
            (it.name?.contains(constraint, true) ?: false) ||
                    (it.vendor?.contains(constraint, true) ?: false) ||
                    it.code.contains(constraint) ||
                    (it.barcode1?.contains(constraint) ?: false) ||
                    (it.barcode2?.contains(constraint) ?: false)
        }

    }

    fun sortBy(sortBy: SortBy) {
        when (sortBy) {
            SortBy.NAME -> {
                this.sortBy = SortBy.NAME
                itemsSorted = itemsList.sortedBy { it.name }
                itemsFilterResults = itemsFilterResults.sortedBy { it.name }
            }
            SortBy.CODE -> {
                this.sortBy = SortBy.CODE
                itemsSorted = itemsList.sortedBy { it.code }
                itemsFilterResults = itemsFilterResults.sortedBy { it.code }
            }
            SortBy.COST -> {
                this.sortBy = SortBy.COST
                itemsSorted = itemsList.sortedBy { it.cost }
                itemsFilterResults = itemsFilterResults.sortedBy { it.cost }
            }
            SortBy.PRICE -> {
                this.sortBy = SortBy.PRICE
                itemsSorted = itemsList.sortedBy { it.price }
                itemsFilterResults = itemsFilterResults.sortedBy { it.price }
            }
            SortBy.STOCK -> {
                this.sortBy = SortBy.STOCK
                itemsSorted = itemsList.sortedByDescending { it.quantity }
                itemsFilterResults = itemsFilterResults.sortedByDescending { it.quantity }


            }
            SortBy.PROFIT -> {
                this.sortBy = SortBy.PROFIT
                itemsSorted = itemsList.sortedBy { UtilTools.getProfit(it.cost, it.price) }
                itemsFilterResults =
                    itemsFilterResults.sortedBy { UtilTools.getProfit(it.cost, it.price) }
            }

        }
        ascending = true
        notifyDataSetChanged()
    }

    fun swapOrder() {
        when (sortBy) {
            SortBy.NAME -> {
                if (ascending) {
                    itemsSorted = itemsList.sortedByDescending { it.name }
                    itemsFilterResults = itemsFilterResults.sortedByDescending { it.name }
                } else {
                    itemsSorted = itemsList.sortedBy { it.name }
                    itemsFilterResults = itemsFilterResults.sortedBy { it.name }
                }
            }
            SortBy.CODE -> {
                if (ascending) {
                    itemsSorted = itemsList.sortedByDescending { it.code }
                    itemsFilterResults = itemsFilterResults.sortedByDescending { it.code }
                } else {
                    itemsSorted = itemsList.sortedBy { it.code }
                    itemsFilterResults = itemsFilterResults.sortedBy { it.code }
                }
            }
            SortBy.COST -> {
                if (ascending) {
                    itemsSorted = itemsList.sortedByDescending { it.cost }
                    itemsFilterResults = itemsFilterResults.sortedByDescending { it.cost }
                } else {
                    itemsSorted = itemsList.sortedBy { it.cost }
                    itemsFilterResults = itemsFilterResults.sortedBy { it.cost }
                }
            }
            SortBy.PRICE -> {
                if (ascending) {
                    itemsSorted = itemsList.sortedByDescending { it.price }
                    itemsFilterResults = itemsFilterResults.sortedByDescending { it.price }
                } else {
                    itemsSorted = itemsList.sortedBy { it.price }
                    itemsFilterResults = itemsFilterResults.sortedBy { it.price }
                }
            }
            SortBy.STOCK -> {
                if (ascending) {
                    itemsSorted = itemsList.sortedBy { it.quantity }
                    itemsFilterResults = itemsFilterResults.sortedBy { it.quantity }
                } else {
                    itemsSorted = itemsList.sortedByDescending { it.quantity }
                    itemsFilterResults = itemsFilterResults.sortedByDescending { it.quantity }
                }
            }
            SortBy.PROFIT -> {
                if (ascending) {
                    itemsSorted =
                        itemsList.sortedByDescending { UtilTools.getProfit(it.cost, it.price) }
                    itemsFilterResults = itemsFilterResults.sortedByDescending {
                        UtilTools.getProfit(
                            it.cost,
                            it.price
                        )
                    }
                } else {
                    itemsSorted = itemsList.sortedBy { UtilTools.getProfit(it.cost, it.price) }
                    itemsFilterResults =
                        itemsFilterResults.sortedBy { UtilTools.getProfit(it.cost, it.price) }
                }
            }

        }
        notifyDataSetChanged()
        ascending = ascending.not()
    }

}
