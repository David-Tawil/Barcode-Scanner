package com.example.barcodescanner.settings

import android.content.Context
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import com.example.barcodescanner.R

/** Utility class to retrieve shared preferences.  */
object PreferenceUtil {

    fun isSoundEnabled(context: Context): Boolean =
        getBooleanPref(context, R.string.pref_key_enable_sound, true)

    fun isVibrationEnabled(context: Context): Boolean =
        getBooleanPref(context, R.string.pref_key_enable_vibration, true)

    fun isCostEnabled(context: Context): Boolean =
        getBooleanPref(context, R.string.pref_key_enable_cost, false)


    private fun getBooleanPref(
        context: Context,
        @StringRes prefKeyId: Int,
        defaultValue: Boolean
    ): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(context.getString(prefKeyId), defaultValue)


}