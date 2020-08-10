package com.example.barcodescanner

import AppPermission
import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.util.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.barcodescanner.data.Item
import com.example.barcodescanner.data.ItemDatabase
import com.example.barcodescanner.data.ItemDatabaseDao
import com.example.barcodescanner.settings.PreferenceUtil
import com.example.barcodescanner.utilities.UtilTools
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import handlePermission
import handlePermissionsResult
import kotlinx.android.synthetic.main.fragment_scanner.*
import kotlinx.android.synthetic.main.top_action_bar.*
import requestPermission

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ScannerFragment : Fragment(), View.OnClickListener {

    private lateinit var cameraSource: CameraSource
    private lateinit var detector: BarcodeDetector
    private lateinit var database: ItemDatabaseDao

    companion object {
        const val TAG = "CameraScannerFragment"
        const val VAT = 1.17
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = ItemDatabase.getInstance(requireContext()).itemDatabaseDao
        // Request camera permissions
        handlePermission(
            AppPermission.CAMERA,
            onGranted = {
                setUpDetector()
            },
            onDenied = {
                requestPermission(AppPermission.CAMERA)
            },
            onRationaleNeeded = {

            }
        )
        flash_button.setOnClickListener(this)
        close_button.setOnClickListener(this)
    }

    private fun setUpDetector() {
        detector = BarcodeDetector.Builder(requireActivity()).setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()
        cameraSource = CameraSource.Builder(requireActivity(), detector)
            .setAutoFocusEnabled(true).build()
        cameraSurfaceView.holder.addCallback(surfaceCallback)
        detector.setProcessor(processor)
    }
    // callback for camera surface
    private val  surfaceCallback = object : SurfaceHolder.Callback{
        override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

        }

        override fun surfaceDestroyed(holder: SurfaceHolder?) {
            cameraSource.stop()
        }

        override fun surfaceCreated(holder: SurfaceHolder?) {
            try{
                cameraSource.start(holder)
            }catch (exception : SecurityException){
                Toast.makeText(context,"something went wrong...",Toast.LENGTH_LONG).show()
            }
        }
    }
    // processor for barcode detector
    private val processor = object : Detector.Processor<Barcode>{
        private var lastBarcode: String = ""
        override fun release() {
        }
        override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
            if (detections != null && detections.detectedItems.isNotEmpty()) {
                val barcodes: SparseArray<Barcode> = detections.detectedItems
                val barcode = barcodes.valueAt(0)
                if (lastBarcode == barcode.rawValue) return // same barcode - ignore
                lastBarcode = barcode.rawValue
                val rawValue: String =
                    if (barcode.rawValue.startsWith("]C1")) barcode.rawValue.substring(3) else barcode.rawValue

                if (PreferenceUtil.isVibrationEnabled(requireContext()))
                    UtilTools.vibrate(activity)
                if (PreferenceUtil.isSoundEnabled(requireContext()))
                    MediaPlayer.create(requireContext(), R.raw.barcode_beep).start()

                val itemList: List<Item> = if (database.getBarcode2(rawValue).isEmpty()) {
                    database.getBarcode1(rawValue)
                } else
                    database.getBarcode2(rawValue)

                val item: Item? = if (itemList.isNotEmpty()) itemList[0] else null


                var profit: Double = 0.0
                if (item != null) {
                    if (item.cost != null && item.price != null)
                        profit = (item.price - item.cost * VAT) / item.price
                }


                val text = if (item != null) {
                    """
                    קוד פריט: ${item.code}
                    ${item.name}
                    ספק: ${item.vendor}
                    עלות: ${item.cost}₪ מכירה: ${item.price}₪ 
                    רווח: ${String.format("%.2f", profit * 100).toDouble()}%
                    מלאי חנות: ${itemList.find { it.storage == 1 }?.quantity ?: 0}
                    """.trimIndent()
                } else
                    """
                        $rawValue
                        פריט לא נמצא
                    """.trimIndent()

                textview_second.text = text
            }
        }
    }


    // take care of camera permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    )
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        handlePermissionsResult(requestCode, permissions, grantResults,
            onPermissionGranted = {
                setUpDetector()
                createDialog("חזור אחורה ושוב לחץ על סרוק(רק בפעם הראשונה...)")
            },
            onPermissionDenied = {
                createDialog("נדרשת גישה למצלמה כדי לסרוק ברקודים")
            },
            onPermissionDeniedPermanently = {
                createDialog("ללא מצלמה לא נוכל לסרוק ברקודים...")
            }
        )
    }


    private fun createDialog(msg: String) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(msg)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create().show()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.close_button -> findNavController().navigateUp()
            R.id.flash_button -> {
                flash_button.let {
                    if (it.isSelected) {
                        it.isSelected = false
                        UtilTools.turnOffFlashLight(cameraSource)
                    } else {
                        it.isSelected = true
                        UtilTools.turnOnFlashLight(cameraSource)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        flash_button.isSelected = false
    }


}