package ca.sfu.minerva

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class BikeRegisterActivity : AppCompatActivity() {
    private lateinit var btnRegister: Button
    private lateinit var btnLater: Button
    private lateinit var sharedPreferences: SharedPreferences
    private var bikeIsRegistered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bike_register)

        sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        bikeIsRegistered = sharedPreferences.getBoolean("registered", false)

        btnRegister = findViewById(R.id.buttonRegisterProject529)
        btnLater = findViewById(R.id.buttonLater)

        btnRegister.setOnClickListener {
            onClickRegister()
        }

        btnLater.setOnClickListener {
            onClickLater()
        }
    }

    private fun onClickRegister() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("registered", true)
        editor.apply()
        val url = "https://project529.com/garage/users/sign_in"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
        finish()
    }

    private fun onClickLater() {
        finish()
    }
}