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
import ca.sfu.minerva.databinding.ActivityFavouritePoiBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class FavouritePoiActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener, LocationListener {
    private lateinit var binding: ActivityFavouritePoiBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var mMap: GoogleMap
    private lateinit var poiMarkerOption: MarkerOptions
    private lateinit var poiOptionListView: ListView
    private lateinit var poiListAdapter: PoiListAdapter
    private lateinit var poiMarker: Marker
    private lateinit var locationManager:  LocationManager
    private val optionList: List<String> = listOf("Work", "Favourite", "Home", "School")
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

        prefs = this.getSharedPreferences("change this to in string.xml later", Context.MODE_PRIVATE)
        poiOptionListView = findViewById(R.id.poiListView)
        poiListAdapter = PoiListAdapter(this, optionList)
        poiOptionListView.adapter = poiListAdapter
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

        dropPoiPin("Work")

        poiOptionListView.setOnItemClickListener { parent, view, position, id ->
            when (position) {
                0 -> dropPoiPin(optionList[position])
                1 -> dropPoiPin(optionList[position])
                2 -> dropPoiPin(optionList[position])
                3 -> dropPoiPin(optionList[position])
                4 -> dropPoiPin(optionList[position])
            }

            Toast.makeText(
                this,
                "Viewing location of ${optionList[position]}",
                Toast.LENGTH_SHORT
            ).show()
        }

    }


    fun dropPoiPin(title: String){
        val lat = prefs.getFloat(title+"_lat", defaultLat.toFloat()).toDouble()
        val lng = prefs.getFloat(title+"_lng", defaultLang.toFloat()).toDouble()
        val poiLatLng = LatLng(lat, lng)
        poiMarkerOption.title(title).position(poiLatLng)

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
            val builder = AlertDialog.Builder(it)
            var posList: Int = 0
            val editor = prefs.edit()

            val input = EditText(it)
            input.inputType = InputType.TYPE_CLASS_TEXT
            input.hint = "Enter comment"
            builder.setView(input)

            builder.setSingleChoiceItems(optionList.toTypedArray(), -1) { dialog, pos ->
                posList = pos
                Toast.makeText(this, "Update", Toast.LENGTH_SHORT).show()
            }
                .setPositiveButton("Ok") { dialog, pos ->
                    editor.putFloat(optionList[posList]+"_lat", latLng.latitude.toFloat())
                    editor.putFloat(optionList[posList]+"_lng", latLng.longitude.toFloat())

                    var descriptionResult: String = "None"
                    if(input.text.toString() != "")
                        descriptionResult = input.text.toString()
                    editor.putString(optionList[posList]+"_description", descriptionResult)
                    editor.apply()

                    dropPoiPin(optionList[posList])
                    Toast.makeText(this, "Updating ${optionList[posList]}", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel"){dialog, pos ->
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

}

class PoiListAdapter(private val context: Context, private var optionList: List<String>): BaseAdapter(){
    private var prefs: SharedPreferences = context.getSharedPreferences("change this to in string.xml later", Context.MODE_PRIVATE)

    override fun getCount(): Int {
        return optionList.size
    }

    override fun getItem(position: Int): Any {
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
        textTop.text = optionList[position]
        val description = "Description: ${prefs.getString(optionList[position]+"_description", "None")}"
        textMiddle.text = description

        val lat = prefs.getFloat(optionList[position]+"_lat", 49.2781F).toDouble()
        val lng = prefs.getFloat(optionList[position]+"_lng", -122.9199F).toDouble()
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(lat, lng,1)
        var addressResult: String = "Could not define"
        if(addresses.isNotEmpty())
            addressResult = addresses[0].getAddressLine(0).toString()

        textBottom.text = "Address: $addressResult"

        return view
    }

}