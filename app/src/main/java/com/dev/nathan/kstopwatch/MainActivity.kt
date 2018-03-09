package com.dev.nathan.kstopwatch

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import android.content.pm.PackageManager
import android.location.Location

import android.support.v4.app.ActivityCompat

import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices


class MainActivity : AppCompatActivity() , GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private lateinit var txtCoordinates: TextView

    private var mRequestingLocationUpdates = false
    private var mLocationRequest: LocationRequest? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private  var mLastLocation: Location? = null
     lateinit var btnStart: Button
     lateinit var btnPause: Button

     lateinit var txtTimer: TextView
     var customHandler = Handler()
     lateinit var container: LinearLayout

     var startTime = 0L
     var timeInMilliseconds = 0L
     var timeSwapBuff = 0L
     var updateTime = 0L

     var updateTimerThread: Runnable = object : Runnable {
        override fun run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime
            updateTime = timeSwapBuff + timeInMilliseconds
            var secs = (updateTime / 1000).toInt()
            val mins = secs / 60
            secs %= 60
            val miliseconds = (updateTime % 1000).toInt()
            txtTimer.text = ("" + mins + ":" + String.format("%02d", secs) + ":"
                    + String.format("%02d", miliseconds))
            customHandler.postDelayed(this, 0)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSION_REQUEST_CODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkPlayServices()) {
                    buildGoogleApiClient()
                    createLocationRequest()
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtCoordinates = findViewById<TextView>(R.id.txtCoord)
        txtTimer = findViewById<TextView>(R.id.timerValue)
        btnStart = findViewById<Button>(R.id.main_start_btn)
        btnPause = findViewById<Button>(R.id.main_pause_btn)

        container = findViewById<LinearLayout>(R.id.main_result)



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Run-time request permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), MY_PERMISSION_REQUEST_CODE)
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient()
                createLocationRequest()
            }
        }



        btnStart.setOnClickListener {
            tooglePeriodicLoctionUpdates()
            startTime = SystemClock.uptimeMillis()

            customHandler.postDelayed(updateTimerThread, 0)
            displayLocation()
        }

        btnPause.setOnClickListener {
            timeSwapBuff += timeInMilliseconds
            customHandler.removeCallbacks(updateTimerThread)




            val inflater = baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val addView = inflater.inflate(R.layout.row, null)
            val txtValue = addView.findViewById(R.id.txtContent) as TextView
            txtValue.text = txtTimer.text
            container.addView(addView)
        }


    }

    override fun onResume() {
        super.onResume()
        checkPlayServices()
    }

    override fun onStart() {
        super.onStart()
        if (mGoogleApiClient != null)
            mGoogleApiClient?.connect()
    }

    override fun onStop() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
        if (mGoogleApiClient != null)
            mGoogleApiClient?.disconnect()
        super.onStop()
    }

    private fun tooglePeriodicLoctionUpdates() {
        if (!mRequestingLocationUpdates) {

            btnPause.text ="Stop location update"
            mRequestingLocationUpdates = true
            startLocationUpdates()
        } else {
            btnStart.text = "Start location update"
            mRequestingLocationUpdates = false
            stopLocationUpdates()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
        if (mLastLocation != null) {
            val latitude = mLastLocation?.latitude
            val longitude = mLastLocation?.longitude
            txtCoordinates.text = latitude.toString() + " / " + longitude
        } else
            txtCoordinates.text = "Couldn't get the location. Make sure location is enable on the device"

    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest?.interval = UPDATE_INTERVAL.toLong()
        mLocationRequest?.fastestInterval = FATEST_INTERVAL.toLong()
        mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest?.smallestDisplacement = DISPLACEMENT.toFloat()

    }

    @Synchronized
    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build()

        //Fix first time run app if permission doesn't grant yet so can't get anything
        mGoogleApiClient?.connect()


    }

    private fun checkPlayServices(): Boolean {
        val resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show()
            } else {
                Toast.makeText(applicationContext, "This device is not supported", Toast.LENGTH_LONG).show()
                finish()
            }
            return false
        }
        return true
    }

    private fun startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
    }

    private fun stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
    }

    override fun onConnected(bundle: Bundle?) {
        displayLocation()
        if (mRequestingLocationUpdates)
            startLocationUpdates()
    }


    override fun onConnectionSuspended(i: Int) {
        mGoogleApiClient?.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

    }

    override fun onLocationChanged(location: Location) {
        mLastLocation = location
        displayLocation()
    }
    companion object {

        private val MY_PERMISSION_REQUEST_CODE = 7171
        private val PLAY_SERVICES_RESOLUTION_REQUEST = 7172

        private val UPDATE_INTERVAL = 5000 // SEC
        private val FATEST_INTERVAL = 3000 // SEC
        private val DISPLACEMENT = 10 // METERS
    }
}





