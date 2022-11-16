package ca.sfu.minerva

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ca.sfu.minerva.database.*

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ca.sfu.minerva.databinding.ActivityMapsBinding
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.Marker

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener,  GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager:  LocationManager
    private lateinit var location: Location
    private lateinit var bikeMarkerOptions: MarkerOptions
    private lateinit var bikeMarker: Marker

    private lateinit var database: MinervaDatabase
    private lateinit var databaseDao: MinervaDatabaseDao
    private lateinit var repository: MinervaRepository
    private lateinit var viewModelFactory: MinervaViewModelFactory
    private lateinit var viewModel: MinervaViewModel

    private lateinit var sharedPreferences: SharedPreferences

    private var bikeMarkerPresent = false


    private var latLngDB: LatLng = LatLng(0.0,0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        database = MinervaDatabase.getInstance(this)
        databaseDao = database.MinervaDatabaseDao
        repository = MinervaRepository(databaseDao)
        viewModelFactory = MinervaViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MinervaViewModel::class.java]
        viewModel.BikeLocationDataLive.observe(this, Observer {})

        // Use Shared Preferences to determine whether user is prompted to register with Project 529
        sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        promptBikeRegistration()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.setOnMapLongClickListener(this)

        bikeMarkerOptions = MarkerOptions()
        bikeMarkerOptions.title("bikeLocation")
        initLocationManager()
    }

    @SuppressLint("MissingPermission")
    fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_COARSE

            val provider : String? = locationManager.getBestProvider(criteria, true)
            if(provider != null) {
                location = locationManager.getLastKnownLocation(provider)!!


                onLocationChanged(location)



                locationManager.requestLocationUpdates(provider, 0, 0f, this)
            }
        } catch (e: SecurityException) {
        }
    }

    override fun onLocationChanged(location: Location) {

        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)
        val cameraUpdate:CameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)

        latLngDB = latLng
        bikeMarkerOptions.position(latLng)

        if(!bikeMarkerPresent){

            mMap.animateCamera(cameraUpdate)
            bikeMarker = mMap.addMarker(bikeMarkerOptions)!!
            bikeMarkerPresent=true
        }else{

            bikeMarker.remove()
            bikeMarker = mMap.addMarker(bikeMarkerOptions)!!

        }

        bikeMarker.showInfoWindow()



    }

    override fun onMapLongClick(latLng: LatLng) {
        latLngDB = latLng
        locationManager.removeUpdates(this)
        bikeMarker.remove()
        bikeMarkerOptions.position(latLng)

        bikeMarker = mMap.addMarker(bikeMarkerOptions)!!
        bikeMarker.showInfoWindow()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem)= when (item.itemId) {
        R.id.item1 -> {
            // User chose the "Settings" item, show the app settings UI...
            val bikeLocation = BikeLocation()
            bikeLocation.bikeLocation = latLngDB
            viewModel.insertBikeLocation(bikeLocation)



            true

        }



        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun promptBikeRegistration() {
        val isBikeRegistered = sharedPreferences.getBoolean("registered", false)

        if (!isBikeRegistered) {
            val intent = Intent(this, BikeRegisterActivity::class.java)
            startActivity(intent)
        }
    }
}



