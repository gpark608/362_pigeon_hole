package ca.sfu.minerva.ui.profile

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import ca.sfu.minerva.CheckBoxDialog
import ca.sfu.minerva.DatePickerDialog
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
    private lateinit var editTextGender: EditText
    private lateinit var editTextWeight: EditText
    private lateinit var editTextBirthdate: EditText

    private val provinces = arrayOf("AB", "BC", "MB", "NB", "NL", "NS", "NT", "NU", "ON", "PE", "QC", "SK", "YT")
    private val gender = arrayOf("M", "F", "Prefer Not To Say")

    companion object {
        const val FIRST_NAME = "first_name"
        const val LAST_NAME = "last_name"
        const val CITY = "city"
        const val PROVINCE = "province"
        const val BIRTHDATE = "birthdate"
        const val GENDER = "gender"
        const val WEIGHT = "weight"
        const val EMAIL = "email"
        const val PASSWORD = "password"
        const val CHOICES = "choices"
        const val TITLE = "title"
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
        editTextProvince = this.findViewById(R.id.edit_text_province)
        editTextGender = this.findViewById(R.id.edit_text_gender)
        editTextWeight = this.findViewById(R.id.edit_text_weight)
        editTextBirthdate = this.findViewById(R.id.edit_text_birthdate)

        loadUserData()

        btnSave.setOnClickListener {
            onClickSave()
        }

        editTextProvince.setOnClickListener {
            onClickCheckBox(it)
        }
        editTextGender.setOnClickListener {
            onClickCheckBox(it)
        }
        editTextBirthdate.setOnClickListener {
            onClickDatePickerDialog()
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

    private fun onClickCheckBox(view: View) {
        val bundle = Bundle()
        if (view == editTextProvince) {
            bundle.putStringArray(CHOICES, provinces)
            bundle.putString(TITLE, PROVINCE)
        } else if (view == editTextGender) {
            bundle.putStringArray(CHOICES, gender)
            bundle.putString(TITLE, GENDER)
        }
        val checkBoxDialog = CheckBoxDialog()
        checkBoxDialog.arguments = bundle
        checkBoxDialog.show(supportFragmentManager, "")
    }

    private fun onClickDatePickerDialog() {
        val datePickerDialog = DatePickerDialog()
        datePickerDialog.show(supportFragmentManager, "")
    }

    private fun loadUserData() {
        val firstName = sharedPreferences.getString(FIRST_NAME, "")
        val lastName = sharedPreferences.getString(LAST_NAME, "")
        val city = sharedPreferences.getString(CITY, "")
        val province = sharedPreferences.getString(PROVINCE, "")
        val gender = sharedPreferences.getString(GENDER, "")
        val weight = sharedPreferences.getString(WEIGHT, "")
        val birthdate = sharedPreferences.getString(BIRTHDATE, "")

        editTextFirstName.setText(firstName)
        editTextLastName.setText(lastName)
        editTextCity.setText(city)
        editTextProvince.setText(province)
        editTextGender.setText(gender)
        editTextWeight.setText(weight)
        editTextBirthdate.setText(birthdate)
    }

    private fun saveUserData() {
        val editor = sharedPreferences.edit()

        val firstName = editTextFirstName.text.toString()
        val lastName = editTextLastName.text.toString()
        val city = editTextCity.text.toString()
        val province = editTextProvince.text.toString()
        val gender = editTextGender.text.toString()
        val weight = editTextWeight.text.toString()
        val birthdate = editTextBirthdate.text.toString()

        editor.putString(FIRST_NAME, firstName)
        editor.putString(LAST_NAME, lastName)
        editor.putString(CITY, city)
        editor.putString(PROVINCE, province)
        editor.putString(GENDER, gender)
        editor.putString(WEIGHT, weight)
        editor.putString(BIRTHDATE, birthdate)

        editor.apply()
    }
}