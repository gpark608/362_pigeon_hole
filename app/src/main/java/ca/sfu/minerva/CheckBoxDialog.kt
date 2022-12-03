package ca.sfu.minerva

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import ca.sfu.minerva.ui.profile.EditProfileActivity

class CheckBoxDialog: DialogFragment(), DialogInterface.OnClickListener {
    private lateinit var editProfileActivity: EditProfileActivity
    private lateinit var editTextProvince: EditText
    private lateinit var bundle: Bundle
    private lateinit var title: String

    private val provinces = arrayOf("AB", "BC", "MB", "NB", "NL", "NS", "NT", "NU", "ON", "PE", "QC", "SK", "YT", "N/A")
    private var checked = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var checkBoxDialog: Dialog
        editProfileActivity = activity as EditProfileActivity
        editTextProvince = editProfileActivity.findViewById(R.id.edit_text_province)

        title = "Select Province"
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
        builder.setTitle(title)

        builder.setSingleChoiceItems(provinces, checked) { dialog, pos ->
            // update the selected item which is selected by the user so that it should be selected
            // when user opens the dialog next time and pass the instance to setSingleChoiceItems method
            checked = pos

            // now also update the TextView which previews the selected item
            editTextProvince.setText(provinces[pos])

            // when selected an item the dialog should be closed with the dismiss method
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { _, _ -> }

        checkBoxDialog = builder.create()

        return checkBoxDialog
    }

    override fun onClick(p0: DialogInterface?, p1: Int) {
        TODO("Not yet implemented")
    }
}