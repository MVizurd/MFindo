package com.vizurd.mfindo.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.vizurd.mfindo.R


@SuppressLint("MissingPermission")
class DashBoardActivity : FragmentActivity(),
        OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val PERMISSION_ALL = 0
    private var mLastKnownLocation: Location? = null
    private val DEFAULT_ZOOM = 15f
    private lateinit var mapFragment: SupportMapFragment

    private lateinit var mFusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        positionLocationButton()
        with(mMap) {
            isMyLocationEnabled = true
        }
        if (checkPermissions()) {
            initializeMap()
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

    public override fun onPause() {
        super.onPause()
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
    }


    @SuppressLint("MissingPermission")
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

    private fun checkPermissions(): Boolean {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        return if (!hasPermissions(this, *permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_ALL)
            false
        } else {
            true
        }
    }

    private fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ALL) {
            if (permissions.size == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeMap()
            } else {
                Toast.makeText(this@DashBoardActivity, "Please enable permission to capture data", Toast.LENGTH_LONG).show()
            }
        }
    }
}
