package com.vizurd.mfindo.dashboard

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.vizurd.mfindo.ContainerActivity
import com.vizurd.mfindo.R


@SuppressLint("MissingPermission")
class DashBoardFragment : Fragment(),
        OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val PERMISSION_ALL = 0
    private var mLastKnownLocation: Location? = null
    private val DEFAULT_ZOOM = 15f
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        mapFragment = childFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)

        mapFragment.getMapAsync(this)
        return view
    }


    override fun onMapReady(map: GoogleMap) {
        mMap = map
        positionLocationButton()
        if ((activity as ContainerActivity).checkPermissions()) {
            initializeMap()
            with(mMap) {
                isMyLocationEnabled = true
            }
        }
    }

    private fun positionLocationButton() {
        val mapView = mapFragment.view
        val locationButton = (mapView?.findViewById<View>(Integer.parseInt("1"))?.parent as View).findViewById<View>(Integer.parseInt("2"))
        val rlp = locationButton.layoutParams as (RelativeLayout.LayoutParams)
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        rlp.setMargins(0, 0, 30, 30)
    }

    override fun onPause() {
        super.onPause()
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
    }

    private fun initializeMap() {
        val locationRequest = LocationRequest()
                .apply {
                    interval = 120000
                    fastestInterval = 120000
                    priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                }
        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper())
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.lastLocation?.let {
                mLastKnownLocation = it
                moveCameraToLocation(it)
            }
        }
    }

    private fun moveCameraToLocation(location: Location) {
        // Set the map's camera position to the current location of the device.
        with(mMap) {
            moveCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(location.latitude,
                            location.longitude), DEFAULT_ZOOM))
            addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)))
        }

    }
}