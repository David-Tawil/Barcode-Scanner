package com.example.barcodescanner.utilities

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.format.DateUtils
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.*

object UtilTools {

    internal fun requestRuntimePermissions(activity: Activity) {

        val allNeededPermissions = getRequiredPermissions(activity).filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (allNeededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity, allNeededPermissions.toTypedArray(), /* requestCode= */ 0
            )
        }
    }

    internal fun allPermissionsGranted(context: Context): Boolean = getRequiredPermissions(context)
        .all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

    private fun getRequiredPermissions(context: Context): Array<String> {
        return try {
            val info = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_PERMISSIONS
            )
            val ps = info.requestedPermissions
            if (ps != null && ps.isNotEmpty()) ps else arrayOf()
        } catch (e: Exception) {
            arrayOf()
        }
    }


    fun vibrate(activity: Activity?) {
        @Suppress("DEPRECATION")
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                (activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                (activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(
                    VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            else ->
                (activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(300)
        }
    }

    @Suppress("DEPRECATION")
    fun turnOnFlashLight(mCameraSource: CameraSource) {
        val camera = getCamera(mCameraSource)
        if (camera != null) {
            try {
                val param: Camera.Parameters = camera.parameters
                param.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                camera.parameters = param

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Suppress("DEPRECATION")
    fun turnOffFlashLight(mCameraSource: CameraSource) {
        val camera = getCamera(mCameraSource)
        if (camera != null) {
            try {
                val param: Camera.Parameters = camera.parameters
                param.flashMode = Camera.Parameters.FLASH_MODE_OFF
                camera.parameters = param
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun getCamera(cameraSource: CameraSource): Camera? {
        val declaredFields: Array<Field> = CameraSource::class.java.declaredFields
        for (field in declaredFields) {
            if (field.type === Camera::class.java) {
                field.isAccessible = true
                try {
                    return field.get(cameraSource) as Camera?
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
                break
            }
        }
        return null
    }

    fun getProfit(cost: Double?, price: Double?): Double {
        return if (cost != null && price != null)
            (price - cost * VAT) / price
        else
            0.0

    }

    fun hideKeyboard(activity: Activity) {
        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // Check if no view has focus
        val currentFocusedView = activity.currentFocus
        currentFocusedView?.let {
            inputMethodManager.hideSoftInputFromWindow(
                currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    fun getReadableRelativeDate(context: Context, millis: Long): String {

        var str = ""

        str += when {
            DateUtils.isToday(millis) -> {
                "היום "
            }
            DateUtils.isToday(millis + DateUtils.DAY_IN_MILLIS) -> {
                "אתמול "
            }
            Date(Calendar.getInstance().timeInMillis).before(Date(millis + DateUtils.DAY_IN_MILLIS * 7)) -> {
                DateUtils.formatDateTime(context, millis, DateUtils.FORMAT_SHOW_WEEKDAY)
            }
            else -> {
                // val flags = DateUtils.FORMAT_NO_YEAR or DateUtils.FORMAT_NUMERIC_DATE
                //DateUtils.formatDateTime(context,millis,flags)
                SimpleDateFormat("MM/dd", Locale.getDefault()).format(millis)
            }
        }
        str += " ${DateUtils.formatDateTime(context, millis, DateUtils.FORMAT_SHOW_TIME)}"
        return str
    }
    //SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(newTime)

    fun Context.copyToClipboard(text: CharSequence) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
    }
}