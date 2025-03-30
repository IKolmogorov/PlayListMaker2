//package com.example.playlistmaker2
//
//import android.os.Parcel
//import android.os.Parcelable
//
//data class ParcelableTrackArrayList(val trackArrayList: ArrayList<Track>): Parcelable {
//    constructor(parcel: Parcel) : this(
//        source.createTypedArrayList(OrderedService.CREATOR)
//        }
//    ) {
//    }
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeList(trackArrayList)
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<ParcelableTrackArrayList> {
//        override fun createFromParcel(parcel: Parcel): ParcelableTrackArrayList {
//            return ParcelableTrackArrayList(parcel)
//        }
//
//        override fun newArray(size: Int): Array<ParcelableTrackArrayList?> {
//            return arrayOfNulls(size)
//        }
//    }
//
//
//}