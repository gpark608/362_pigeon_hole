package ca.sfu.minerva

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
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
import ca.sfu.minerva.databinding.ActivityBikeMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng


class BikeMapActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener  {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityBikeMapBinding

    private lateinit var locationManager: LocationManager
    private var centerMap = false
    private lateinit var markerOptions: MarkerOptions
    private lateinit var mClusterManager: ClusterManager<BikeRack>
    private lateinit var bikeRacks: ArrayList<BikeRack>
    private lateinit var bikeThefts: ArrayList<WeightedLatLng>
    private lateinit var mProvider: HeatmapTileProvider
    private lateinit var mOverlay: TileOverlay


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

        addBikeRacks()
        addBikeTheft()
    }

    private fun addBikeRacks() {
        runOnUiThread{
            mClusterManager.addItems(bikeRacks)
            mClusterManager.cluster()
        }
    }

    private fun addBikeTheft() {
        runOnUiThread{
            val colors = intArrayOf(
                Color.rgb(101, 38, 178), //Purpleish
                Color.rgb(51, 86, 255), //Blueish
                Color.GREEN,
                Color.YELLOW,
                Color.rgb(255,165,0), //Orangeish
                Color.RED,
            )

            val startPoints = floatArrayOf(
                0.1f, 0.2f, 0.3f, 0.4f, 0.6f, 1.0f
            )

            val gradient = Gradient(colors, startPoints)

            mProvider = HeatmapTileProvider.Builder()
                .weightedData(bikeThefts).radius(30).gradient(gradient).opacity(0.5)
                .build()

            mOverlay = mMap.addTileOverlay(TileOverlayOptions().tileProvider(mProvider))!!
        }
    }


    private fun fakeBikeRackList(){
        bikeRacks = ArrayList()
        for (i in 0..9) {
            val latLng = LatLng((49.267502010791375 + i * 0.001 * (0..5).random()), (-123.00311497930385 + i * 0.001 * (0..5).random()))
            bikeRacks.add(BikeRack(latLng))
        }
    }

    private fun fakeBikeTheftList(){
        bikeThefts = ArrayList()
        for (i in 0..9) {
            val latLng = LatLng((49.267502010791375 + (i * 0.0001 * (0..5).random())), (-123.00311497930385 + (i * 0.0001 * (0..5).random())))
            bikeThefts.add(WeightedLatLng(latLng, (0..10).random().toDouble()))
        }
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