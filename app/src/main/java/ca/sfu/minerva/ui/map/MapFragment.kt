package ca.sfu.minerva.ui.map

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.location.*
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.sfu.minerva.CoreActivity
import ca.sfu.minerva.FavouritePoiActivity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ca.sfu.minerva.R
import ca.sfu.minerva.database.*
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
import java.util.*
import kotlin.collections.ArrayList

class MapFragment : Fragment(), OnMapReadyCallback, LocationListener, GoogleMap.OnMapLongClickListener {

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
    private lateinit var polylineOptions: PolylineOptions

    private lateinit var mMap: GoogleMap


    private var bikeRacksToggle: Boolean = false
    private var bikeTheftsToggle: Boolean = false
    private var bikeRouteToggle: Boolean = false

    private var polyline_final: ArrayList<Polyline> = arrayListOf()
    private lateinit var database: MinervaDatabase
    private lateinit var databaseDao: MinervaDatabaseDao
    private lateinit var repository: MinervaRepository
    private lateinit var viewModelFactory: MinervaViewModelFactory
    private lateinit var viewModel: MinervaViewModel

    // Favourite POI declarations
    private lateinit var mapListView: ListView
    private lateinit var mapListAdapter: MapListAdapter
    private lateinit var poiDataList: ArrayList<FavouriteLocation>
    private lateinit var poiMarkerOption: MarkerOptions
    private lateinit var poiMarkers: ArrayList<Marker>
    private lateinit var buttonList: Button

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

        database = MinervaDatabase.getInstance(requireActivity())
        databaseDao = database.MinervaDatabaseDao
        repository = MinervaRepository(databaseDao)
        viewModelFactory = MinervaViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MinervaViewModel::class.java)


        // Favourite POI initializations
        poiDataList = ArrayList()
        poiMarkers = ArrayList()
        buttonList = root.findViewById(R.id.buttonActivateList)

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
        mMap.setOnMapLongClickListener(this)
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.setPadding(0,700,0,0)


        mClusterManager = ClusterManager(activity, mMap)
        mMap.setOnCameraIdleListener(mClusterManager);

        bikeRackList()
        bikeTheftList()
        bikeRouteList()

        initLocationManager()


        favouritesList()

        if(viewModel.favouritesActive)
            addFavouritesToMap()

        requireActivity().findViewById<Button>(R.id.buttonFavourites)?.setOnClickListener {
            if(!viewModel.favouritesActive){
                buttonList.visibility = View.VISIBLE
                addFavouritesToMap()
            }
            else {
                buttonList.visibility = View.INVISIBLE
                removeFavouritesFromMap()
            }
            viewModel.favouritesActive = !viewModel.favouritesActive

            buttonList.setOnClickListener {
                showListOfFavourites()
            }
        }

        requireActivity().findViewById<Button>(R.id.buttonBikeRacks)?.setOnClickListener {
            bikeRacksToggle = if(!bikeRacksToggle){
                addBikeRacks()
                true
            }else{
                mClusterManager.clearItems()
                mClusterManager.cluster()
                false
            }

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
            bikeRouteToggle = if(!bikeRouteToggle){
                addBikeRoutes()
                true
            }else{
                for(i in polyline_final){
                    i.remove()
                }
                false
            }

        }

        requireActivity().findViewById<Button>(R.id.buttonBikeTheft)?.setOnClickListener {
            bikeTheftsToggle = if(!bikeTheftsToggle){
                addBikeTheft()
                true
            }else{
                mOverlay.remove()
                false
            }

        }

    }

    private fun favouritesList(){
        // setting poi marker
        poiMarkerOption = MarkerOptions().icon(
            BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_RED))

        viewModel.FavouriteLocationDataLive.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            poiDataList.clear()
            poiDataList.addAll(it)
        })
    }

    private fun showListOfFavourites(){
        val alertDialog: AlertDialog? = this.let {
            var customLayout: LinearLayout = LinearLayout(requireActivity())
            customLayout.orientation = LinearLayout.VERTICAL

            val builder = AlertDialog.Builder(requireActivity())

            mapListView = ListView(requireActivity())
            mapListAdapter = MapListAdapter(requireActivity(), poiDataList)
            mapListView.adapter = mapListAdapter
            customLayout.addView(mapListView)

            viewModel.FavouriteLocationDataLive.observe(this){
                mapListAdapter.replaceList(it)
                mapListAdapter.notifyDataSetChanged()
            }

            val deleteOption = CheckBox(requireActivity())
            deleteOption.isChecked = false
            deleteOption.text = "Select to delete favourite"
            customLayout.addView(deleteOption)

            mapListView.setOnItemClickListener{parent, view, position, id ->
                val fav = mapListAdapter.getItem(position)
                if(deleteOption.isChecked){
                    // process a delete
                    viewModel.deleteFavouriteLocation(fav)
                }
                else{
                    // zoom into specific pin
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(getLatLang(fav).latitude, getLatLang(fav).longitude), 25f)
                    mMap.animateCamera(cameraUpdate)

                    Toast.makeText(
                        requireActivity(),
                        "Viewing location of ${fav.name}", Toast.LENGTH_SHORT
                    ).show()
                }
            }

            builder.setView(customLayout)

            builder.setPositiveButton("Close") { dialog, pos ->
                dialog.dismiss()
            }

            builder.setTitle("List of Favourites")

            builder.create()
            builder.show()
        }
    }

    private fun addFavouritesToMap(){
        poiMarkerOption = MarkerOptions()
        poiMarkerOption.position(LatLng(49.2781, -122.9199))
        var poiMarker = mMap.addMarker(poiMarkerOption)!!
        poiMarker.remove()

        buttonList.visibility = View.VISIBLE

        Log.d("poilist", "${poiDataList.size}")
        for(favourite in poiDataList){
            poiMarkers.add( mMap.addMarker(poiMarkerOption.title(favourite.name).position(getLatLang(favourite)))!! )

        }
    }

    private fun removeFavouritesFromMap(){
        buttonList.visibility = View.INVISIBLE

        for(point in poiMarkers)
            point.remove()
    }

    fun getLatLang(fav: FavouriteLocation): LatLng{
        var favLatLng = LatLng(
            fav.favLatLng.substringBefore(',').toDouble(),
            fav.favLatLng.substringAfter(',').toDouble()
        )

        return favLatLng
    }

    override fun onMapLongClick(latLng: LatLng) {
        if(viewModel.favouritesActive) {
            val alertDialog: AlertDialog? = this.let {
                var customLayout: LinearLayout = LinearLayout(requireActivity())
                customLayout.orientation = LinearLayout.VERTICAL

                val builder = AlertDialog.Builder(requireActivity())
                var fav: FavouriteLocation = FavouriteLocation()

                val titleInput = EditText(requireActivity())
                titleInput.inputType = InputType.TYPE_CLASS_TEXT
                titleInput.hint = "Enter name of poi"
                customLayout.addView(titleInput)


                val commentInput = EditText(requireActivity())
                commentInput.inputType = InputType.TYPE_CLASS_TEXT
                commentInput.hint = "Enter comment"
                customLayout.addView(commentInput)

                builder.setView(customLayout)

                builder.setPositiveButton("Ok") { dialog, pos ->
                    fav.name = titleInput.text.toString()

                    fav.favLatLng = "${latLng.latitude.toFloat()},${latLng.longitude.toFloat()}"

                    var descriptionResult: String = "None"
                    if (commentInput.text.toString() != "")
                        descriptionResult = commentInput.text.toString()
                    fav.description = descriptionResult

                    viewModel.insertFavouriteLocation(fav)


                    mMap.addMarker(poiMarkerOption.title(fav.name).position(latLng))



                    Toast.makeText(requireActivity(), "Saved ${fav.name}", Toast.LENGTH_SHORT)
                        .show()
                }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setTitle("Save location as:")

                builder.create()
                builder.show()
            }
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
                polylineOptions = PolylineOptions()
                polylineOptions.color(Color.rgb((0..255).random(),(0..255).random(),(0..255).random()))
                polylineOptions.width(10f)
                polylineOptions.addAll(i)
                polyline_final.add(mMap.addPolyline(polylineOptions))
            }


        }
    }

    private fun bikeRackList(){
        bikeRacks = ArrayList()
        val bikeRackList = (activity as CoreActivity).bikeRack
        for (i in bikeRackList) {
            val latLng = LatLng(i.javaClass.getMethod("getLatitude").invoke(i).toString().toDouble(), i.javaClass.getMethod("getLongitude").invoke(i).toString().toDouble())
            val locationTitle = "${i.javaClass.getMethod("getStreetName").invoke(i)} ${i.javaClass.getMethod("getStreetNumber").invoke(i)}"
            var locationSnippet = ""
            if(i.javaClass.getMethod("getSkytrainStationName").invoke(i).toString().isNotBlank()){
                locationSnippet = "${i.javaClass.getMethod("getSkytrainStationName").invoke(i)} Station"
            }
            bikeRacks.add(BikeRack(latLng, locationTitle, locationSnippet))
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

class MapListAdapter(private val context: Context, private var optionList: List<FavouriteLocation>): BaseAdapter(){
    override fun getCount(): Int {
        return optionList.size
    }

    override fun getItem(position: Int): FavouriteLocation {
        return optionList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = View.inflate(context, R.layout.favourites_adapter_layout, null)
        val textTop = view.findViewById<TextView>(R.id.list_adapter_top)
        val textMiddle = view.findViewById<TextView>(R.id.list_adapter_middle)
        val textBottom = view.findViewById<TextView>(R.id.list_adapter_bottom)
        textTop.text = optionList[position].name
        val description = optionList[position].description
        if(description != "")
            textMiddle.text = "Description: " + description
        else
            textMiddle.text = "Description: None"

        var favLatLng = LatLng(
            optionList[position].favLatLng.substringBefore(',').toDouble(),
            optionList[position].favLatLng.substringAfter(',').toDouble()
        )

        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(favLatLng.latitude, favLatLng.longitude,1)
        var addressResult: String = "Could not define"
        if(addresses.isNotEmpty())
            addressResult = addresses[0].getAddressLine(0).toString()

        textBottom.text = "Address: $addressResult"

        return view
    }

    fun replaceList(newList: List<FavouriteLocation>){
        optionList = newList
    }

}