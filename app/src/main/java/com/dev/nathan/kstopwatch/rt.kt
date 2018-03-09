//package com.dev.nathan.kstopwatch
//
//import android.Manifest
//
//import android.content.pm.PackageManager
//import android.location.Location
//import android.os.Bundle
//import android.support.v4.app.ActivityCompat
//import android.support.v7.app.AppCompatActivity
//import android.widget.Button
//import android.widget.TextView
//import android.widget.Toast
//import com.google.android.gms.common.ConnectionResult
//import com.google.android.gms.common.GooglePlayServicesUtil
//import com.google.android.gms.common.api.GoogleApiClient
//import com.google.android.gms.location.LocationListener
//import com.google.android.gms.location.LocationRequest
//import com.google.android.gms.location.LocationServices
//
///**
// * Created by natha on 09/03/2018.
// */
//
//class rt : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
//
//
//
//    private lateinit var txtCoordinates: TextView
//    private lateinit var btnStart: Button
//    private lateinit var btnPause: Button
//    private var mRequestingLocationUpdates = false
//    private lateinit var mLocationRequest: LocationRequest
//    private lateinit var mGoogleApiClient: GoogleApiClient
//    private  var mLastLocation: Location? = null
//
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        when (requestCode) {
//            MY_PERMISSION_REQUEST_CODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                if (checkPlayServices()) {
//                    buildGoogleApiClient()
//                    createLocationRequest()
//                }
//            }
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        txtCoordinates = findViewById<TextView>(R.id.txtCoord)
//        btnStart = findViewById<Button>(R.id.main_start_btn)
//        btnPause = findViewById<Button>(R.id.main_pause_btn)
//
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            //Run-time request permission
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), MY_PERMISSION_REQUEST_CODE)
//        } else {
//            if (checkPlayServices()) {
//                buildGoogleApiClient()
//                createLocationRequest()
//            }
//        }
//
//        btnStart?.setOnClickListener { displayLocation() }
//
//        btnPause?.setOnClickListener { tooglePeriodicLoctionUpdates() }
//    }
//
//
//    override fun onResume() {
//        super.onResume()
//        checkPlayServices()
//    }
//
//    override fun onStart() {
//        super.onStart()
//        if (mGoogleApiClient != null)
//            mGoogleApiClient?.connect()
//    }
//
//    override fun onStop() {
//        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
//        if (mGoogleApiClient != null)
//            mGoogleApiClient?.disconnect()
//        super.onStop()
//    }
//
//    private fun tooglePeriodicLoctionUpdates() {
//        if (!mRequestingLocationUpdates) {
//            btnPause?.text = "Stop location update"
//            mRequestingLocationUpdates = true
//            startLocationUpdates()
//        } else {
//            btnPause?.text = "Start location update"
//            mRequestingLocationUpdates = false
//            stopLocationUpdates()
//        }
//    }
//
//
//    private fun displayLocation() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return
//        }
//        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
//        if (mLastLocation != null) {
//            val latitude = mLastLocation?.latitude
//            val longitude = mLastLocation?.longitude
//            txtCoordinates.text = latitude.toString() + " / " + longitude
//        } else
//            txtCoordinates.text = "Couldn't get the location. Make sure location is enable on the device"
//
//    }
//
//    private fun createLocationRequest() {
//        mLocationRequest = LocationRequest()
//        mLocationRequest?.interval = UPDATE_INTERVAL.toLong()
//        mLocationRequest?.fastestInterval = FATEST_INTERVAL.toLong()
//        mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        mLocationRequest?.smallestDisplacement = DISPLACEMENT.toFloat()
//
//    }
//
//    @Synchronized
//    private fun buildGoogleApiClient() {
//        mGoogleApiClient = GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API).build()
//
//        //Fix first time run app if permission doesn't grant yet so can't get anything
//        mGoogleApiClient?.connect()
//
//
//    }
//
//    private fun checkPlayServices(): Boolean {
//        val resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
//        if (resultCode != ConnectionResult.SUCCESS) {
//            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
//                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show()
//            } else {
//                Toast.makeText(applicationContext, "This device is not supported", Toast.LENGTH_LONG).show()
//                finish()
//            }
//            return false
//        }
//        return true
//    }
//
//    private fun startLocationUpdates() {
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return
//        }
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
//    }
//
//    private fun stopLocationUpdates() {
//        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
//    }
//
//    override fun onConnected(bundle: Bundle?) {
//        displayLocation()
//        if (mRequestingLocationUpdates)
//            startLocationUpdates()
//    }
//
//
//    override fun onConnectionSuspended(i: Int) {
//        mGoogleApiClient.connect()
//    }
//
//    override fun onConnectionFailed(connectionResult: ConnectionResult) {
//
//    }
//
//    override fun onLocationChanged(location: Location) {
//        mLastLocation = location
//        displayLocation()
//    }
//
//    companion object {
//
//        private val MY_PERMISSION_REQUEST_CODE = 7171
//        private val PLAY_SERVICES_RESOLUTION_REQUEST = 7172
//
//        private val UPDATE_INTERVAL = 5000 // SEC
//        private val FATEST_INTERVAL = 3000 // SEC
//        private val DISPLACEMENT = 10 // METERS
//    }
//}
