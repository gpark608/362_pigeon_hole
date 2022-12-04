package ca.sfu.minerva

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.res.Configuration
import android.location.*
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import ca.sfu.minerva.database.*
import ca.sfu.minerva.databinding.ActivityFavouritePoiBinding
import ca.sfu.minerva.ui.map.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*
import kotlin.collections.ArrayList

class FavouritePoiActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener, LocationListener {
    private lateinit var binding: ActivityFavouritePoiBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var mMap: GoogleMap
    private lateinit var poiMarkerOption: MarkerOptions
    private lateinit var poiOptionListView: ListView
    private lateinit var poiListAdapter: PoiListAdapter
    private lateinit var poiMarker: Marker
    private lateinit var locationManager:  LocationManager

    private lateinit var database: MinervaDatabase
    private lateinit var databaseDao: MinervaDatabaseDao
    private lateinit var repository: MinervaRepository
    private lateinit var viewModelFactory: MinervaViewModelFactory
    private lateinit var viewModel: MinervaViewModel
    private lateinit var optionList: ArrayList<FavouriteLocation>
    private var mapReady: Boolean = false
    private val defaultLat: Double = 49.2781
    private val defaultLang: Double = -122.9199

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFavouritePoiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.POIMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //prefs = this.getSharedPreferences(R.string.shared_preference_key.toString(), Context.MODE_PRIVATE)
        optionList = ArrayList()
        poiOptionListView = findViewById(R.id.poiListView)
        poiListAdapter = PoiListAdapter(this, optionList)
        poiOptionListView.adapter = poiListAdapter

        database = MinervaDatabase.getInstance(this)
        databaseDao = database.MinervaDatabaseDao
        repository = MinervaRepository(databaseDao)
        viewModelFactory = MinervaViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MinervaViewModel::class.java)

        viewModel.FavouriteLocationDataLive.observe(this){
            poiListAdapter.replaceList(it)
            poiListAdapter.notifyDataSetChanged()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.setOnMapLongClickListener(this)
        mapReady = true

        initLocationManager()

        // Setting up marker
        poiMarkerOption = MarkerOptions()
        poiMarkerOption.position(LatLng(defaultLat, defaultLang))
        poiMarker = mMap.addMarker(poiMarkerOption)!!
        poiMarker.remove()

        poiOptionListView.setOnItemClickListener { parent, view, position, id ->
            val fav = poiListAdapter.getItem(position)
            dropPoiPin(fav)

            Toast.makeText(
                this,
                "Viewing location of ${fav.name}",
                Toast.LENGTH_SHORT
            ).show()
        }

        if(poiListAdapter.count > 0){
            dropPoiPin(poiListAdapter.getItem(0))
        }
        else{
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(defaultLat, defaultLang), 25f)
            mMap.animateCamera(cameraUpdate)
        }

    }


    fun dropPoiPin(fav: FavouriteLocation){
        val poiLatLng = getLatLang(fav)
        poiMarkerOption.title(fav.name).position(poiLatLng)

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(poiLatLng, 17f)
        mMap.animateCamera(cameraUpdate)
        if(poiMarker != null)
            poiMarker.remove()
        poiMarker = mMap.addMarker(poiMarkerOption)!!
    }

    override fun onMapLongClick(latLng: LatLng) {
        showAlertDialog(latLng)
    }

    fun showAlertDialog(latLng: LatLng) {
        val alertDialog: AlertDialog? = this.let {
            var customLayout: LinearLayout = LinearLayout(this)
            customLayout.orientation = LinearLayout.VERTICAL

            val builder = AlertDialog.Builder(it)
            var posList: Int = 0
            var fav: FavouriteLocation = FavouriteLocation()

            val titleInput = EditText(it)
            titleInput.inputType = InputType.TYPE_CLASS_TEXT
            titleInput.hint = "Enter name of poi"
            customLayout.addView(titleInput)


            val commentInput = EditText(it)
            commentInput.inputType = InputType.TYPE_CLASS_TEXT
            commentInput.hint = "Enter comment"
            customLayout.addView(commentInput)

            builder.setView(customLayout)

            builder.setPositiveButton("Ok") { dialog, pos ->
                fav.name = titleInput.text.toString()

                fav.favLatLng = "${latLng.latitude.toFloat()},${latLng.longitude.toFloat()}"

                var descriptionResult: String = "None"
                if(commentInput.text.toString() != "")
                    descriptionResult = commentInput.text.toString()
                fav.description = descriptionResult

                viewModel.insertFavouriteLocation(fav)
                dropPoiPin(fav)
                Toast.makeText(this, "Saved ${fav.name}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel"){dialog, _ ->
                dialog.dismiss()
            }
            .setTitle("Save location as:")

            builder.create()
            builder.show()
        }
    }

    @SuppressLint("MissingPermission")
    fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_COARSE

            val provider : String? = locationManager.getBestProvider(criteria, true)
            if(provider != null)
                locationManager.requestLocationUpdates(provider, 0L, 0F, this)
        } catch (e: SecurityException) {
        }
    }

    override fun onLocationChanged(location: Location) {
        // Not needed
    }

    fun getLatLang(fav: FavouriteLocation): LatLng{
        var favLatLng = LatLng(
            fav.favLatLng.substringBefore(',').toDouble(),
            fav.favLatLng.substringAfter(',').toDouble()
        )

        return favLatLng
    }

}

class PoiListAdapter(private val context: Context, private var optionList: List<FavouriteLocation>): BaseAdapter(){
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