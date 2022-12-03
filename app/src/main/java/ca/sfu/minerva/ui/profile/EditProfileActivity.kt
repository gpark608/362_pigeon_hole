package ca.sfu.minerva.ui.profile

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import ca.sfu.minerva.R
import com.google.android.material.appbar.MaterialToolbar

class EditProfileActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var btnSave: TextView
    private lateinit var editTextFirstName: EditText
    private lateinit var editTextLastName: EditText
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        const val FIRST_NAME = "first_name"
        const val LAST_NAME = "last_name"
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

        loadUserData()

        btnSave.setOnClickListener {
            onClickSave()
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

    private fun loadUserData() {
        val firstName = sharedPreferences.getString(FIRST_NAME, "")
        val lastName = sharedPreferences.getString(LAST_NAME, "")

        editTextFirstName.setText(firstName)
        editTextLastName.setText(lastName)

    }

    private fun saveUserData() {
        val editor = sharedPreferences.edit()

        val firstName = editTextFirstName.text.toString()
        val lastName = editTextLastName.text.toString()

        editor.putString(FIRST_NAME, firstName)
        editor.putString(LAST_NAME, lastName)

        editor.apply()
    }
}