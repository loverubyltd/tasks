package org.tasks.location

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.huawei.hms.maps.*
import com.huawei.hms.maps.HuaweiMap.OnMarkerClickListener
import com.huawei.hms.maps.model.*
import org.tasks.R
import org.tasks.data.Place
import org.tasks.location.MapFragment.MapFragmentCallback


class HuaweiMapFragment(private val context: Context) : MapFragment, OnMapReadyCallback,
    OnMarkerClickListener {
    private lateinit var callbacks: MapFragmentCallback
    private var dark = false
    private var map: HuaweiMap? = null
    private val markers: MutableList<Marker> = arrayListOf()

    override fun init(
        fragmentManager: FragmentManager,
        callbacks: MapFragmentCallback,
        dark: Boolean
    ) {
        this.callbacks = callbacks
        this.dark = dark

        var mapFragment = fragmentManager.findFragmentByTag(FRAG_TAG_MAP) as SupportMapFragment?
        if (mapFragment == null) {
            mapFragment = SupportMapFragment()
            fragmentManager.commit { replace(R.id.map, mapFragment) }
        }
        mapFragment.getMapAsync(this)
    }

    override fun getMapPosition(): MapPosition? {
        if (map == null) {
            return null
        }
        val cameraPosition = map!!.cameraPosition
        val target = cameraPosition.target
        return MapPosition(target.latitude, target.longitude, cameraPosition.zoom)
    }

    override fun movePosition(mapPosition: MapPosition, animate: Boolean) {
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder()
                .target(LatLng(mapPosition.latitude, mapPosition.longitude))
                .zoom(mapPosition.zoom)
                .build()
        )
        if (animate) {
            map?.animateCamera(cameraUpdate)
        } else {
            map?.moveCamera(cameraUpdate)
        }
    }

    override fun setMarkers(places: List<Place>) {
        if (map == null) {
            return
        }
        for (marker in markers) {
            marker.remove()
        }
        markers.clear()
        for (place in places) {
            val marker = map!!.addMarker(
                MarkerOptions().position(LatLng(place.latitude, place.longitude))
            )
            marker.tag = place
            markers.add(marker)
        }
    }

    override fun disableGestures() {
        map?.run { uiSettings.setAllGesturesEnabled(false) }
    }

    @SuppressLint("MissingPermission")
    override fun showMyLocation() {
        map?.isMyLocationEnabled = true
    }

    override fun onMapReady(huaweiMap: HuaweiMap) {
        map = huaweiMap.apply {
            uiSettings.apply {
                isMyLocationButtonEnabled = false
                isRotateGesturesEnabled = false
            }
            setOnMarkerClickListener(this@HuaweiMapFragment)
            if (dark) {
                setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.mapstyle_night))
            }
        }
    }

    override fun getMarkerId(): Int = R.id.huawei_marker

    override fun onMarkerClick(marker: Marker): Boolean {
        callbacks.onPlaceSelected(marker.tag as Place?)
        return false
    }

    companion object {
        private const val FRAG_TAG_MAP = "frag_tag_map"
    }
}