package org.tasks.location

import androidx.fragment.app.FragmentManager
import org.tasks.data.Place

interface MapFragment {
    fun init(fragmentManager: FragmentManager, callbacks: MapFragmentCallback, dark: Boolean)
    val mapPosition: MapPosition?
    fun movePosition(mapPosition: MapPosition, animate: Boolean)
    fun setMarkers(places: List<Place>)
    fun disableGestures()
    fun showMyLocation()
    val markerId: Int

    interface MapFragmentCallback {
        fun onMapReady(mapFragment: MapFragment)
        fun onPlaceSelected(place: Place)
    }
}