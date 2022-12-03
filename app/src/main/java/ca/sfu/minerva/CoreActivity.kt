package ca.sfu.minerva

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import ca.sfu.minerva.data.BikeTrail
import ca.sfu.minerva.data.BikeRack
import ca.sfu.minerva.data.BikeTheft
import ca.sfu.minerva.databinding.ActivityCoreBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream

class CoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCoreBinding
    var bikeTrail: List<Any> = arrayListOf()
    var bikeTheft: List<Any> = arrayListOf()
    var bikeRack: List<Any> = arrayListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_core)
        navView.setupWithNavController(navController)


        CoroutineScope(Dispatchers.IO).launch {
            bikeTrail = readCsv(resources.openRawResource(R.raw.bike_trail))
        }.invokeOnCompletion {}
        CoroutineScope(Dispatchers.IO).launch {
            bikeTheft = readCsv(resources.openRawResource(R.raw.bike_theft))
        }.invokeOnCompletion {}

        CoroutineScope(Dispatchers.IO).launch {
            bikeRack = readCsv(resources.openRawResource(R.raw.bike_rack))
        }.invokeOnCompletion{}








    }
    private fun readCsv(inputStream: InputStream): List<Any> {
        val reader = inputStream.bufferedReader()
        val header = reader.readLine()
        return reader.lineSequence()
            .filter { it.isNotBlank() }
            .map {
                val columns = it.split(',', ignoreCase = false)
                when(columns.size){

                    6 -> BikeTheft(columns[0].toInt(), columns[1].toInt(), columns[2], columns[3].toDouble(), columns[4].toDouble(), columns[5])
                    8 -> BikeRack(columns[0].toInt(), columns[1].toDouble(), columns[2].toDouble(), columns[3].toInt(), columns[4], columns[5], columns[6], columns[7])
                    else -> BikeTrail(columns[0].toInt(), columns[1], columns.subList(2, columns.size).joinToString() )

                }
            }.toList()
    }



}