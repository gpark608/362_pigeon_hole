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
    private val optionList: List<String> = listOf("Work", "Favourite", "Destination", "Home", "School")
    private var mapReady: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
println("debug: created")
        binding = ActivityFavouritePoiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = this.getSharedPreferences("change this to in string.xml later", Context.MODE_PRIVATE)
        poiOptionListView = findViewById(R.id.poiListView)
        poiListAdapter = PoiListAdapter(this, optionList)
        poiOptionListView.adapter = poiListAdapter
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        poiMarkerOption = MarkerOptions()
        mMap.setOnMapLongClickListener(this)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(49.2827, 123.1207), 17f)
        mMap.animateCamera(cameraUpdate)
        mapReady = true
        Log.d("list click", "map is ready")

        initLocationManager()

        poiOptionListView.setOnItemClickListener { parent, view, position, id ->
            Log.d("list click", "performing list click")
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
        val lat = prefs.getFloat(title+"_lat", 0F).toDouble()
        val lng = prefs.getFloat(title+"_lng", 0F).toDouble()
        val poiLatLng = LatLng(lat, lng)
        poiMarkerOption.title(title).position(poiLatLng)

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(poiLatLng, 17f)
        mMap.animateCamera(cameraUpdate)
        if(poiMarker != null)
            poiMarker.remove()
        poiMarker = mMap.addMarker(poiMarkerOption)!!
        println("debug: dropping the pin")
    }

    override fun onMapLongClick(latLng: LatLng) {
        showAlertDialog(latLng)
        Log.d("long click", "performing the long click")
    }

    fun showAlertDialog(latLng: LatLng) {
        val alertDialog: AlertDialog? = this.let {
            val builder = AlertDialog.Builder(it)

            builder.setSingleChoiceItems(optionList.toTypedArray(), 0) { dialog, pos ->
                Toast.makeText(this, "Update", Toast.LENGTH_SHORT).show()
            }
                .setPositiveButton("Ok") { dialog, pos ->
                    prefs.edit().putFloat(optionList[pos]+"_lat", latLng.latitude.toFloat())
                    prefs.edit().putFloat(optionList[pos]+"_lng", latLng.longitude.toFloat())
                    prefs.edit().apply()
                    poiListAdapter.notifyDataSetChanged()
                    dropPoiPin(optionList[pos])
                    Toast.makeText(this, "Updating ${optionList[pos]}", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel"){dialog, pos ->
                    dialog.dismiss()
                }
                .setTitle("Save location as:")

            val input = EditText(this)
            input.setHint("Enter comment")
            builder.setView(input)

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
        // Not needed for this
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
        textMiddle.text = "Description: ${prefs.getString(optionList[position]+"_description", "None")}"

        val lat = prefs.getFloat(optionList[position]+"_lat", 0F).toDouble()
        val lng = prefs.getFloat(optionList[position]+"_lng", 0F).toDouble()
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(lat, lng,1)
        var addressResult: String = "None"
        if(addresses.isNotEmpty())
            addressResult = addresses[0].getAddressLine(0).toString()

        textBottom.text = "Address: $addressResult"
        return view
    }

}