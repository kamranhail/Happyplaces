package com.example.happyplaces


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.Uri.fromParts

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.happyplaces.databinding.ActivityAddHapyPlacesBinding
import com.example.happyplaces.databinding.ActivityMainBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_add_hapy_places.*
import java.io.IOException
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*



class AddHapyPlacesActivity : AppCompatActivity(), View.OnClickListener
{

    private var cal= Calendar.getInstance()
    lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    private var binding : ActivityAddHapyPlacesBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAddHapyPlacesBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setSupportActionBar(binding?.tbAddplaces)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.tbAddplaces?.setNavigationOnClickListener{
            onBackPressed()

        }

        dateSetListener=DatePickerDialog.OnDateSetListener{
            view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDateInView()
        }
        binding?.etDate?.setOnClickListener (this)
        binding?.tvAddImage?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
    //    Toast.makeText(this,"$v.id",Toast.LENGTH_LONG).show()
      when(v!!.id)
      {

          R.id.et_date->
          {
              DatePickerDialog(this@AddHapyPlacesActivity,dateSetListener,cal.get(Calendar.YEAR),
                  cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()


          }
          R.id.tv_add_image->
          {
             // Toast.makeText(this,"utton clisked",Toast.LENGTH_LONG).show()
              val picturedialog= AlertDialog.Builder(this)
              picturedialog.setTitle("Select Action")
              val pictureDialogItems=arrayOf("Select pic from gallry","Capture photo from camera")
picturedialog.setItems(pictureDialogItems) { dialog, which ->
    when (which) {
        0 -> {

            choosePhotoFromGallery()
        }
        1 -> {
            takePhotoFromCamera()
            Toast.makeText(this, "Camera Selection Comming Soon", Toast.LENGTH_LONG).show()

        }
    }
}
    picturedialog.show()


          }
      }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        // Here this is used to get an bitmap from URI
                        @Suppress("DEPRECATION")
                        val selectedImageBitmap =
                            MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)

                        iv_place_image!!.setImageBitmap(selectedImageBitmap) // Set the selected image from GALLERY to imageView.
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@AddHapyPlacesActivity, "Failed!", Toast.LENGTH_SHORT).show()
                    }
                }
                // TODO (Step 7: Camera result will be received here.)
            } else if (requestCode == CAMERA) {

                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap // Bitmap from camera
                iv_place_image!!.setImageBitmap(thumbnail) // Set to the imageView.
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Cancelled", "Cancelled")
        }
    }
    private fun takePhotoFromCamera() {

        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    // Here after all the permission are granted launch the CAMERA to capture an image.
                    if (report!!.areAllPermissionsGranted()) {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(intent, CAMERA)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread()
            .check()
    }


    private fun choosePhotoFromGallery() {
Dexter.withActivity(this).withPermissions(

    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
).withListener(object : MultiplePermissionsListener{
    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
        report?.let {
            if(report!!.areAllPermissionsGranted()){
                Toast.makeText(this@AddHapyPlacesActivity,"Granted",Toast.LENGTH_LONG).show()
                val galleryIntent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )

                startActivityForResult(galleryIntent, GALLERY)

            }
        }
    }
    override fun onPermissionRationaleShouldBeShown(
        permissions: MutableList<PermissionRequest>?,
        token: PermissionToken?
    ) {

        //showRationalDialogForPermissions()
        AlertDialog.Builder(this@AddHapyPlacesActivity).setMessage("permission" +
                " turned off").setPositiveButton("go to setting")
        {
            _,_ ->
            try{
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package:" ,packageName, null)
                startActivity(intent)
            } catch (e:ActivityNotFoundException){
                e.printStackTrace()
            }
        }.setNegativeButton("cancel"){
            dialog,which ->
            dialog.dismiss()
        }.show()
        // Remember to invoke this method when the custom rationale is closed
        // or just by default if you don't want to use any custom rationale.

    }
}).onSameThread().check()
    }


    private fun showRationalDialogForPermissions() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions required for this feature. It can be enabled under Application Settings")
            .setPositiveButton("GO TO SETTINGS"
            ) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog,
                                           _ ->
                dialog.dismiss()
            }.show()
    }




    private fun updateDateInView()
    {

        val myformate= " dd.MM.YYYY"
        val sdf=  SimpleDateFormat(myformate, Locale.getDefault())
        binding?.etDate?.setText(sdf.format(cal.time).toString())
    }

   companion object{

       private  const val GALLERY=1
       private  const val CAMERA=2
   }

}