package com.example.happyplaces.modals

import android.os.Parcel
import android.os.Parcelable

data class HapyPlaceModal(
    val id : Int,
    val title :String?,
    val image :String?,
    val description : String?,
    val data : String?,
    val location : String?,
    val latitdue: Double,
    val longitude : Double
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(image)
        parcel.writeString(description)
        parcel.writeString(data)
        parcel.writeString(location)
        parcel.writeDouble(latitdue)
        parcel.writeDouble(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HapyPlaceModal> {
        override fun createFromParcel(parcel: Parcel): HapyPlaceModal {
            return HapyPlaceModal(parcel)
        }

        override fun newArray(size: Int): Array<HapyPlaceModal?> {
            return arrayOfNulls(size)
        }
    }
}


