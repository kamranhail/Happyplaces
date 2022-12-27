package com.example.happyplaces.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happyplaces.R
import com.example.happyplaces.databinding.ActivityHappyPlaceDetailBinding
import com.example.happyplaces.databinding.ActivityMainBinding
import com.example.happyplaces.modals.HapyPlaceModal
import kotlinx.android.synthetic.main.activity_happy_place_detail.*

@Suppress("DEPRECATION")
class HappyPlaceDetailActivity : AppCompatActivity() {
    private var binding : ActivityHappyPlaceDetailBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityHappyPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)
var happyPlaceDetailModal : HapyPlaceModal? =null
if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS))
{
    happyPlaceDetailModal= intent.getParcelableExtra<HapyPlaceModal>(MainActivity.EXTRA_PLACE_DETAILS)!! as HapyPlaceModal

}
        if(happyPlaceDetailModal!=null)
        {
setSupportActionBar(toolbar_happy_place_detail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title=happyPlaceDetailModal.title
           binding!!.toolbarHappyPlaceDetail.setNavigationOnClickListener{
               onBackPressed()


           }
            binding!!. ivPlaceImage.setImageURI(Uri.parse(happyPlaceDetailModal.image))
            binding!!.tvDescription.text = happyPlaceDetailModal.description
            binding!!.tvLocation.text = happyPlaceDetailModal.location
        }

    }
}