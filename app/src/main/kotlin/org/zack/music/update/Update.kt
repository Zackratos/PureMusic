package org.zack.music.update

import android.os.Parcel
import android.os.Parcelable

/**
 * @author : Zhangwenchao
 * @e-mail : zhangwch@yidianling.com
 * @time   : 2018/5/31
 */
data class Update(val check: Check) {

    data class Check(
            val version: String,
            val fileName: String,
            val versionCode: Int,
            val url: String,
            val updateContent: String) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                parcel.readInt(),
                parcel.readString(),
                parcel.readString()) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(version)
            parcel.writeString(fileName)
            parcel.writeInt(versionCode)
            parcel.writeString(url)
            parcel.writeString(updateContent)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Check> {
            override fun createFromParcel(parcel: Parcel): Check {
                return Check(parcel)
            }

            override fun newArray(size: Int): Array<Check?> {
                return arrayOfNulls(size)
            }
        }
    }

}