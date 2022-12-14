package ca.sfu.minerva.ui.profile

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import ca.sfu.minerva.MainActivity
import ca.sfu.minerva.R
import ca.sfu.minerva.SettingsActivity
import ca.sfu.minerva.database.*
import ca.sfu.minerva.databinding.FragmentProfileBinding
import ca.sfu.minerva.util.Helper
import ca.sfu.minerva.util.getBitmap
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class ProfileFragment : Fragment() {
    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!
    private var _binding: FragmentProfileBinding? = null

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var imageViewProfilePicture: ShapeableImageView
    private lateinit var textFullName: TextView
    private lateinit var btnLogOut: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnSettings: TextView
    private lateinit var profilePictureUri: Uri
    private lateinit var defaultProfilePicture: Drawable
    private lateinit var averageSpeedText: TextView
    private lateinit var totalDistanceText: TextView
    private lateinit var topSpeedText: TextView
    private lateinit var totalCaloriesText: TextView
    private lateinit var bikingPoints: TextView

    private lateinit var database: MinervaDatabase
    private lateinit var databaseDao: MinervaDatabaseDao
    private lateinit var repository: MinervaRepository
    private lateinit var viewModelFactory: MinervaViewModelFactory
    private lateinit var viewModel: MinervaViewModel

    private var eventCount: Int = 0
    private var avgSpeed = 0F
    private var totalDist = 0F
    private var topSpeed = 0F
    private var totalCals = 0F
    private var points: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        defaultProfilePicture = ContextCompat.getDrawable(requireContext(), R.drawable.default_profile_picture)!!
        imageViewProfilePicture = root.findViewById(R.id.image_profile_picture)
        textFullName = root.findViewById(R.id.text_full_name)

        loadUserData()

        btnEdit = root.findViewById(R.id.button_edit)
        btnEdit.setOnClickListener { onClickEditProfile() }

        btnSettings = root.findViewById(R.id.text_settings)
        btnSettings.setOnClickListener { onClickSettings() }

        btnLogOut = root.findViewById(R.id.text_logout)
        btnLogOut.setOnClickListener { onClickLogOut() }

        averageSpeedText = root.findViewById(R.id.textAverageSpeedStat)
        totalDistanceText = root.findViewById(R.id.textTotalDistanceStat)
        topSpeedText = root.findViewById(R.id.textTopSpeedText)
        totalCaloriesText = root.findViewById(R.id.textTotalCaloriesBurned)
        bikingPoints = root.findViewById(R.id.textPoints)

        database = MinervaDatabase.getInstance(requireActivity())
        databaseDao = database.MinervaDatabaseDao
        repository = MinervaRepository(databaseDao)
        viewModelFactory = MinervaViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MinervaViewModel::class.java]

        viewModel.allBikeUsageLiveData.observe(requireActivity(), Observer{})
        loadUserStats()

        return root
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
        loadUserStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadUserData() {
        val firstName = sharedPreferences.getString(EditProfileActivity.FIRST_NAME, "")
        val lastName = sharedPreferences.getString(EditProfileActivity.LAST_NAME, "")
        val profilePictureUriString = sharedPreferences.getString("profilePictureUri", null)

        if (profilePictureUriString != null) {
            profilePictureUri = Uri.parse(profilePictureUriString)
            setProfilePicture()
        }

        if (firstName!!.isEmpty() && lastName!!.isEmpty()) {
            textFullName.text = "User"
        } else {
            textFullName.text = String.format("%s %s", firstName, lastName)
        }
    }

    private fun setProfilePicture() {
        try {
            val profilePictureBitmap = getBitmap(requireContext(), profilePictureUri, 125, 125)
            imageViewProfilePicture.scaleType = ImageView.ScaleType.CENTER_CROP
            imageViewProfilePicture.setImageBitmap(profilePictureBitmap)
        } catch (e: Exception) {
            println(e)
            imageViewProfilePicture.setImageDrawable(defaultProfilePicture)
        }
    }

    private fun onClickEditProfile() {
        val intent = Intent(activity, EditProfileActivity::class.java)
        startActivity(intent)
    }

    private fun onClickSettings() {
        val intent = Intent(activity, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun onClickLogOut() {
        sharedPreferences = requireActivity().getSharedPreferences("LoginInfo", AppCompatActivity.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        editor.putString(Helper.EMAIL, "")
        editor.putString(Helper.PASSWORD, "")
        editor.apply()
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
    }

    private fun loadUserStats(){
        CoroutineScope(IO).launch {
            eventCount = viewModel.getBikeUsageCounting()
            avgSpeed = viewModel.getBikeUsageAverageSpeed().toFloat()
            totalDist = viewModel.getBikeUsageTotalDistance().toFloat()
            topSpeed = viewModel.getBikeUsageTopSpeed().toFloat()
            totalCals = viewModel.getBikeUsageTotalDistance().toFloat() * 62
        }

        if(eventCount == 0 || points.toDouble().isNaN() || !points.toDouble().isFinite())
            points = 0
        else
            points = DecimalFormat("#####").format(
                ((avgSpeed * totalDist) / eventCount).toFloat()
            ).toInt()

        averageSpeedText.text = "Average Speed: ${formatStat(avgSpeed, true)}"
        totalDistanceText.text = "Total Distance: ${formatStat(totalDist, false)}"
        topSpeedText.text = "Top Speed: ${formatStat(topSpeed, true)}"
        totalCaloriesText.text = "Total Calories Burned: ${ DecimalFormat("##.##").format(totalCals) }"
        bikingPoints.text = "Biking Points: ${points}"
    }

    private fun formatStat(number: Float, needTime: Boolean): String{
        var result: String =""

        if(sharedPreferences.getString("units_of_measure", "Metric") == "Metric")
            result = DecimalFormat("##.##").format(number).toString() + " km"
        else
            result = DecimalFormat("##.##").format(
                number*0.621371
            ).toString() + " m"

        if(needTime)
            result += "/h"
        return result
    }

}