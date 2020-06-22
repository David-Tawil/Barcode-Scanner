package com.example.barcodescanner.viewModel

import android.app.Activity
import android.content.Context
import androidx.core.content.edit
import com.example.barcodescanner.R
import com.example.barcodescanner.data.ItemDatabase
import com.example.barcodescanner.data.ItemDatabaseDao
import com.example.barcodescanner.utilities.ITEM_DATA_FILENAME
import com.example.barcodescanner.utilities.ItemsXmlParser
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class ItemsModel {

    fun pullDataToDB(activity : Activity){
        val itemDatabase : ItemDatabaseDao= ItemDatabase.getInstance(activity.applicationContext).itemDatabaseDao

        val xmlRef = Firebase.storage.getReference(ITEM_DATA_FILENAME)

        val destinationFile = File(activity.applicationContext.filesDir.toString() + "/" + "fileData.xml")

        val lastTime = activity.getPreferences(Context.MODE_PRIVATE).getLong(activity.applicationContext.getString(R.string.saved_last_updated_time_key),0)
        xmlRef.metadata.addOnSuccessListener {
            val newTime = it.updatedTimeMillis
            if(newTime > lastTime ){
                xmlRef.getFile(destinationFile).addOnSuccessListener {
                    val items = ItemsXmlParser().parse(destinationFile.inputStream())
                    itemDatabase.clear()
                    itemDatabase.insertAll(items)
                    activity.getPreferences(Context.MODE_PRIVATE).edit {
                        putLong(activity.applicationContext.getString(R.string.saved_last_updated_time_key),newTime)
                        putString(activity.applicationContext.getString(R.string.saved_updated_date_label_key),
                            "עדכון אחרון ${SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(newTime)}")
                        apply()
                    }

                }.addOnFailureListener{ exception ->
                    exception.printStackTrace()
                }
            }
        }.addOnFailureListener {
            it.printStackTrace()
        }
    }
    companion object{
        const val TAG = "ItemsModel"
    }
}