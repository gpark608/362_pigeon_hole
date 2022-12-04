package ca.sfu.minerva.ui.profile

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import ca.sfu.minerva.MainActivity
import ca.sfu.minerva.R
import ca.sfu.minerva.databinding.FragmentProfileBinding
import ca.sfu.minerva.util.Helper
import ca.sfu.minerva.util.getBitmap
import com.google.android.material.imageview.ShapeableImageView

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
    private lateinit var profilePicturePath: String
    private lateinit var profilePictureUri: Uri
    private lateinit var defaultProfilePicture: Drawable

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

        btnLogOut = root.findViewById(R.id.text_logout)
        btnLogOut.setOnClickListener { onClickLogOut() }

        return root
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadUserData() {
        val firstName = sharedPreferences.getString(EditProfileActivity.FIRST_NAME, "")
        val lastName = sharedPreferences.getString(EditProfileActivity.LAST_NAME, "")
        val profilePicturePathString = sharedPreferences.getString("profilePicturePath", null)
        val profilePictureUriString = sharedPreferences.getString("profilePictureUri", null)

        if (profilePicturePathString != null) {
            profilePicturePath = profilePicturePathString
        }

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

    private fun onClickLogOut() {
        sharedPreferences = requireActivity().getSharedPreferences("LoginInfo", AppCompatActivity.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        editor.putString(Helper.EMAIL, "")
        editor.putString(Helper.PASSWORD, "")
        editor.apply()
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
    }
}