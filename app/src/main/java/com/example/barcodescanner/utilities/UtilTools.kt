package com.example.barcodescanner.utilities

import android.app.Activity
import android.content.Context
import android.hardware.Camera
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.google.android.gms.vision.CameraSource
import java.lang.reflect.Field

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
            (activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(150)
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