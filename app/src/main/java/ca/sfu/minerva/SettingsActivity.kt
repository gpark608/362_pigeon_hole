package ca.sfu.minerva

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {

    companion object {
        const val PROJECT_529_KEY = "project529"
        const val CHANGE_BACKGROUND_KEY = "background"
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private lateinit var btnProject529: Preference
        private lateinit var btnChangeBackground: Preference

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

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