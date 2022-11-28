package ca.sfu.minerva.ui.map

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ca.sfu.minerva.BuildConfig
import ca.sfu.minerva.FavouritePoiActivity
import ca.sfu.minerva.R
import ca.sfu.minerva.database.BikeRack
import ca.sfu.minerva.databinding.FragmentMapBinding
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.util.Properties
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MapFragment : Fragment(), OnMapReadyCallback, LocationListener,GoogleMap.OnMapLongClickListener {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var locationManager: LocationManager
    private var centerMap = false
    private lateinit var markerOptions: MarkerOptions
    private lateinit var mClusterManager: ClusterManager<BikeRack>
    private lateinit var bikeRacks: ArrayList<BikeRack>
    private lateinit var bikeThefts: ArrayList<WeightedLatLng>
    private lateinit var bikeRoute1: ArrayList<LatLng>
    private lateinit var bikeRoute2: ArrayList<LatLng>
    private lateinit var mProvider: HeatmapTileProvider
    private lateinit var mOverlay: TileOverlay
    private lateinit var polylineOptions1: PolylineOptions
    private lateinit var polylineOptions2: PolylineOptions
    private lateinit var mMap: GoogleMap

    private lateinit var bikeMarkerOptions: MarkerOptions
    private lateinit var bikeMarker: Marker

    private var bikeMarkerPresent = false
    private var latLngDB: LatLng = LatLng(0.0,0.0)
    private val mapsAPIKey = BuildConfig.MAPS_API_KEY

    private var currentLatLng = LatLng(0.0, 0.0)
    private var destLatLng = LatLng(49.256390, -122.956770)

    private var transMode = arrayOf("BICYCLING", "WALKING")
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


        root.findViewById<Button>(R.id.buttonFavourites)?.setOnClickListener {
            val favouritesIntent = Intent(activity, FavouritePoiActivity::class.java)
            startActivity(favouritesIntent)
        }


        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        locationManager.removeUpdates(this)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.setOnMapLongClickListener(this)

        markerOptions = MarkerOptions()
        bikeMarkerOptions = MarkerOptions()
        bikeMarkerOptions.title("bikeLocation")

        mClusterManager = ClusterManager(activity, mMap)
        mMap.setOnCameraIdleListener(mClusterManager);

        fakeBikeRackList()
        fakeBikeTheftList()
        fakeBikeRouteList()

        addBikeRacks()
        addBikeTheft()
        addBikeRoutes()

        initLocationManager()
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
                0.1f, 0.2f, 0.3f, 0.4f, 0.6f, 1.0f
            )

            val gradient = Gradient(colors, startPoints)

            mProvider = HeatmapTileProvider.Builder()
                .weightedData(bikeThefts).radius(30).gradient(gradient).opacity(0.5)
                .build()

            mOverlay = mMap.addTileOverlay(TileOverlayOptions().tileProvider(mProvider))!!
        }
    }

    private fun addBikeRoutes() {
        activity?.runOnUiThread{
            polylineOptions1 = PolylineOptions()
            polylineOptions2 = PolylineOptions()
            polylineOptions1.color(Color.BLUE)
            polylineOptions1.width(15f)
            polylineOptions2.color(Color.RED)
            polylineOptions1.width(15f)

            polylineOptions1.addAll(bikeRoute1)
            polylineOptions2.addAll(bikeRoute2)

            mMap.addPolyline(polylineOptions1)
            mMap.addPolyline(polylineOptions2)
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

    private fun fakeBikeRouteList(){
        bikeRoute1 = ArrayList()
        bikeRoute1.add(LatLng(49.27249601539955, -123.13048439225132))
        bikeRoute1.add(LatLng(49.27245498146834, -123.1305055947476))
        bikeRoute1.add(LatLng(49.27236202151856, -123.13063695695938))
        bikeRoute1.add(LatLng(49.27232556611822, -123.13066531295438))
        bikeRoute1.add(LatLng(49.2722931088385, -123.13065808564242))
        bikeRoute1.add(LatLng(49.27176081294581, -123.1298408652264))
        bikeRoute1.add(LatLng(49.27173828084447, -123.12980487311506))
        bikeRoute1.add(LatLng(49.27172186503857, -123.12976685704314))
        bikeRoute1.add(LatLng(49.27170931828907, -123.12972782974923))
        bikeRoute1.add(LatLng(49.27170208235533, -123.12968473632525))
        bikeRoute1.add(LatLng(49.271697810987014, -123.12964425708918))
        bikeRoute1.add(LatLng(49.27169786096204, -123.12959970910934))
        bikeRoute1.add(LatLng(49.27170447279747, -123.12955767199581))
        bikeRoute1.add(LatLng(49.27171592892096, -123.12952732277496))
        bikeRoute1.add(LatLng(49.271869347312794, -123.12929364603939))
        bikeRoute1.add(LatLng(49.27189775429988, -123.1292243477885))
        bikeRoute1.add(LatLng(49.271882265573744, -123.12916150864038))
        bikeRoute1.add(LatLng(49.27128312647653, -123.12823823821586))
        bikeRoute1.add(LatLng(49.27101014493617, -123.12781758468621))
        bikeRoute1.add(LatLng(49.271006815076554, -123.12781011988474))

        bikeRoute2 = ArrayList()
        bikeRoute2.add(LatLng(49.308080088698574, -123.147076521593))
        bikeRoute2.add(LatLng(49.30848086458077, -123.14633517216237))
        bikeRoute2.add(LatLng(49.30913795492011, -123.14616240078895))
        bikeRoute2.add(LatLng(49.30968030896991, -123.14638240459482))
        bikeRoute2.add(LatLng(49.31002241366392, -123.14686390142336))
        bikeRoute2.add(LatLng(49.31070795224121, -123.14677857775216))
        bikeRoute2.add(LatLng(49.31090897457683, -123.14590555457607))
        bikeRoute2.add(LatLng(49.311280688887194, -123.14555720075641))
        bikeRoute2.add(LatLng(49.31196639214391, -123.14534081164526))
        bikeRoute2.add(LatLng(49.31193865560781, -123.144908183308))
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
        currentLatLng = LatLng(lat, lng)

        val cameraUpdate: CameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)

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

    override fun onMapLongClick(latLng: LatLng) {
        latLngDB = latLng
        locationManager.removeUpdates(this)
//        bikeMarker.remove()
        bikeMarkerOptions.position(latLng)

        bikeMarker = mMap.addMarker(bikeMarkerOptions)!!
        bikeMarker.showInfoWindow()
        destLatLng = LatLng(latLng.latitude, latLng.longitude)
        test()
    }

    fun test(){
//        https://lwgmnz.me/2018/09/20/google-maps-and-directions-api-using-kotlin/
        val path: MutableList<List<LatLng>> = ArrayList()
        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=${currentLatLng.latitude},${currentLatLng.longitude}&destination=${destLatLng.latitude},${destLatLng.longitude}&mode=BICYCLING&key=${mapsAPIKey}"


        val directionsRequest = object : StringRequest(Request.Method.GET, urlDirections, Response.Listener<String> {
           response ->
            val jsonResponse = JSONObject(response)
            val statusCode = jsonResponse.getString("status")
            if(statusCode == "OK"){
                val routes = jsonResponse.getJSONArray("routes")
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                val steps = legs.getJSONObject(0).getJSONArray("steps")
                for (i in 0 until steps.length()) {
                    val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                    path.add(PolyUtil.decode(points))
                }
                for (i in 0 until path.size) {
                    mMap.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
                }
//                println("debug: jsonResponse ${jsonResponse}")
            }else{}

           }, Response.ErrorListener {
           _ ->
           }){}
       val requestQueue = Volley.newRequestQueue(context)
       requestQueue.add(directionsRequest)
    }
}
