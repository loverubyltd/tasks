package org.tasks.location

import android.os.Parcel
import android.os.Parcelable

class MapPosition @JvmOverloads constructor(
    val latitude: Double,
    val longitude: Double,
    val zoom: Float = 15.0f
) : Parcelable {

    private constructor(source: Parcel) : this(
        source.readDouble(),
        source.readDouble(),
        source.readFloat()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeDouble(latitude)
        dest.writeDouble(longitude)
        dest.writeFloat(zoom)
    }

    companion object {
        val CREATOR: Parcelable.Creator<MapPosition> = object : Parcelable.Creator<MapPosition> {
            override fun createFromParcel(source: Parcel): MapPosition = MapPosition(source)

            override fun newArray(size: Int): Array<MapPosition?> = arrayOfNulls(size)
        }
    }
}