package ca.sfu.minerva

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import ca.sfu.minerva.util.Helper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
//import io.ktor.network.sockets.*
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailET: EditText
    private lateinit var passwordET: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var btnContinue: TextView

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor:SharedPreferences.Editor

    private val requestCode = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermission(this)
        if(!getConnectionType()){
            setContentView(R.layout.activity_main)
            internetAlert()
        }

        auth = Firebase.auth
        sharedPreferences = this.getSharedPreferences("LoginInfo", MODE_PRIVATE)
        editor = sharedPreferences.edit()
        val email = sharedPreferences.getString(Helper.EMAIL, "")!!
        val pass = sharedPreferences.getString(Helper.PASSWORD, "")!!
        login(email, pass)
    }

    private fun internetAlert(){
        val alertDialogBuilder: androidx.appcompat.app.AlertDialog.Builder = androidx.appcompat.app.AlertDialog.Builder(this)
        alertDialogBuilder.setMessage("Internet connection is required. Check your Wifi or Mobile Data")
        alertDialogBuilder.setTitle("Connection Failed")
        alertDialogBuilder.setNegativeButton(
            "ok"
        ) { _, _ ->}
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
    private fun login(email: String, pass: String) {
        if (email.isNotEmpty() && pass.isNotEmpty()) {
            auth(email, pass)
        }else{
            loginRequired()

        }
    }

    private fun loginRequired(){
        setContentView(R.layout.activity_main)
        emailET = findViewById(R.id.editTextEmail)
        passwordET = findViewById(R.id.editTextPassword)
        btnLogin = findViewById(R.id.buttonLogin)
        btnRegister = findViewById(R.id.buttonRegister)
        btnContinue = findViewById(R.id.textViewGuest)
        btnLogin.setOnClickListener {
            if(!getConnectionType()){
                internetAlert()
            }else{
                val email = emailET.text.toString().trim()
                val pass = passwordET.text.toString().trim()
                auth(email, pass)
            }

        }

        btnRegister.setOnClickListener {
            if(!getConnectionType()){
                internetAlert()
            }else{
                register()
            }

        }

        btnContinue.setOnClickListener {
            if(!getConnectionType()){
                internetAlert()
            }else{
                startCoreActivity()
            }

        }
    }
    private fun auth(email: String, pass: String){
        startCoreActivity()
        if(email.isNotEmpty() && pass.isNotEmpty()){
            auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    editor.putString(Helper.EMAIL, email)
                    editor.putString(Helper.PASSWORD, pass)
                    editor.apply()
                    startCoreActivity()
                }else{
                    Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
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
                startCoreActivity()
            } else {
                Toast.makeText(this, "Registration Failed. Please try again later.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermission(activity: Activity){
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE),
                requestCode)

        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 200 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED &&
            grantResults[1] == PackageManager.PERMISSION_GRANTED &&
            grantResults[2] == PackageManager.PERMISSION_GRANTED &&
            grantResults[3] == PackageManager.PERMISSION_GRANTED &&
            grantResults[4] == PackageManager.PERMISSION_GRANTED){

            println("debug: permission checked")
        }else{
            println("debug: permission denied")
            checkPermission(this)
        }
    }
    private fun startCoreActivity () {

        val intent = Intent(this, CoreActivity::class.java)
        startActivity(intent)

        finish()
    }





    @SuppressLint("MissingPermission")
    fun getConnectionType(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    if (hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||hasTransport(NetworkCapabilities.TRANSPORT_VPN) ) {
                        return true
                    }
                }
            }
        } else {
            cm?.run {
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI || type == ConnectivityManager.TYPE_MOBILE || type == ConnectivityManager.TYPE_VPN) {
                        return true
                    }
                }
            }
        }

        return false
    }

}