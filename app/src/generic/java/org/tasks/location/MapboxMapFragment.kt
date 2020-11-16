package org.tasks.location

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.FragmentManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
 import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.maps.SupportMapFragment
import org.tasks.R
import org.tasks.data.Place
import org.tasks.location.MapFragment.MapFragmentCallback
import java.util.*

class MapboxMapFragment(private val context: Context) : MapFragment, OnMapReadyCallback,
    MapboxMap.OnMarkerClickListener {
    private var callbacks: MapFragmentCallback? = null
    private var dark = false
    private var map: MapboxMap? = null
    private val markers: MutableMap<Marker, Place> = hashMapOf()

    override fun init(
        fragmentManager: FragmentManager,
        callbacks: MapFragmentCallback,
        dark: Boolean
    ) {
        this.callbacks = callbacks
        this.dark = dark
        Mapbox.getInstance(context, context.getString(R.string.mapbox_key))

        var mapFragment = fragmentManager.findFragmentByTag(FRAG_TAG_MAP) as SupportMapFragment?
        if (mapFragment == null) {
            mapFragment = SupportMapFragment()
            fragmentManager.beginTransaction().replace(R.id.map, mapFragment).commit()
        }
        mapFragment.getMapAsync(this)
    }

    override val mapPosition: MapPosition?
        get() {
            if (map == null) {
                return null
            }
            val cameraPosition = map!!.cameraPosition
            val target = cameraPosition.target
            return MapPosition(
                target.latitude, target.longitude, cameraPosition.zoom.toFloat()
            )
        }

    override fun movePosition(mapPosition: MapPosition, animate: Boolean) {
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder()
                .target(LatLng(mapPosition.latitude, mapPosition.longitude))
                .zoom(mapPosition.zoom.toDouble())
                .build()
        )
        if (animate) {
            map!!.animateCamera(cameraUpdate)
        } else {
            map!!.moveCamera(cameraUpdate)
        }
    }

    override fun setMarkers(places: List<Place>) {
        if (map == null) {
            return
        }
        for (marker in map!!.markers) {
            map!!.removeMarker(marker!!)
        }
        markers.clear()
        for (place in places) {
            val marker = map!!.addMarker(
                    MarkerOptions().setPosition(LatLng(place.latitude, place.longitude)))
            markers[marker] = place
        }
    }

    override fun disableGestures() {
        map!!.uiSettings.setAllGesturesEnabled(false)
    }

    @SuppressLint("MissingPermission")
    override fun showMyLocation() {
        val locationComponent = map!!.locationComponent
        locationComponent.run {
            activateLocationComponent(context, map!!.style!!)
            isLocationComponentEnabled = true
            cameraMode = CameraMode.NONE
            renderMode = RenderMode.NORMAL
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        map = mapboxMap
        map!!.uiSettings.isRotateGesturesEnabled = false
        map!!.setOnMarkerClickListener(this)
        map!!.setStyle(if (dark) Style.DARK else Style.MAPBOX_STREETS) {
            callbacks!!.onMapReady(this)
        }
    }

    override val markerId: Int
        get() = R.id.mapbox_marker

    override fun onMarkerClick(marker: Marker): Boolean {
        val place = markers[marker]
        callbacks!!.onPlaceSelected(place!!)
        return false
    }

    companion object {
        private const val FRAG_TAG_MAP = "frag_tag_map"
    }
}