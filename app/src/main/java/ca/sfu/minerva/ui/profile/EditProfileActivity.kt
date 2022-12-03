package ca.sfu.minerva.ui.profile

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import ca.sfu.minerva.CheckBoxDialog
import ca.sfu.minerva.R
import com.google.android.material.appbar.MaterialToolbar

class EditProfileActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var toolbar: MaterialToolbar
    private lateinit var btnSave: TextView
    private lateinit var editTextFirstName: EditText
    private lateinit var editTextLastName: EditText
    private lateinit var editTextCity: EditText
    private lateinit var editTextProvince: EditText
    private lateinit var textInputProvince: LinearLayout

    companion object {
        const val FIRST_NAME = "first_name"
        const val LAST_NAME = "last_name"
        const val CITY = "city"
        const val PROVINCE = "province"
        const val BIRTHDAY = "birthday"
        const val GENDER = "gender"
        const val WEIGHT = "weight"
        const val EMAIL = "email"
        const val PASSWORD = "password"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // load toolbar before setup
        toolbar = findViewById(R.id.materialToolbar)
        setupToolBar()

        btnSave = toolbar.findViewById(R.id.button_save)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        editTextFirstName = this.findViewById(R.id.edit_text_first_name)
        editTextLastName = this.findViewById(R.id.edit_text_last_name)
        editTextCity = this.findViewById(R.id.edit_text_city)
        textInputProvince = this.findViewById(R.id.text_field_province)
        editTextProvince = this.findViewById(R.id.edit_text_province)

        loadUserData()

        btnSave.setOnClickListener {
            onClickSave()
        }

        editTextProvince.setOnClickListener {
            onClickProvince()
        }
    }

    private fun setupToolBar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onClickSave() {
        saveUserData()
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun onClickProvince() {
        val provinceDialog = CheckBoxDialog()
        provinceDialog.show(supportFragmentManager, "")
    }

    private fun loadUserData() {
        val firstName = sharedPreferences.getString(FIRST_NAME, "")
        val lastName = sharedPreferences.getString(LAST_NAME, "")
        val city = sharedPreferences.getString(CITY, "")
        val province = sharedPreferences.getString(PROVINCE, "")

        editTextFirstName.setText(firstName)
        editTextLastName.setText(lastName)
        editTextCity.setText(city)
        editTextProvince.setText(province)
    }

    private fun saveUserData() {
        val editor = sharedPreferences.edit()

        val firstName = editTextFirstName.text.toString()
        val lastName = editTextLastName.text.toString()
        val city = editTextCity.text.toString()
        val province = editTextProvince.text.toString()

        editor.putString(FIRST_NAME, firstName)
        editor.putString(LAST_NAME, lastName)
        editor.putString(CITY, city)
        editor.putString(PROVINCE, province)

        editor.apply()
    }
}