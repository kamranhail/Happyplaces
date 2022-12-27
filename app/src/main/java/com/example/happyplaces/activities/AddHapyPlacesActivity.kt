package com.example.happyplaces.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.happyplaces.R
import com.example.happyplaces.database.DatabaseHelper
import com.example.happyplaces.databinding.ActivityAddHapyPlacesBinding
import com.example.happyplaces.modals.HapyPlaceModal
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_add_hapy_places.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*



class AddHapyPlacesActivity : AppCompatActivity(), View.OnClickListener
{

    private var cal= Calendar.getInstance()
    lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    private var binding : ActivityAddHapyPlacesBinding?=null
    private var saveImageToInternalStorage: Uri? = null

    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var mHappyPlaceDetails:HapyPlaceModal?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAddHapyPlacesBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setSupportActionBar(binding?.tbAddplaces)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.tbAddplaces?.setNavigationOnClickListener{
            onBackPressed()

        }
if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS))
{
    mHappyPlaceDetails=intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HapyPlaceModal?

}

        dateSetListener=DatePickerDialog.OnDateSetListener{
            view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDateInView()
 }

        if (mHappyPlaceDetails != null) {
            supportActionBar?.title = "Edit Happy Place"

            et_title.setText(mHappyPlaceDetails!!.title)
            et_description.setText(mHappyPlaceDetails!!.description)
            et_date.setText(mHappyPlaceDetails!!.data)
            et_location.setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitdue
            mLongitude = mHappyPlaceDetails!!.longitude

            saveImageToInternalStorage = Uri.parse(mHappyPlaceDetails!!.image)

            iv_place_image.setImageURI(saveImageToInternalStorage)

            btn_save.text = "UPDATE"
        }
        binding?.etDate?.setOnClickListener (this)
        binding?.tvAddImage?.setOnClickListener(this)
        binding?.btnSave?.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
    //    Toast.makeText(this,"$v.id",Toast.LENGTH_LONG).show()
      when(v!!.id)
      {

          R.id.et_date ->
          {
              DatePickerDialog(this@AddHapyPlacesActivity,dateSetListener,cal.get(Calendar.YEAR),
                  cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()


          }
          R.id.tv_add_image ->
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
          R.id.btn_save->
          {// save data to
              when {
                  et_title.text.isNullOrEmpty() -> {
                      Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
                  }
                  et_description.text.isNullOrEmpty() -> {
                      Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT)
                          .show()
                  }
                  et_location.text.isNullOrEmpty() -> {
                      Toast.makeText(this, "Please select location", Toast.LENGTH_SHORT)
                          .show()
                  }
                  saveImageToInternalStorage == null -> {
                      Toast.makeText(this, "Please add image", Toast.LENGTH_SHORT).show()
                  }
                  else -> {

                      // Assigning all the values to data model class.
                      // use id to update updatedate happyplace details
                      val happyPlaceModel = HapyPlaceModal(
                          if(mHappyPlaceDetails==null) 0 else mHappyPlaceDetails!!.id,
                          et_title.text.toString(),
                          saveImageToInternalStorage.toString(),
                          et_description.text.toString(),
                          et_date.text.toString(),
                          et_location.text.toString(),
                          mLatitude,
                          mLongitude
                      )

                      // Here we initialize the database handler class.
                      val dbHandler = DatabaseHelper(this)

                      if(mHappyPlaceDetails==null){
                          val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)

                          if (addHappyPlace > 0) {
                              Toast.makeText(
                                  this,
                                  "The happy place details are inserted successfully.",
                                  Toast.LENGTH_SHORT
                              ).show()
                              // first start activity for result then set result
                              setResult(Activity.RESULT_OK)
                              finish();//finishing activity
                          }
                      }
                      else
                      {
                          val updateplace = dbHandler.updateHappyPlace(happyPlaceModel)

                          if (updateplace > 0) {
                              Toast.makeText(
                                  this,
                                  "The happy place details are inserted successfully.",
                                  Toast.LENGTH_SHORT
                              ).show()
                              // first start activity for result then set result
                              setResult(Activity.RESULT_OK)
                              finish();//finishing activity
                          }
                      }

                  }
          }


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

                        saveImageToInternalStorage =
                            saveImageToInternalStorage(selectedImageBitmap)
                        Log.e("Saved Image : ", "Path :: $saveImageToInternalStorage")
                        iv_place_image!!.setImageBitmap(selectedImageBitmap) // Set the selected image from GALLERY to imageView.
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@AddHapyPlacesActivity, "Failed!", Toast.LENGTH_SHORT).show()
                    }
                }

            } else if (requestCode == CAMERA) {

                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap // Bitmap from camera
                saveImageToInternalStorage =
                    saveImageToInternalStorage(thumbnail)
                Log.e("Saved Image : ", "Path :: $saveImageToInternalStorage")

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


    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {

        // Get the context wrapper instance
        val wrapper = ContextWrapper(applicationContext)

        // Initializing a new file
        // The bellow line return a directory in internal storage
        /**
         * The Mode Private here is
         * File creation mode: the default mode, where the created file can only
         * be accessed by the calling application (or all applications sharing the
         * same user ID).
         */
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)

        // Create a file to save the image
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(file)

            // Compress bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

            // Flush the stream
            stream.flush()

            // Close stream
            stream.close()
        } catch (e: IOException) { // Catch the exception
            e.printStackTrace()
        }

        // Return the saved image uri
        return Uri.parse(file.absolutePath)
    }

    private fun updateDateInView()
    {

        val myformate= " dd.MM.YYYY"
        val sdf=  SimpleDateFormat(myformate, Locale.getDefault())
        binding?.etDate?.setText(sdf.format(cal.time).toString())
    }

   companion object{
       private const val IMAGE_DIRECTORY = "HappyPlacesImages"
       private  const val GALLERY=1
       private  const val CAMERA=2
   }

}