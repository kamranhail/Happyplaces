package com.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.R
import com.example.happyplaces.activities.AddHapyPlacesActivity
import com.example.happyplaces.activities.MainActivity
import com.example.happyplaces.database.DatabaseHelper
import com.example.happyplaces.databinding.ActivityMainBinding
import com.example.happyplaces.modals.HapyPlaceModal

import kotlinx.android.synthetic.main.item_happy_place.view.*

open class HappyPlacesAdapter(
    private val context: Context,
    private var list: ArrayList<HapyPlaceModal>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var onClickListener :OnClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_happy_place,
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            holder.itemView.iv_place_image.setImageURI(Uri.parse(model.image))
            holder.itemView.tvTitle.text = model.title
            holder.itemView.tvDescription.text = model.description
            holder.itemView.setOnClickListener{
                if(onClickListener!=null)
                {
                    onClickListener!!.onClick(position, model )
                }
            }
        }
    }
fun removeAt(position: Int)
{
val dbHandler=DatabaseHelper(context)
    val isDeleted=dbHandler.deleteHappyPlace(list[position])
    if(isDeleted>0)
    {
        list.removeAt(position)
        notifyItemRemoved(position)
    }
}
    fun notifyEditItems(activity : Activity, position: Int, requestCode :Int)
    {
        val intent =Intent(context,AddHapyPlacesActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS,list[position])
        activity.startActivityForResult(intent,requestCode)
        notifyItemChanged(position)
    }

    fun setOnClickListener(onClickListener: OnClickListener)
    {
this.onClickListener=onClickListener
    }


    interface  OnClickListener {
        fun onClick(position :Int, modal :HapyPlaceModal)

    }

    override fun getItemCount(): Int {
        return list.size
    }


    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        // we can also  attach listner to its init block
    }
}