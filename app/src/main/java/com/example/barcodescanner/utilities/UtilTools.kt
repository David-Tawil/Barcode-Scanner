package com.example.barcodescanner.utilities

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

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