package ca.sfu.minerva.ui.map

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.location.*
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ca.sfu.minerva.CoreActivity
import ca.sfu.minerva.R
import ca.sfu.minerva.data.BikeShop
import ca.sfu.minerva.database.*
import ca.sfu.minerva.databinding.FragmentMapBinding
import ca.sfu.minerva.util.TrackingService
import ca.sfu.minerva.util.TrackingViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.util.*


class MapFragment : Fragment(), OnMapReadyCallback, LocationListener, GoogleMap.OnMapLongClickListener {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var locationManager: LocationManager
    private var centerMap = false
    private lateinit var markerOptions: MarkerOptions
    private lateinit var mClusterManagerBikeRack: ClusterManager<BikeRack>
    private lateinit var mClusterManagerRecycleCenter: ClusterManager<RecycleCenterCluster>
    private lateinit var mClusterManagerBikeShop: ClusterManager<BikeShopCluster>


    private lateinit var bikeRacks: ArrayList<BikeRack>
    private lateinit var bikeThefts: ArrayList<WeightedLatLng>
    private lateinit var bikeRoute: ArrayList<ArrayList<LatLng>>
    private lateinit var recycleCenter: ArrayList<RecycleCenterCluster>
    private lateinit var bikeShop: ArrayList<BikeShopCluster>

    private lateinit var mProvider: HeatmapTileProvider
    private lateinit var mOverlay: TileOverlay
    private lateinit var polylineOptions: PolylineOptions

    private lateinit var mMap: GoogleMap

    // Bottom Sheet
    private lateinit var bottomSheetView: ConstraintLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var textViewTitle: TextView
    private lateinit var textViewAddress: TextView
    private lateinit var textViewBikeRacks: TextView
    private var isBikeRackSelected = false

    private var bikeRacksToggle: Boolean = false
    private var bikeTheftsToggle: Boolean = false
    private var bikeRouteToggle: Boolean = false
    private var recycleCenterToggle: Boolean = false
    private var bikeShopToggle: Boolean = false

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

    // current Bike Location
    private lateinit var currentBikeLocationMarkerOption: MarkerOptions
    private lateinit var currentBikeLocationMarker: Marker
    private var currentbikeMarkerPresent = false
    private var clickedMarkerLocation = LatLng(0.0, 0.0)
    private lateinit var location: Location

    // Tracking declarations
    private lateinit var buttonTracking: Button
    private lateinit var trackingServiceIntent: Intent
    private lateinit var trackingViewModel: TrackingViewModel
    private lateinit var latestTrackingBundle: Bundle


    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mapViewModel =
            ViewModelProvider(this).get(MapViewModel::class.java)

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        textViewTitle = root.findViewById(R.id.text_title)
        textViewAddress = root.findViewById(R.id.text_address)
        textViewBikeRacks = root.findViewById(R.id.text_bike_racks)
        bottomSheetView = root.findViewById(R.id.bottomSheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)
        setBottomSheetVisibility(false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        database = MinervaDatabase.getInstance(requireActivity())
        databaseDao = database.MinervaDatabaseDao
        repository = MinervaRepository(databaseDao)
        viewModelFactory = MinervaViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MinervaViewModel::class.java]





        // Favourite POI initializations
        poiDataList = ArrayList()
        poiMarkers = ArrayList()
        buttonList = root.findViewById(R.id.buttonActivateList)

        // Tracking initializations
        buttonTracking = root.findViewById(R.id.buttonTracking)
        trackingViewModel = ViewModelProvider(this)[TrackingViewModel::class.java]
        trackingServiceIntent = Intent(requireActivity(), TrackingService::class.java)

        return root
    }

    private fun onClickMarker(bikeRack: BikeRack) {
        val lat = bikeRack.position.latitude
        val lng = bikeRack.position.longitude
        clickedMarkerLocation = LatLng(lat, lng)

        val geoCoder = Geocoder(context)
        val matches = geoCoder.getFromLocation(lat, lng, 1)
        val bestMatch = if (matches.isEmpty()) null else matches[0]

        textViewTitle.text = "Bike Rack - ${bikeRack.title}"
        textViewAddress.text = bestMatch!!.getAddressLine(0).toString()
        textViewBikeRacks.text = bikeRack.getNumberOfRacks().toString()

        isBikeRackSelected = true
        setBottomSheetVisibility(true)
    }



    private fun setBottomSheetVisibility(isVisible: Boolean) {
        val updatedState = if (isVisible) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.state = updatedState
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
        mMap.setPadding(0,600,0,800)

        currentBikeLocationMarkerOption = MarkerOptions()
        currentBikeLocationMarkerOption.title("Current Bike Location")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

        sharedPreferences = requireActivity().getSharedPreferences("bikeLocation", AppCompatActivity.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        val currentMarkerSharedPreference =sharedPreferences.getStringSet("latlng", setOf())
        println("debug: currentMarkerSharedPreference ${currentMarkerSharedPreference}")
        if(!currentMarkerSharedPreference.isNullOrEmpty()){
            val lat = currentMarkerSharedPreference.elementAt(0).toDouble()
            val lng = currentMarkerSharedPreference.elementAt(1).toDouble()
            clickedMarkerLocation = LatLng(lat, lng)

            currentBikeLocationMarkerOption.position(clickedMarkerLocation)
            currentBikeLocationMarker = mMap.addMarker(currentBikeLocationMarkerOption)!!
            currentBikeLocationMarker.zIndex = Float.MAX_VALUE
            currentBikeLocationMarker.showInfoWindow()
            requireActivity().findViewById<TextView>(R.id.saveBikeRack).text = "Remove Current Location"
            requireActivity().findViewById<TextView>(R.id.saveBikeRack).setTextColor(Color.RED)
            currentbikeMarkerPresent = true


        }
        mClusterManagerBikeRack = ClusterManager(activity, mMap)
        mClusterManagerRecycleCenter = ClusterManager(activity, mMap)
        mClusterManagerBikeShop = ClusterManager(activity, mMap)


//        mMap.setOnCameraIdleListener(mClusterManagerBikeRack)

        mMap.setOnCameraIdleListener {

            mClusterManagerBikeRack.cluster()
            mClusterManagerRecycleCenter.cluster()
            mClusterManagerBikeShop.cluster()

        }

        mMap.setOnCameraMoveListener {
            val zoomLevel = mMap.cameraPosition.zoom

            if (bikeTheftsToggle && zoomLevel < 12) {
                mOverlay.remove()
            }
        }
        bikeRackList()
        bikeTheftList()
        bikeRouteList()
        recycleCenterList()
        bikeShopList()
        initLocationManager()

        mClusterManagerBikeRack.setOnClusterItemClickListener { item ->
            onClickMarker(item)
            false
        }


        mMap.setOnMapClickListener {
            setBottomSheetVisibility(false)
        }

        favouritesList()

        if(viewModel.favouritesActive)
            addFavouritesToMap()

        requireActivity().findViewById<ImageView>(R.id.image_share).setOnClickListener {
            if (isBikeRackSelected) {
                val sharingIntent = Intent(Intent.ACTION_SEND)

                // type of the content to be shared
                sharingIntent.type = "text/plain"

                // subject of the content. you can share anything
                val shareSubject = textViewTitle.text.toString()
                // passing subject of the content
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject)

                // Body of the content
                val shareBody = textViewAddress.text.toString()
                // passing body of the content
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)

                startActivity(Intent.createChooser(sharingIntent, "Share via"))
            }
        }

        val saveBikeRackTextView =requireActivity().findViewById<TextView>(R.id.saveBikeRack)
        saveBikeRackTextView.setOnClickListener {
            if (isBikeRackSelected) {
                if(!currentbikeMarkerPresent){
                    val latlngSet = mutableSetOf<String>()
                    latlngSet.add(clickedMarkerLocation.latitude.toString())
                    latlngSet.add(clickedMarkerLocation.longitude.toString())
                    editor.putStringSet("latlng", latlngSet)
                    editor.apply()

                    currentBikeLocationMarkerOption.position(clickedMarkerLocation)
                    currentBikeLocationMarker = mMap.addMarker(currentBikeLocationMarkerOption)!!
                    currentBikeLocationMarker.zIndex = Float.MAX_VALUE
                    currentBikeLocationMarker.showInfoWindow()
                    saveBikeRackTextView.text = "Remove Current Location"
                    saveBikeRackTextView.setTextColor(Color.RED)

                    currentbikeMarkerPresent = true

                }else{
                    editor.putStringSet("latlng", setOf())
                    saveBikeRackTextView.text = "Save BikeRack as \nCurrent Location"
                    saveBikeRackTextView.setTextColor(resources.getColor(R.color.green,null))
                    currentBikeLocationMarker.remove()
                    currentbikeMarkerPresent = false
                }
            }else if(!isBikeRackSelected && currentbikeMarkerPresent){
                editor.putStringSet("latlng", setOf())
                saveBikeRackTextView.text = "Save BikeRack as \nCurrent Location"
                saveBikeRackTextView.setTextColor(resources.getColor(R.color.green,null))
                currentBikeLocationMarker.remove()
                currentbikeMarkerPresent = false
            }


        }

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
            onToggle(it as Button)
            val btnBikeTheft = requireActivity().findViewById<Button>(R.id.buttonBikeTheft)
            // only allow the Bike Theft button to be toggled if the
            // Bike Racks button is toggled and un-toggle Bike Theft
            // button if toggled when Bike Racks button is toggled
            btnBikeTheft.isEnabled = it.isSelected
            if (!it.isSelected && btnBikeTheft.isSelected) {
                btnBikeTheft.callOnClick()
            }

            bikeRacksToggle = if(!bikeRacksToggle){
                addBikeRacks()
                true
            }else{
                mClusterManagerBikeRack.clearItems()
                mClusterManagerBikeRack.cluster()
                false
            }

        }

        buttonTracking.setOnClickListener {
            if(!viewModel.bikeTrackingToggle){
                startTrackingService()
                buttonTracking.text = "Stop Tracking"
            }
            else {
                // Turning off the service
                stopTrackingService()
                buttonTracking.text = "Start Tracking"
            }

            viewModel.bikeTrackingToggle = !viewModel.bikeTrackingToggle

        }

        requireActivity().findViewById<Button>(R.id.buttonRecycleCenter)?.setOnClickListener {
            onToggle(it as Button)


            recycleCenterToggle = if(!recycleCenterToggle){
                addRecycleCenter()
                true
            }else{
                mClusterManagerRecycleCenter.clearItems()
                mClusterManagerRecycleCenter.cluster()
                false
            }

        }

        requireActivity().findViewById<Button>(R.id.buttonBikeShop)?.setOnClickListener {
            onToggle(it as Button)
            bikeShopToggle = if(!bikeShopToggle){
                addBikeShop()
                true
            }else{
                mClusterManagerBikeShop.clearItems()
                mClusterManagerBikeShop.cluster()
                false
            }
        }


        requireActivity().findViewById<Button>(R.id.buttonBikeRoutes)?.setOnClickListener {
            onToggle(it as Button)
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
            onToggle(it as Button)
            if(!bikeTheftsToggle){
                if(mMap.cameraPosition.zoom < 12){
                    Toast.makeText(
                        requireActivity(),
                        "Need to Zoom in More", Toast.LENGTH_SHORT
                    ).show()
                }else{
                    addBikeTheft()
                    bikeTheftsToggle = true
                }

            }else{
                mOverlay.remove()
                bikeTheftsToggle = false
            }

        }
    }


    private fun onToggle(button: Button) {
        button.isSelected = !button.isSelected
        if (button.isSelected) {
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.ocean_green))
        } else {
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.ghost_white))
        }
    }

    private fun startTrackingService(){
        ContextCompat.startForegroundService(requireActivity(), trackingServiceIntent)
//        requireActivity().application.startForegroundService(trackingServiceIntent)
        requireActivity().applicationContext.bindService(trackingServiceIntent, trackingViewModel, Context.BIND_AUTO_CREATE)

        trackingViewModel.mapBundle.observe(viewLifecycleOwner){
            latestTrackingBundle = it

            drawCurrentBikeUsage()
        }

    }

    private fun stopTrackingService(){
        requireActivity().applicationContext.unbindService(trackingViewModel)
        requireActivity().applicationContext.stopService(trackingServiceIntent)

        CoroutineScope(IO).launch {
            // save the current usage into db
            var bikeUsage = BikeUsage()
            bikeUsage.climb = latestTrackingBundle.getFloat("currentAltitude").toDouble()
            bikeUsage.date = latestTrackingBundle.getFloat("date").toString()
            bikeUsage.duration = latestTrackingBundle.getDouble("elapsedTime").toString()
            bikeUsage.distance = latestTrackingBundle.getDouble("elapsedDistance")
            bikeUsage.avgSpeed = latestTrackingBundle.getDouble("avgSpeed")
            bikeUsage.time = latestTrackingBundle.getDouble("startTime").toString()

            var tempArr = ArrayList<LatLng>()

            if(latestTrackingBundle.getParcelableArrayList<Location>("locations") != null) {
                for (element in latestTrackingBundle.getParcelableArrayList<Location>("locations") as ArrayList<Location>) {
                    tempArr.add(LatLng(element.latitude, element.longitude))
                }
            }
            else{
                tempArr.add(LatLng(0.0, 0.0))
            }
            bikeUsage.locationList = tempArr

            viewModel.insertBikeUsage(bikeUsage)
        }

        Toast.makeText(
            requireActivity(),
            "Saved biking event",
            Toast.LENGTH_SHORT
        ).show()

        removeCurrentBikeUsage()
    }

    private fun drawCurrentBikeUsage(){
        // TODO: drawing on map the current service reading
    }

    private fun removeCurrentBikeUsage(){
        latestTrackingBundle.clear()
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
            poiMarkers.add(mMap.addMarker(poiMarkerOption.title(favourite.name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .position(getLatLang(favourite)))!! )

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
            mClusterManagerBikeRack.addItems(bikeRacks)
            mClusterManagerBikeRack.cluster()
        }
    }

    private fun addRecycleCenter() {
        activity?.runOnUiThread{
            mClusterManagerRecycleCenter.addItems(recycleCenter)
            mClusterManagerRecycleCenter.cluster()
        }
    }
    private fun addBikeShop() {
        activity?.runOnUiThread{
            mClusterManagerBikeShop.addItems(bikeShop)
            mClusterManagerBikeShop.cluster()
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
            val locationTitle = "${i.javaClass.getMethod("getStreetNumber").invoke(i)} ${i.javaClass.getMethod("getStreetName").invoke(i)}"
            var skytrainStationName = ""
            if(i.javaClass.getMethod("getSkytrainStationName").invoke(i).toString().isNotBlank()){
                skytrainStationName = "${i.javaClass.getMethod("getSkytrainStationName").invoke(i)} Station"
            }
            val numberOfRacks = i.javaClass.getMethod("getNumberOfRacks").invoke(i).toString().toInt()
            bikeRacks.add(BikeRack(latLng, locationTitle, skytrainStationName, numberOfRacks))
        }
    }

    private fun recycleCenterList(){
        recycleCenter = ArrayList()
        val bikeRackList = (activity as CoreActivity).recycleCenter
        for (i in bikeRackList) {
            val latLng = LatLng(i.javaClass.getMethod("getLatitude").invoke(i).toString().toDouble(), i.javaClass.getMethod("getLongitude").invoke(i).toString().toDouble())
            val locationTitle = "${i.javaClass.getMethod("getName").invoke(i)}"
            val phoneNumber = "${i.javaClass.getMethod("getPhone").invoke(i)}"
            recycleCenter.add(RecycleCenterCluster(latLng, locationTitle, phoneNumber))



        }
    }

    private fun bikeShopList(){
        bikeShop = ArrayList()
        val bikeRackList = (activity as CoreActivity).bikeShop
        for (i in bikeRackList) {
            val latLng = LatLng(i.javaClass.getMethod("getLatitude").invoke(i).toString().toDouble(), i.javaClass.getMethod("getLongitude").invoke(i).toString().toDouble())
            val locationTitle = "${i.javaClass.getMethod("getName").invoke(i)}"
            val phoneNumber = "${i.javaClass.getMethod("getPhone").invoke(i)}"
            bikeShop.add(BikeShopCluster(latLng, locationTitle, phoneNumber))



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


    override fun onLocationChanged(location: Location) {
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)
        if (!centerMap){
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
                location = locationManager.getLastKnownLocation(provider)!!

                if(location != null){
                    onLocationChanged(location)
                }else{
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