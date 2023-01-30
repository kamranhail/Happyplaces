package com.example.happyplaces.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.happyplaces.R
import com.example.happyplaces.activities.AddHapyPlacesActivity.Companion.PLACE_AUTOCOMPLETE_REQUEST_CODE
import com.example.happyplaces.database.DatabaseHelper
import com.example.happyplaces.databinding.ActivityAddHapyPlacesBinding
import com.example.happyplaces.modals.HapyPlaceModal
import com.example.happyplaces.utils.GetAddressFromLatLng
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
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

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAddHapyPlacesBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setSupportActionBar(binding?.tbAddplaces)

mFusedLocationClient=LocationServices.getFusedLocationProviderClient(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.tbAddplaces?.setNavigationOnClickListener{
            onBackPressed()

        }

        if(!Places.isInitialized())
        {
            Places.initialize(this@AddHapyPlacesActivity,
                resources.getString(R.string.google_mas_api_key))
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
        binding?.etLocation?.setOnClickListener(this)
        binding?.tvSelectCurrentLocation?.setOnClickListener(this)
    }


    /**
     * A function which is used to verify that the location or let's GPS is enable or not of the user's device.
     */
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
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
          R.id.et_location->
          {
              try {
                  val fields  = listOf(
                      Place. Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                      Place. Field.ADDRESS
                  )
                  // Start the autocomplete intent with a unique request code.
                  val intent =
                      Autocomplete. IntentBuilder(AutocompleteActivityMode. FULLSCREEN, fields)
                          .build(this@AddHapyPlacesActivity)
                  startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)


              }catch (e:java.lang.Exception)
              {
                  e.printStackTrace()
              }
          }
          R.id.tv_select_current_location->
          {
              if (!isLocationEnabled()) {
                  Toast.makeText(
                      this,
                      "Your location provider is turned off. Please turn it on.",
                      Toast.LENGTH_SHORT
                  ).show()

                  // This will redirect you to settings from where you need to turn on the location provider.
                  val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                  startActivity(intent)
              } else {

                  Dexter.withActivity(this)
                      .withPermissions(
                          Manifest.permission.ACCESS_FINE_LOCATION,
                          Manifest.permission.ACCESS_COARSE_LOCATION
                      )
                      .withListener(object : MultiplePermissionsListener {
                          override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                              if (report!!.areAllPermissionsGranted()) {

                                  requestNewLocationData()
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

          }

      }


    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            Log.e("kami", " places $requestCode")

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
                        Toast.makeText(this@AddHapyPlacesActivity, "Failed!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

            } else if (requestCode == CAMERA) {

                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap // Bitmap from camera
                saveImageToInternalStorage =
                    saveImageToInternalStorage(thumbnail)
                Log.e("Saved Image : ", "Path :: $saveImageToInternalStorage")
             iv_place_image!!.setImageBitmap(thumbnail) // Set to the imageView.
            } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {

                val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                Log.e("result", " places  $place")
               et_location.setText(place.address)
                mLatitude = place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude

            }else if (resultCode == Activity.RESULT_CANCELED) {
                Log.e("Cancelled", "Cancelled")
            }

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

    /**
     * A function to request the current location. Using the fused location provider client.
     */
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    /**
     * A location callback object of fused location provider client where we will get the current location details.
     */
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation!!
            mLatitude = mLastLocation.latitude
            Log.e("Current Latitude", "$mLatitude")
            mLongitude = mLastLocation.longitude
            Log.e("Current Longitude", "$mLongitude")


            val addressTask =
                GetAddressFromLatLng(this@AddHapyPlacesActivity, mLatitude, mLongitude)

            addressTask.setAddressListener(object :
                GetAddressFromLatLng.AddressListener {
                override fun onAddressFound(address: String?) {
                    Log.e("Address ::", "" + address)
                    et_location.setText(address) // Address is set to the edittext
                }

                override fun onError() {
                    Log.e("Get Address ::", "Something is wrong...")
                }
            })

            addressTask.getAddress()
            // END
        }
    }

   companion object{
       private const val IMAGE_DIRECTORY = "HappyPlacesImages"
       private  const val GALLERY= 1
       private  const val CAMERA= 2
       private const val PLACE_AUTOCOMPLETE_REQUEST_CODE= 3
   }

}