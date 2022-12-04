package ca.sfu.minerva.ui.home
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ca.sfu.minerva.BuildConfig
import ca.sfu.minerva.R
import ca.sfu.minerva.databinding.FragmentHomeBinding
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
class HomeFragment : Fragment(), LocationListener {
    private var _binding: FragmentHomeBinding? = null
    private lateinit var tvTemperature: TextView
    private lateinit var tvCity: TextView
    private lateinit var tvWeekday: TextView
    private lateinit var tvFullDate: TextView
    private lateinit var ivWeatherIcon: ImageView
    private lateinit var container: LinearLayout
    private lateinit var listview: ListView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var eventList: ArrayList<Event>
    private lateinit var adapter: EventAdapter
    private lateinit var locationManager: LocationManager
    private val apiKey: String = BuildConfig.W_API_KEY

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        firestore = Firebase.firestore
        eventList = ArrayList()
        initializeTextViews()
        checkPermission()
        CoroutineScope(Dispatchers.IO).launch{
            if (container != null) {
                getEventsAPICall(container.context)
            }
        }
        return root
    }
    private fun initializeTextViews() {
        tvTemperature = binding.tvTemperature
        tvCity = binding.tvCity
        tvWeekday = binding.tvWeekday
        tvWeekday.text = getWeekday()
        tvFullDate = binding.tvFullDate
        tvFullDate.text = getFullDate()
        ivWeatherIcon = binding.ivWeatherIcon
        container = binding.container
    }

    private suspend fun getWeatherAPICall(latLng: LatLng){
        withContext(Dispatchers.IO){
            val client = HttpClient {
                install(JsonFeature) {
                    serializer = KotlinxSerializer()
                }
            }
            val WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?" +
                    "lat=" + latLng.latitude +
                    "&lon=" + latLng.longitude +
                    "&units=metric" +
                    "&APPID=" + apiKey
            val response: String = client.get(WEATHER_URL)
            println("WHY $latLng")
            //GET WEATHER DESCRIPTION
            val jsonObj = JSONObject(response)
            val weather = jsonObj.getJSONArray("weather")
            val weatherObj = weather.getJSONObject(0)
            val description = weatherObj.getString("main")
            updateWeatherIcon(ivWeatherIcon, description);
            //GET TEMPERATURE
            val main = jsonObj.getJSONObject("main")
            val temp = main.getDouble("temp").toInt().toString() + "°"
            //GET CITY NAME
            val city = jsonObj.getString("name")
            updateTextView(tvTemperature, "$description $temp $city")
            updateTextView(tvTemperature, temp)
            updateTextView(tvCity, city)
        }
    }
    private suspend fun getEventsAPICall(context: Context) {
        withContext(Dispatchers.IO){
            firestore.collection("events")
                .orderBy("startTime")
                .limit(10)
                .get().addOnSuccessListener { events ->
                    for (event in events) {
                        val id = event["id"].toString()
                        val title = event["title"].toString()
                        val description = event["description"].toString()
                        val startTime = (event["startTime"] as Timestamp).toDate()
                        val endTime = (event["endTime"] as Timestamp).toDate()
                        val g = event["geopoint"] as GeoPoint
                        val geopoint = LatLng(g.latitude, g.longitude);
                        val location = event["location"].toString()
                        var allDay = false
                        if(event["allDay"] == 1){
                            allDay = true
                        }
                        eventList.add(Event(id, title, description, startTime, endTime, geopoint, location, allDay))
                    }
                    adapter = EventAdapter(context, eventList)
                    listview = binding.listview
                    listview.adapter = adapter
                }
        }
    }
    private suspend fun updateTextView(tv: TextView, input: String){
        withContext(Dispatchers.Main){
            tv.text = input
        }
    }
    private suspend fun updateWeatherIcon(view: ImageView, value: String) {
        withContext(Dispatchers.Main){
            when (value) {
                "Clouds" -> view.setImageResource(R.drawable.weather_clouds)
                "Clear" -> view.setImageResource(R.drawable.weather_clear)
                "Drizzle" -> view.setImageResource(R.drawable.weather_drizzle)
                "Rain" -> view.setImageResource(R.drawable.weather_rain)
                "Snow" -> view.setImageResource(R.drawable.weather_snow)
                "Thunderstorm" -> view.setImageResource(R.drawable.weather_thunderstorm)
                else -> view.setImageResource(R.drawable.weather_unknown)
            }
        }
    }
    private fun getWeekday(): String? {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        val d = Date()
        return sdf.format(d)
    }
    private fun getFullDate(): String? {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val d = Date()
        return sdf.format(d)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onLocationChanged(location: Location) {
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)
        CoroutineScope(Dispatchers.IO).launch{
            getWeatherAPICall(latLng)
        }
    }
    /*
    Verifies that location permissions were granted then calls initLocationManager
     */
    private fun checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return
        if (activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) } != PackageManager.PERMISSION_GRANTED){
            activity?.let { ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0) }
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
                    locationManager.requestLocationUpdates(
                        provider,
                        10*1000*60,
                        0f,
                        this
                    )
                }
            }
        } catch (e: SecurityException) {
        }
    }
}