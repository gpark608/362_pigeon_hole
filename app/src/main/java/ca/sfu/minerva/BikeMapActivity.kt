package ca.sfu.minerva

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ca.sfu.minerva.database.BikeRack
import ca.sfu.minerva.database.BikeTheft
import ca.sfu.minerva.databinding.ActivityBikeMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager

class BikeMapActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener  {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityBikeMapBinding

    private lateinit var locationManager: LocationManager
    private var centerMap = false
    private lateinit var markerOptions: MarkerOptions
    private lateinit var mClusterManager: ClusterManager<BikeRack>
    private lateinit var bikeRacks: ArrayList<BikeRack>
    private lateinit var bikeThefts: ArrayList<BikeTheft>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBikeMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    /*
    Sets up map type;
     and verifies location permissions were granted
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        markerOptions = MarkerOptions()
        checkPermission()

        mClusterManager = ClusterManager(this, mMap)
        mMap.setOnCameraIdleListener(mClusterManager);

        fakeBikeRackList()
        fakeBikeTheftList()

        addToMap()
    }

    private fun addToMap() {
        runOnUiThread{
            mClusterManager.addItems(bikeRacks)
            mClusterManager.cluster()
        }
    }


    private fun fakeBikeRackList(){
        bikeRacks = ArrayList()
        for (i in 0..9) {
            val latLng = LatLng((49.267502010791375 + (i * 0.001)).toDouble(), (-123.00311497930385 + (i * 0.001)).toDouble())
            bikeRacks.add(BikeRack(latLng))
        }
    }

    private fun fakeBikeTheftList(){
        bikeThefts = ArrayList()
        val bt1 = BikeTheft(LatLng(49.267221975231465, -123.01393841745823))
        val bt2 = BikeTheft(LatLng(49.26713096333225, -123.01436488868474))
        bikeThefts.add(bt1)
        bikeThefts.add(bt2)
    }

    /*
    Upon location change:
    1) Center map
    2) Update location marker
     */
    override fun onLocationChanged(location: Location) {
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)

        if (!centerMap) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
            mMap.animateCamera(cameraUpdate)
        }
    }

    /*
    Selects the best type of provider based on locationManager and attaches listener to onLocationChange function
     */
    @SuppressLint("MissingPermission")
    fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE
            val provider : String? = locationManager.getBestProvider(criteria, true)
            if(provider != null) {
                val location = locationManager.getLastKnownLocation(provider)

                if(location != null) {
                    onLocationChanged(location)
                }else {
//                    locationManager.requestLocationUpdates(provider, 0, 0f, this)
                    locationManager.requestLocationUpdates(
                        provider,
                        0,
                        0f,
                        this
                    )
                }
            }
        } catch (e: SecurityException) {
        }
    }

    /*
    Verifies that location permissions were granted then calls initLocationManager
     */
    fun checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        } else {
            initLocationManager()
        }
    }


    /*
    Verifies permissions were granted and calls initLocationManager
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                initLocationManager()
            }
        }
    }

    /*
    Upon closing activity, disconnect the listener attached to locationManager.
     */
    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null)
            locationManager.removeUpdates(this)
    }
}