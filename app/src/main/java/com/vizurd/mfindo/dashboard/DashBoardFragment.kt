package com.vizurd.mfindo.dashboard

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import androidx.core.graphics.drawable.toBitmap
import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.vizurd.mfindo.ContainerActivity
import com.vizurd.mfindo.R
import com.vizurd.mfindo.commuteList.CommuteListActivity
import com.vizurd.mfindo.core.di.DIHandler
import com.vizurd.mfindo.dashboard.di.DashBoardComponent
import com.vizurd.mfindo.dashboard.models.DashBoardViewModel
import com.vizurd.mfindo.handlers.GeocodeWorkerThread
import kotlinx.android.synthetic.main.fragment_dashboard.*
import java.util.*
import javax.inject.Inject


@SuppressLint("MissingPermission")
class DashBoardFragment : Fragment(),
        OnMapReadyCallback {

    @Inject
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    private lateinit var mMap: GoogleMap
    private var mLastKnownLocation: Location? = null
    private val DEFAULT_ZOOM = 17f
    private val TAG = DashBoardFragment::class.java.canonicalName
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var startPlaceFragment: PlaceAutocompleteFragment
    private lateinit var destPlaceFragment: PlaceAutocompleteFragment
    private val dashBoardComponent: DashBoardComponent by lazy { DIHandler.getDashBoardComponent() }
    private lateinit var destinationLoc: Location
    private lateinit var startLocation: Location
    private lateinit var btnFindCompanion: CircularProgressButton
    private var startPlaceActive: Boolean = true
    private lateinit var geoCodeHandler: GeocodeWorkerThread

    //    TODO: Make use of this to make api calls
    private val viewModel: DashBoardViewModel by lazy { ViewModelProviders.of(this).get(DashBoardViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        dashBoardComponent.inject(this)
        mapFragment = childFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        geoCodeHandler = GeocodeWorkerThread("GeoCodeHandler")
                .apply { start() }
        startPlaceFragment = activity?.fragmentManager!!
                .findFragmentById(R.id.startPlaceFragment) as PlaceAutocompleteFragment
        with(startPlaceFragment) {
            setHint("From where ...")
            setOnPlaceSelectedListener(mPlaceSelectionListener)
            this.view.findViewById<ImageButton>(R.id.place_autocomplete_search_button).setOnClickListener {
                moveCameraToLocation(startLocation)
            }
        }

        destPlaceFragment = activity?.fragmentManager!!
                .findFragmentById(R.id.endPlaceFragment) as PlaceAutocompleteFragment
        with(destPlaceFragment) {
            setHint("Where to ...")
            setOnPlaceSelectedListener(mDestPlaceListner)
            this.view.findViewById<ImageButton>(R.id.place_autocomplete_search_button).setOnClickListener {
                moveCameraToLocation(destinationLoc)
            }

        }
        btnFindCompanion = view.findViewById<CircularProgressButton>(R.id.btnFindCompanion)
        btnFindCompanion.setOnClickListener(findCompanionListner)
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
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        rlp.setMargins(0, 0, 30, 30)
    }

    override fun onResume() {
        super.onResume()
        btnFindCompanion.revertAnimation()
    }

    override fun onPause() {
        super.onPause()
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
    }

    private fun initializeMap() {
        mMap.setOnCameraMoveListener(mCameraMoveListner)
        val locationRequest = LocationRequest()
                .apply {
                    interval = 120000
                    fastestInterval = 120000
                    priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                }
        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper())
    }

    private val findCompanionListner = object : View.OnClickListener {
        override fun onClick(v: View?) {
            Log.i(TAG, "Start Location -> ${mLastKnownLocation}")
            Log.i(TAG, "End Location -> ${destinationLoc}")
            with(btnFindCompanion) {
                startAnimation()
                postDelayed({
                    doneLoadingAnimation(R.color.colorPrimaryDark, resources.getDrawable(R.drawable.ic_done_white).toBitmap())
                    openCommuteListPage()
                }, 3000)
            }

        }
    }

    private fun openCommuteListPage() {
        val cx = (btnFindCompanion.left + btnFindCompanion.right) / 2
        val cy = (btnFindCompanion.top + btnFindCompanion.bottom) / 2
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity!!, btnFindCompanion, "transition");
        val intent = Intent(activity, CommuteListActivity::class.java)
        intent.putExtra(CommuteListActivity.EXTRA_CIRCULAR_REVEAL_X, cx)
        intent.putExtra(CommuteListActivity.EXTRA_CIRCULAR_REVEAL_Y, cy)
        activity?.startActivity(intent, options.toBundle())
//        btnFindCompanion.revertAnimation()
        animateView.visibility = View.INVISIBLE
        /*val animator = ViewAnimationUtils.createCircularReveal(animateView, cx, cy, 0f, resources.displayMetrics.heightPixels * 1.2f)
                .apply {
                    setDuration(500)
                    setInterpolator(AccelerateInterpolator())
                    animateView.setVisibility(View.VISIBLE)
                    cardSearch.setVisibility(View.INVISIBLE)
                    start()
                }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                btnFindCompanion.postDelayed({
                    btnFindCompanion.revertAnimation()
                }, 1000)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity!!, btnFindCompanion, "transition");
                val intent = Intent(activity, CommuteListActivity::class.java)
                intent.putExtra(CommuteListActivity.EXTRA_CIRCULAR_REVEAL_X, cx)
                intent.putExtra(CommuteListActivity.EXTRA_CIRCULAR_REVEAL_Y, cy)
                activity?.startActivity(intent, options.toBundle())
                animateView.visibility = View.INVISIBLE
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })*/
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.lastLocation?.let { loc ->
                mLastKnownLocation = loc
                startLocation = loc
                getAddressFromLocation(loc)
                moveCameraToLocation(loc)
            }
        }
    }

    private val mCameraMoveListner = object : GoogleMap.OnCameraMoveListener {
        override fun onCameraMove() {
            Log.i("Marker", "map => ${mMap.cameraPosition.target}")
            val latLng = mMap.cameraPosition.target
            val location = Location(LocationManager.GPS_PROVIDER)
                    .apply {
                        latitude = latLng.latitude
                        longitude = latLng.longitude
                    }
            if (startPlaceActive)
                startLocation = location
            else
                destinationLoc = location
            getAddressFromLocation(location)
        }

    }

    fun getAddressFromLocation(location: Location) {
        val task = Runnable {
            val address = arrayListOf<Address>()
            Geocoder(activity, Locale.getDefault()).apply {
                address.addAll(getFromLocation(location.latitude, location.longitude, 1))
            }
            val addrText = if (address.size > 0) address[0].getAddressLine(0) else ""
            Handler(Looper.getMainLooper()).post {
                if (startPlaceActive)
                    startPlaceFragment.setText(addrText)
                else
                    destPlaceFragment.setText(addrText)
            }
        }
        geoCodeHandler.postTask(task)
    }

    private val mPlaceSelectionListener = object : PlaceSelectionListener {
        override fun onPlaceSelected(place: Place?) {
            place?.let { p ->
                Location(LocationManager.GPS_PROVIDER)
                        .apply {
                            destinationLoc = this
                            latitude = p.latLng.latitude
                            longitude = p.latLng.longitude
                        }.apply {
                            moveCameraToLocation(this)
                            imgPin.setImageDrawable(activity?.resources?.getDrawable(R.drawable.ic_pin_start))
                        }
                startPlaceActive = true
                btnFindCompanion.visibility = View.VISIBLE
            }
        }

        override fun onError(status: Status?) {
            Log.i(TAG, "An error occurred: " + status)
        }

    }

    private val mDestPlaceListner = object : PlaceSelectionListener {
        override fun onPlaceSelected(place: Place?) {
            place?.let { p ->
                Location(LocationManager.GPS_PROVIDER)
                        .apply {
                            destinationLoc = this
                            latitude = p.latLng.latitude
                            longitude = p.latLng.longitude
                        }.apply {
                            moveCameraToLocation(this)
                            imgPin.setImageDrawable(activity?.resources?.getDrawable(R.drawable.ic_pin_dest))
                        }
                startPlaceActive = false
                btnFindCompanion.visibility = View.VISIBLE
            }
        }

        override fun onError(status: Status?) {
            Log.i(TAG, "An error occurred: " + status)
        }

    }


    private fun moveCameraToLocation(location: Location) {
        with(mMap) {
            moveCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(location.latitude,
                            location.longitude), DEFAULT_ZOOM))
//            centerMarker = addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)))
        }

    }
}