package com.doubleshadow.fiw

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException





class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    override fun onMarkerClick(p0: Marker?) = false

    private lateinit var map: GoogleMap

    private lateinit var mDatabase: DatabaseReference

    private lateinit var refDatabase: DatabaseReference

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var lastKnownLocation: Location

    private lateinit var locationCallback: LocationCallback

    private lateinit var locationRequest: LocationRequest

    private var locationUpdateState = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1

        private const val REQUEST_CHECK_SETTINGS = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastKnownLocation = p0.lastLocation
                placeMarkerOnMap(LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude))
            }
        }

        createLocationRequest()

        /*add_location_btn.setOnClickListener(object : View.OnClickListener(){
            //addLocation()
            override fun onClick(view: View) {
                //change to refDatabase or mDatabase.child("Location")
                val newPost = mDatabase.push()
                //the push() command is already creating unique key
                newPost.setValue(latlng)
            }
        })*/

    }

    private fun getAddress(latLng: LatLng): String {
        // Creating a Geocoder object to turn a coordinates into an address
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            // Get the address from the location
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            // If there is address on the location, it returns string
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses[0]
                for (i in 0 until address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(i)
                }
            }
        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage)
        }

        return addressText
    }

    private fun placeMarkerOnMap(location: LatLng) {
        // Create marker and set it to current location
        val markerOptions = MarkerOptions().position(location)

        // Add the address to the marker as a title
        val titleStr = getAddress(location)
        markerOptions.title(titleStr)

        map.addMarker(markerOptions)
    }

    private fun setUpMap() {
        // Checks if location tracking is enabled by user
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        // Sets location tracking boolean true
        map.isMyLocationEnabled = true

        map.mapType = GoogleMap.MAP_TYPE_TERRAIN

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastKnownLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
    }

    private fun startLocationUpdates() {
        // If location tracking isn't enabled, this requests it and returns it
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        // Requests for location updates
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }

    private fun createLocationRequest() {
        // Retrivies and handles changes in users current location state
        locationRequest = LocationRequest()
        // Rate of updates
        locationRequest.interval = 10000
        // Fastest rate of handling updates
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        // Checking location settings task
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // Initiates a location request if success
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MapsActivity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    // To stop location update request
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // To restart the location update request
    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    private fun addLocation() {
        val intent = Intent(this, SaveActivity::class.java)
        startActivity(intent)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.getUiSettings().setZoomControlsEnabled(true)
        map.setOnMarkerClickListener(this)

        /*
        val helsinki = LatLng(60.192059, 24.945831)
        map.addMarker(MarkerOptions().position(helsinki).title("Marker in Sydney"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(helsinki, 12.0f))
        */

        setUpMap()

        map.setOnMapLongClickListener { point ->

            // added marker saved as marker and coordinates passed to latlng
            val marker = map.addMarker(MarkerOptions().position(point))
            val latlng = marker.getPosition()

            add_location_btn.setOnClickListener {
                val newPost = mDatabase.child("location").push()
                newPost.setValue(latlng)
            }
        }

        /*refDatabase.addChildEventListener(object : ChildEventListener() {
            override fun onChildAdded(dataSnapshot : DataSnapshot, prevChildKey : String) {
                val newLocation = LatLng(
                    dataSnapshot.child("latitude").getValue(Long.class),
                        dataSnapshot.child("longitude").getValue(Long.class)
                        )
                map.addMarker(MarkerOptions()
                    .position(newLocation)
                    .title(dataSnapshot.getKey()));
            }

            override fun onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {}

            override fun onChildRemoved(DataSnapshot dataSnapshot) {}

            override fun onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            override fun onCancelled(DatabaseError databaseError) {}
            })
            */
    }
}
