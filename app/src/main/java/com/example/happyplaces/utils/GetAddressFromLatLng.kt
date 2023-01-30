package com.example.happyplaces.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import java.util.*

class GetAddressFromLatLng(context:Context,private  val latitude:Double,private  val longitude:Double): AsyncTask<Void,String,String>() {

  private val geocoder:Geocoder=Geocoder(context, Locale.getDefault())
  private  lateinit var  mAddressListener: AddressListener

    override fun doInBackground(vararg params: Void?): String {
       try {
           val addresslist: List<Address>?=geocoder.getFromLocation(latitude,longitude,
               1 )
           if(addresslist!=null&&addresslist.isNotEmpty())
           {
               val address :Address=addresslist[0]
               val sb = StringBuilder()
               for(i in 0..address.maxAddressLineIndex)
               {
                   // add empty spaces
                   sb.append(address.getAddressLine(i)).append(" ")

               }
               sb.deleteCharAt(sb.length-1)
               return sb.toString()
           }
       }catch(e: java.lang.Exception)
       {
           e.printStackTrace()
       }
return ""
    }
fun getAddress()
{
    execute()
}
    override fun onPostExecute(result: String?) {

        if(result==null)
        {
          mAddressListener.onError()
        }else
        {
            mAddressListener.onAddressFound(result)
        }
        super.onPostExecute(result)
    }
    fun setAddressListener(addressListener: AddressListener)
    {
        mAddressListener=addressListener
    }
    interface  AddressListener
    {
        fun onAddressFound(address: String?)
        fun onError()

    }
}