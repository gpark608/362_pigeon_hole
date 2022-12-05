package ca.sfu.minerva

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.appbar.MaterialToolbar

class SettingsActivity : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar

    companion object {
        const val PROJECT_529_KEY = "project529"
        const val CHANGE_BACKGROUND_KEY = "background"
        const val MEASURE_KEY = "units_of_measure"
        const val WEIGHT_KEY = "units_of_weight"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }

        // load toolbar before setup
        toolbar = this.findViewById(R.id.toolbar_settings)
        setupToolBar()
    }

    private fun setupToolBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private lateinit var measureUnitPreference: ListPreference
        private lateinit var weightUnitPreference: ListPreference
        private lateinit var btnProject529: Preference
        private lateinit var btnChangeBackground: Preference

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            measureUnitPreference = findPreference(MEASURE_KEY)!!
            measureUnitPreference.setValueIndex(0);

            weightUnitPreference = findPreference(WEIGHT_KEY)!!
            weightUnitPreference.setValueIndex(0);

            btnProject529 = findPreference(PROJECT_529_KEY)!!
            btnProject529.setOnPreferenceClickListener {
                val intent = Intent(requireContext(), BikeRegisterActivity::class.java)
                startActivity(intent)
                true
            }

            btnChangeBackground = findPreference(CHANGE_BACKGROUND_KEY)!!
            btnChangeBackground.setOnPreferenceClickListener {
                println("Place Holder")
                true
            }
        }
    }
}