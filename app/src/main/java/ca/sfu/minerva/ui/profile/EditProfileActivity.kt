package ca.sfu.minerva.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import ca.sfu.minerva.*
import ca.sfu.minerva.util.getBitmap
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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
    private lateinit var editTextFieldWeight: TextInputLayout
    private lateinit var editTextBirthdate: EditText
    private lateinit var imageViewProfilePicture: ShapeableImageView
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<Intent>
    private lateinit var profilePicturePath: String
    private lateinit var profilePictureUri: Uri
    private lateinit var galleryAndCameraDialog: GalleryAndCameraDialog
    private lateinit var galleryAndCameraDialogViewModel: GalleryAndCameraDialogViewModel
    private lateinit var defaultProfilePicture: Drawable

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
        const val CHOICES = "choices"
        const val TITLE = "title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // load toolbar before setup
        toolbar = findViewById(R.id.materialToolbar)
        setupToolBar()

        defaultProfilePicture = ContextCompat.getDrawable(this, R.drawable.default_profile_picture)!!
        imageViewProfilePicture = findViewById(R.id.edit_image_profile_picture)

        btnSave = toolbar.findViewById(R.id.button_save)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        editTextFirstName = this.findViewById(R.id.edit_text_first_name)
        editTextLastName = this.findViewById(R.id.edit_text_last_name)
        editTextCity = this.findViewById(R.id.edit_text_city)
        editTextProvince = this.findViewById(R.id.edit_text_province)
        editTextGender = this.findViewById(R.id.edit_text_gender)
        editTextWeight = this.findViewById(R.id.edit_text_weight)
        editTextFieldWeight = this.findViewById(R.id.text_field_weight)
        editTextBirthdate = this.findViewById(R.id.edit_text_birthdate)

        loadUserData()
        setWeightUnits()

        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                setProfilePicture()
            }
        }

        galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent: Intent? = result.data
                if (intent != null) {
                    val imageUri: Uri? = intent.data
                    profilePictureUri = imageUri!!
                    setProfilePicture()
                }
            }
        }

        galleryAndCameraDialogViewModel = ViewModelProvider(this)[GalleryAndCameraDialogViewModel::class.java]

        galleryAndCameraDialogViewModel.openCamera.observe(this) {
            if (it == true) {
                launchCamera()
            } else {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                intent.putExtra(Intent.ACTION_PICK, true)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.type = "image/*"
                galleryResult.launch(intent)
            }
            galleryAndCameraDialog.dismiss()
        }

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
        imageViewProfilePicture.setOnClickListener {
            onClickEdit()
        }
    }

    private fun setWeightUnits() {
        val unit = sharedPreferences.getString("units_of_weight", "Kg")

        if (unit == "Lbs") {
            editTextFieldWeight.hint = resources.getString(R.string.weight_lb)
        } else {
            editTextFieldWeight.hint = resources.getString(R.string.weight_kg)
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
        galleryAddPic()
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun onClickEdit() {
        checkPermissions()
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            timeStamp,
            ".jpg",
            storageDir
        ).apply {
            profilePicturePath = absolutePath
        }
    }

    private fun setProfilePicture() {
        try {
            val profilePictureBitmap = getBitmap(this, profilePictureUri, 125, 125)
            imageViewProfilePicture.scaleType = ImageView.ScaleType.CENTER_CROP
            imageViewProfilePicture.setImageBitmap(profilePictureBitmap)
        } catch (e: Exception) {
            println(e)
            imageViewProfilePicture.setImageDrawable(defaultProfilePicture)
        }
    }

    private fun galleryAddPic() {
        if (this::profilePicturePath.isInitialized) {
            val f = File(profilePicturePath)
            MediaScannerConnection.scanFile(this, arrayOf(f.toString()), null, null)
        }
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
        val profilePicturePathString = sharedPreferences.getString("profilePicturePath", null)
        val profilePictureUriString = sharedPreferences.getString("profilePictureUri", null)

        if (profilePicturePathString != null) {
            profilePicturePath = profilePicturePathString
        }

        if (profilePictureUriString != null) {
            profilePictureUri = Uri.parse(profilePictureUriString)
            setProfilePicture()
        }

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

        if (this::profilePicturePath.isInitialized) {
            editor.putString("profilePicturePath", profilePicturePath)
        }

        if (this::profilePictureUri.isInitialized) {
            editor.putString("profilePictureUri", profilePictureUri.toString())
        }

        editor.apply()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        }
    }

    private fun checkPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), 0)
        } else showProfilePictureDialog()
    }

    private fun launchCamera() {
        val photoFile = createImageFile()
        photoFile.also { file ->
            profilePictureUri = FileProvider.getUriForFile(
                this,
                "Minerva",
                file
            )
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, profilePictureUri)
            cameraResult.launch(intent)
        }
    }

    private fun showProfilePictureDialog() {
        galleryAndCameraDialog = GalleryAndCameraDialog()
        galleryAndCameraDialog.show(supportFragmentManager, "")
    }
}