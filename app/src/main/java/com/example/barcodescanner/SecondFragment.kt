package com.example.barcodescanner

import AppPermission
import android.app.AlertDialog
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.util.isNotEmpty
import androidx.fragment.app.Fragment
import com.example.barcodescanner.data.Item
import com.example.barcodescanner.data.ItemDatabase
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import handlePermission
import handlePermissionsResult
import kotlinx.android.synthetic.main.fragment_second.*
import requestPermission

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private lateinit var cameraSource : CameraSource
    private lateinit var detector : BarcodeDetector

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handlePermission(AppPermission.CAMERA,
            onGranted = {
                setUpDetector()
            },
            onDenied = {
                requestPermission(AppPermission.CAMERA)
            },
            onRationaleNeeded = {

            }
        )
    }

    private fun setUpDetector() {
        detector = BarcodeDetector.Builder(requireActivity()).setBarcodeFormats( FirebaseVisionBarcode.FORMAT_ALL_FORMATS).build()
        cameraSource = CameraSource.Builder(requireActivity(),detector)
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
        override fun release() {
        }

        override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
            if(detections!= null && detections.detectedItems.isNotEmpty()) {
                val barcodes: SparseArray<Barcode> = detections.detectedItems
                val barcode = barcodes.valueAt(0)

                val database = ItemDatabase.getInstance(requireContext()).itemDatabaseDao
                val item: Item? = database.getBarcode2(barcode.rawValue) ?: database.getBarcode1(barcode.rawValue)

                val text = if(item!=null){
                    """
                    קוד פריט: ${item.code}
                    ${item.name}
                    ספק: ${item.vendor}
                    מחיר: ${item.price}
                    """.trimIndent()
                } else
                    "פריט לא נמצא"

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


    private fun createDialog(msg : String){
        val builder = AlertDialog.Builder(context)
        builder.setMessage(msg)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss()  }
            .create().show()
    }
    companion object{
        const val TAG = "SecondFragment"
    }
}