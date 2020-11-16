package org.tasks.location

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.SupportMapFragment
import com.huawei.hms.maps.model.CameraPosition
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.MapStyleOptions
import com.huawei.hms.maps.model.Marker
import com.huawei.hms.maps.model.MarkerOptions
import org.tasks.R
import org.tasks.data.Place
import org.tasks.location.MapFragment.MapFragmentCallback
import java.util.*

class HuaweiMapFragment(private val context: Context) : MapFragment,
    OnMapReadyCallback, HuaweiMap.OnMarkerClickListener {
    private val markers: MutableList<Marker> = ArrayList()
    private lateinit var callbacks: MapFragmentCallback
    private var dark = false
    private var map: HuaweiMap? = null

    override fun init(
        fragmentManager: FragmentManager,
        callbacks: MapFragmentCallback,
        dark: Boolean
    ) {
        this.callbacks = callbacks
        this.dark = dark
        var mapFragment =
            fragmentManager.findFragmentByTag(FRAG_TAG_MAP) as SupportMapFragment?
        if (mapFragment == null) {
            mapFragment = SupportMapFragment()
            fragmentManager.commit { replace(R.id.map, mapFragment) }
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
                target.latitude,
                target.longitude,
                cameraPosition.zoom
            )
        }

    override fun movePosition(mapPosition: MapPosition, animate: Boolean) {
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder()
                .target(LatLng(mapPosition.latitude, mapPosition.longitude))
                .zoom(mapPosition.zoom)
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
        for (marker in markers) {
            marker.remove()
        }
        markers.clear()
        for (place in places) {
            val marker = map!!.addMarker(
                MarkerOptions().position(
                    LatLng(
                        place.latitude,
                        place.longitude
                    )
                )
            )
            marker.tag = place
            markers.add(marker)
        }
    }

    override fun disableGestures() {
        map!!.uiSettings.setAllGesturesEnabled(false)
    }

    @SuppressLint("MissingPermission")
    override fun showMyLocation() {
        map!!.isMyLocationEnabled = true
    }

    override fun onMapReady(huaweiMap: HuaweiMap) {
        map = huaweiMap
        if (dark) {
            map!!.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.mapstyle_night
                )
            )
        }
        val uiSettings = map!!.uiSettings
        uiSettings.isMyLocationButtonEnabled = false
        uiSettings.isRotateGesturesEnabled = false
        map!!.setOnMarkerClickListener(this)
        callbacks.onMapReady(this)
    }

    override val markerId: Int
        get() = R.id.huawei_marker

    override fun onMarkerClick(marker: Marker): Boolean {
        callbacks.onPlaceSelected(marker.tag as Place)
        return true
    }

    companion object {
        private const val FRAG_TAG_MAP = "frag_tag_map"
    }
}
