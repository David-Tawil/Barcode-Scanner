package com.example.barcodescanner

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
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
import com.example.barcodescanner.data.ItemDatabaseDao
import com.example.barcodescanner.settings.PreferenceUtil
import com.example.barcodescanner.utilities.TAG
import com.example.barcodescanner.utilities.UtilTools
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import handlePermissionsResult
import kotlinx.android.synthetic.main.fragment_scanner.*
import kotlinx.android.synthetic.main.scanner_action_bar.*
import java.text.DecimalFormat

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ScannerFragment : Fragment(), View.OnClickListener {

    private lateinit var cameraSource: CameraSource
    private lateinit var detector: BarcodeDetector
    private lateinit var database: ItemDatabaseDao
    private lateinit var item: Item


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

        // item = Item(null,"error",null,null,null,null,null,null,null,null)

        /* handlePermission(
             AppPermission.CAMERA,
             onGranted = {
                 setUpDetector()
             },
             onDenied = {
                 requestPermission(AppPermission.CAMERA)
             },
             onRationaleNeeded = {

             }
         )*/
         while (!UtilTools.allPermissionsGranted(requireContext())) {
             UtilTools.requestRuntimePermissions(requireActivity())
         }
        setUpDetector()
        camera_surface_view.visibility = View.VISIBLE

        button_details.setOnClickListener(this)
        flash_button.setOnClickListener(this)
    }

    private fun setUpDetector() {
        detector = BarcodeDetector.Builder(requireActivity()).setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()
        cameraSource = CameraSource.Builder(requireActivity(), detector)
            .setAutoFocusEnabled(true).build()
        camera_surface_view.holder.addCallback(surfaceCallback)
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
                    if (barcode.rawValue.startsWith("]C1")) barcode.rawValue.substring(3) else barcode.rawValue.trimStart(
                        '0'
                    )

                if (PreferenceUtil.isVibrationEnabled(requireContext()))
                    UtilTools.vibrate(activity)
                if (PreferenceUtil.isSoundEnabled(requireContext()))
                    MediaPlayer.create(requireContext(), R.raw.barcode_beep).start()

                val itemList: List<Item> = if (database.getItemBarcode2(rawValue).isEmpty()) {
                    database.getItemBarcode1(rawValue)
                } else
                    database.getItemBarcode2(rawValue)

                val item: Item? = if (itemList.isEmpty()) null else itemList[0]

                setInfoUi(item, rawValue)
            }
        }
    }

    private fun setInfoUi(item: Item?, rawValue: String) {
        if (item == null) {
            requireActivity().runOnUiThread {
                code_scanner_item_info.text = rawValue
                divider_code_name.visibility = View.VISIBLE
                name_scanner_item_info.text = resources.getString(R.string.item_not_found)
                divider_name_price.visibility = View.GONE
                price_scanner_item_info.text = ""
                inventory_scanner_item_info.visibility = View.GONE
                button_details.visibility = View.GONE
            }
            return
        }
        this@ScannerFragment.item = item
        requireActivity().runOnUiThread {
            code_scanner_item_info.text = rawValue
            divider_code_name.visibility = View.VISIBLE
            name_scanner_item_info.text = item.name
            Log.d(TAG, "${name_scanner_item_info.text}")
            divider_name_price.visibility = View.VISIBLE
            price_scanner_item_info.text = resources.getString(
                R.string.price_item_scanner_info,
                DecimalFormat("0.00").format(item.price)
            )
            inventory_scanner_item_info.text =
                resources.getString(R.string.inventory_info, database.getInventory(item.code, 1))
            inventory_scanner_item_info.visibility = View.VISIBLE
            button_details.visibility = View.VISIBLE
        }
    }


    // take care of camera permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        handlePermissionsResult(
            requestCode, permissions, grantResults,
            onPermissionGranted = {
                setUpDetector()
                camera_surface_view.visibility = View.VISIBLE
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
            //  R.id.close_button -> findNavController().navigateUp()
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
            R.id.button_details -> {
                Log.d(TAG, "button clicked")
                Log.d(TAG, item.name ?: "no name")
                ScannerBottomSheet(item)
                    .show(parentFragmentManager, TAG)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        flash_button.isSelected = false
    }
}