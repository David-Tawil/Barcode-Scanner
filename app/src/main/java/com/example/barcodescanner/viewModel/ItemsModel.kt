package com.example.barcodescanner.viewModel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.example.barcodescanner.R
import com.example.barcodescanner.data.ItemDatabase
import com.example.barcodescanner.data.ItemDatabaseDao
import com.example.barcodescanner.utilities.ITEM_DATA_FILENAME
import com.example.barcodescanner.utilities.ItemsXmlParser
import com.example.barcodescanner.utilities.TAG
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.io.File


class ItemsModel {
    private val xmlRef = Firebase.storage.getReference(ITEM_DATA_FILENAME)

    fun pullDataToDB(activity : Activity){
        val itemDatabase : ItemDatabaseDao= ItemDatabase.getInstance(activity.applicationContext).itemDatabaseDao



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
                        apply()
                    }

                }.addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
            }
        }.addOnFailureListener {
            it.printStackTrace()
        }
    }

    suspend fun newDataAvail(activity: Activity): Boolean {
        var dataAvail = false
        val lastTime = activity.getPreferences(Context.MODE_PRIVATE)
            .getLong(activity.applicationContext.getString(R.string.saved_last_updated_time_key), 0)
        val metadata = xmlRef.metadata.await()
        val newTime = metadata.updatedTimeMillis

        if (newTime > lastTime)
            dataAvail = true
        Log.d(TAG, "new data avail $dataAvail")
        return dataAvail
    }

    suspend fun updateData(activity: Activity) {
        val itemDatabase: ItemDatabaseDao =
            ItemDatabase.getInstance(activity.applicationContext).itemDatabaseDao

        val destinationFile =
            File(activity.applicationContext.filesDir.toString() + "/" + "fileData.xml")

        val metadata = xmlRef.metadata.await()
        xmlRef.getFile(destinationFile).await()
        val items1 = ItemsXmlParser().parse(destinationFile.inputStream())
        itemDatabase.clear()
        itemDatabase.insertAll(items1)
        activity.getPreferences(Context.MODE_PRIVATE).edit {
            putLong(
                activity.applicationContext.getString(R.string.saved_last_updated_time_key),
                metadata.updatedTimeMillis
            )
            apply()
        }

        /*xmlRef.metadata.addOnSuccessListener {
            val newTime = it.updatedTimeMillis
            xmlRef.getFile(destinationFile).addOnSuccessListener {
                val items = ItemsXmlParser().parse(destinationFile.inputStream())
                itemDatabase.clear()
                itemDatabase.insertAll(items)
                activity.getPreferences(Context.MODE_PRIVATE).edit {
                    putLong(activity.applicationContext.getString(R.string.saved_last_updated_time_key),newTime)
                    apply()
                }
            }.addOnFailureListener { exception ->
                exception.printStackTrace()
            }
        }.addOnFailureListener {
            it.printStackTrace()
        }*/
    }
}