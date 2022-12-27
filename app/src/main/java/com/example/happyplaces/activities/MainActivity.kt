package com.example.happyplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.database.DatabaseHelper
import com.example.happyplaces.databinding.ActivityMainBinding
import com.example.happyplaces.modals.HapyPlaceModal
import com.happyplaces.adapters.HappyPlacesAdapter
import com.happyplaces.utils.SwipeToDeleteCallback
import com.happyplaces.utils.SwipeToEditCallback
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.log

class MainActivity : AppCompatActivity() {

    private var binding :ActivityMainBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.fabAddHappyPlace?.setOnClickListener{
            val intent=Intent(this, AddHapyPlacesActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)

        }
        getHappyPlacesListFromLocalDB()
    }





    private fun setupHappyPlacesRecyclerView(happyPlacesList: ArrayList<HapyPlaceModal>) {

        rv_happy_places_list.layoutManager = LinearLayoutManager(this)
        rv_happy_places_list.setHasFixedSize(true)

        val placesAdapter = HappyPlacesAdapter(this, happyPlacesList)
        rv_happy_places_list.adapter = placesAdapter

        placesAdapter.setOnClickListener(object :HappyPlacesAdapter.OnClickListener{
            override fun onClick(position: Int, modal: HapyPlaceModal) {

                val intent=Intent(this@MainActivity,HappyPlaceDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS,modal)
                startActivity(intent)
            }
        })
val editSwieHandler = object : SwipeToEditCallback(this)
{
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
val adapter=rv_happy_places_list.adapter as HappyPlacesAdapter
        adapter.notifyEditItems(this@MainActivity,viewHolder.adapterPosition,
            ADD_PLACE_ACTIVITY_REQUEST_CODE)
 }
}
        val editItemTouchHelper = ItemTouchHelper(editSwieHandler)
        editItemTouchHelper.attachToRecyclerView(rv_happy_places_list)
        val deleteSwieHandler = object : SwipeToDeleteCallback(this)
        {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter=rv_happy_places_list.adapter as HappyPlacesAdapter

                adapter.removeAt(viewHolder.adapterPosition
                    )
                getHappyPlacesListFromLocalDB()
            }
        }


        val deleteItemTouchHelper = ItemTouchHelper(deleteSwieHandler)
        deleteItemTouchHelper.attachToRecyclerView(rv_happy_places_list)


    }

    override fun onActivityResult(requestCode : Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // check if the request code is same as what is passed  here it is 'ADD_PLACE_ACTIVITY_REQUEST_CODE'
        if (requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                getHappyPlacesListFromLocalDB()
            } else {
                Log.e("Activity", "Cancelled or Back Pressed")
            }
        }
    }

    /**
     * A function to get the list of happy place from local database.
     */
    private fun getHappyPlacesListFromLocalDB() {

        val dbHandler = DatabaseHelper(this)

        val getHappyPlacesList = dbHandler.getHappyPlacesList()

        if (getHappyPlacesList.size > 0) {
            rv_happy_places_list.visibility = View.VISIBLE
            tv_no_records_available.visibility = View.GONE
            setupHappyPlacesRecyclerView(getHappyPlacesList)
        } else {
            rv_happy_places_list.visibility = View.GONE
            tv_no_records_available.visibility = View.VISIBLE
        }
    }
    companion object {
        private const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        internal const val EXTRA_PLACE_DETAILS = "extra_place_details"
    }
}


