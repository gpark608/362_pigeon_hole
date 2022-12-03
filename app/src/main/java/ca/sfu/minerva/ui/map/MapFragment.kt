package ca.sfu.minerva.ui.map

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ca.sfu.minerva.CoreActivity
import ca.sfu.minerva.FavouritePoiActivity
import ca.sfu.minerva.R
import ca.sfu.minerva.database.BikeRack
import ca.sfu.minerva.databinding.FragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng

class MapFragment : Fragment(), OnMapReadyCallback, LocationListener {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var locationManager: LocationManager
    private var centerMap = false
    private lateinit var markerOptions: MarkerOptions
    private lateinit var mClusterManager: ClusterManager<BikeRack>
    private lateinit var bikeRacks: ArrayList<BikeRack>
    private lateinit var bikeThefts: ArrayList<WeightedLatLng>
    private lateinit var bikeRoute: ArrayList<ArrayList<LatLng>>

    private lateinit var mProvider: HeatmapTileProvider
    private lateinit var mOverlay: TileOverlay
    private lateinit var polylineOptions1: PolylineOptions

    private lateinit var mMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mapViewModel =
            ViewModelProvider(this).get(MapViewModel::class.java)

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

//        val bikeTrail = (activity as CoreActivity).bikeTrail
//        val bikeTheft = (activity as CoreActivity).bikeTheft
//        val bikeRack = (activity as CoreActivity).bikeRack





//        println("debug: print ${value.map{it.groupValues[0]}.joinToString() }}")
//        println("debug: bikeTheft ${bikeTheft}")
//        println("debug: bikeRack ${bikeRack}")





        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        locationManager.removeUpdates(this)
    }
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        markerOptions = MarkerOptions()
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.setPadding(0,700,0,0)


        mClusterManager = ClusterManager(activity, mMap)
        mMap.setOnCameraIdleListener(mClusterManager);

        bikeRackList()
        bikeTheftList()
        bikeRouteList()

        initLocationManager()

        requireActivity().findViewById<Button>(R.id.buttonFavourites)?.setOnClickListener {
            val favouritesIntent = Intent(activity, FavouritePoiActivity::class.java)
            startActivity(favouritesIntent)
        }

        requireActivity().findViewById<Button>(R.id.buttonBikeRacks)?.setOnClickListener {
            addBikeRacks()
        }

        requireActivity().findViewById<Button>(R.id.buttonBikeRental)?.setOnClickListener {
            //TODO: add action here
        }

        requireActivity().findViewById<Button>(R.id.buttonBikeRepair)?.setOnClickListener {
            //TODO: add action here
        }

        requireActivity().findViewById<Button>(R.id.buttonRecyclingCenter)?.setOnClickListener {
            //TODO: add action here
        }

        requireActivity().findViewById<Button>(R.id.buttonBikeRoutes)?.setOnClickListener {
            addBikeRoutes()
        }

        requireActivity().findViewById<Button>(R.id.buttonBikeTheft)?.setOnClickListener {
            addBikeTheft()
        }

    }

    private fun addBikeRacks() {
        activity?.runOnUiThread{
            mClusterManager.addItems(bikeRacks)
            mClusterManager.cluster()
        }
    }

    private fun addBikeTheft() {
        activity?.runOnUiThread{
            val colors = intArrayOf(
                Color.rgb(101, 38, 178), //Purpleish
                Color.rgb(51, 86, 255), //Blueish
                Color.GREEN,
                Color.YELLOW,
                Color.rgb(255,165,0), //Orangeish
                Color.RED,
            )

            val startPoints = floatArrayOf(
                0.001f, 0.002f, 0.003f, 0.004f, 0.006f, 0.01f
            )

            val gradient = Gradient(colors, startPoints)

            mProvider = HeatmapTileProvider.Builder()
                .weightedData(bikeThefts).radius(50).gradient(gradient).opacity(0.5)
                .build()

            mOverlay = mMap.addTileOverlay(TileOverlayOptions().tileProvider(mProvider))!!
        }
    }

    private fun addBikeRoutes() {
        activity?.runOnUiThread{
            for(i in bikeRoute){
                polylineOptions1 = PolylineOptions()
                polylineOptions1.color(Color.rgb((0..255).random(),(0..255).random(),(0..255).random()))
                polylineOptions1.width(10f)
                polylineOptions1.addAll(i)
                mMap.addPolyline(polylineOptions1)
            }


        }
    }

    private fun bikeRackList(){
        bikeRacks = ArrayList()
        val bikeRackList = (activity as CoreActivity).bikeRack
        for (i in bikeRackList) {
            val latLng = LatLng(i.javaClass.getMethod("getLatitude").invoke(i).toString().toDouble(), i.javaClass.getMethod("getLongitude").invoke(i).toString().toDouble())
            bikeRacks.add(BikeRack(latLng))
        }
    }

    private fun bikeTheftList(){
        bikeThefts = ArrayList()
        val bikeTheftList = (activity as CoreActivity).bikeTheft
        for (i in bikeTheftList) {
            val latLng = LatLng(i.javaClass.getMethod("getLatitude").invoke(i).toString().toDouble(), i.javaClass.getMethod("getLongitude").invoke(i).toString().toDouble())
            val count = i.javaClass.getMethod("getCount").invoke(i).toString().toDouble()
            bikeThefts.add(WeightedLatLng(latLng, count))

        }
    }

    private fun bikeRouteList(){
        bikeRoute = ArrayList()


        val bikeTrail = (activity as CoreActivity).bikeTrail
        val reg = "(\\[\\D?\\d*.\\d*, \\D?\\d*.\\d*\\])".toRegex()
        for(i in bikeTrail){
            val matches = reg.findAll(i.javaClass.getMethod("getGeom").invoke(i).toString())
            val latLngs = matches.map{it.value}.toList()
            val singleBikeRoute:ArrayList<LatLng> = ArrayList()
            for(j in latLngs){
//                println("debug: j is ${j}")
                val (long, lat) = j.drop(1).dropLast(1).split(", ")
                println("debug: lat is ${lat} long is ${long}")
                singleBikeRoute.add(LatLng(lat.toDouble(), long.toDouble()))
            }
            bikeRoute.add(singleBikeRoute)
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
            locationManager = activity?.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
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
}