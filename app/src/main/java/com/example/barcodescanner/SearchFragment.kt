package com.example.barcodescanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.barcodescanner.data.ItemDatabase
import com.example.barcodescanner.utilities.UtilTools
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : Fragment(), View.OnClickListener {

    private lateinit var vendors: List<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        vendors = ItemDatabase.getInstance(requireContext()).itemDatabaseDao.getVendorsList()
            .filterNotNull()
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //last time data updated
        val lastTime = requireActivity().getPreferences(AppCompatActivity.MODE_PRIVATE)
            .getLong(getString(R.string.saved_last_updated_time_key), 0)
        textview_updated_Label.text = getString(
            R.string.last_update,
            UtilTools.getReadableRelativeDate(requireContext(), lastTime)
        )


        search_button.setOnClickListener(this)

        val adapter = ArrayAdapter(requireContext(), R.layout.row_list_vendors, vendors)
        autocomplete_vendor.setAdapter(adapter)
        autocomplete_vendor.setOnItemClickListener { _, _, _, _ -> autocomplete_vendor.showDropDown() }

        //text listener to change button text description
        setTextListener()


    }

    private fun setTextListener() {

        search_input_text.addTextChangedListener {
            if (it != null) {
                if (it.isEmpty() && autocomplete_vendor.text.isEmpty()) {
                    search_button.text = resources.getString(R.string.all_items)
                } else {
                    search_button.text = resources.getString(R.string.search)
                }
            }
        }
        autocomplete_vendor.addTextChangedListener {
            if (it != null) {
                if (it.isEmpty() && search_input_text.text?.isEmpty() == true) {
                    search_button.text = resources.getString(R.string.all_items)
                } else {
                    search_button.text = resources.getString(R.string.search)
                }
            }
        }
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.search_button -> {
                UtilTools.hideKeyboard(requireActivity())
                findNavController().navigate(
                    SearchFragmentDirections.actionSearchFragmentToSearchResultFragment(
                        autocomplete_vendor.text.toString(),
                        search_input_text.text.toString(),
                        checkbox_only_in_stock.isChecked
                    )
                )
            }
        }
    }


}