package ca.sfu.minerva

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailET: EditText
    private lateinit var passwordET: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var btnContinue: TextView

    private val requestCode = 200
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission(this)
        emailET = findViewById(R.id.editTextEmail)
        passwordET = findViewById(R.id.editTextPassword)
        btnLogin = findViewById(R.id.buttonLogin)
        btnRegister = findViewById(R.id.buttonRegister)
        btnContinue = findViewById(R.id.textViewGuest)

        auth = Firebase.auth

        btnLogin.setOnClickListener {
//            login()
            val intent = Intent(this, BikeMapActivity::class.java)
            startActivity(intent)
        }

        btnRegister.setOnClickListener {
            register()
        }

        btnContinue.setOnClickListener {
            startMapsActivity()
        }
    }

    private fun login() {
//        val email = emailET.text.toString().trim()
//        val pass = passwordET.text.toString().trim()
        val email = "admin@gmail.com"
        val pass = "qwerty123"
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                startMapsActivity()
            } else
                Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show()
        }
    }

    private fun register() {
        val email = emailET.text.toString().trim()
        val pass = passwordET.text.toString().trim()

        if (email.isBlank() || pass.isBlank()) {
            Toast.makeText(this, "Email and/or Password is blank", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show()
                startMapsActivity()
            } else {
                Toast.makeText(this, "Registration Failed. Please try again later.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermission(activity: Activity){
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION),
                requestCode)

        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == requestCode &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED &&
            grantResults[1] == PackageManager.PERMISSION_GRANTED &&
            grantResults[2] == PackageManager.PERMISSION_GRANTED &&
            grantResults[3] == PackageManager.PERMISSION_GRANTED){
            println("debug: permission checked")
        }else{
            println("debug: permission denied")
            checkPermission(this)
        }
    }
    private fun startMapsActivity () {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }
}