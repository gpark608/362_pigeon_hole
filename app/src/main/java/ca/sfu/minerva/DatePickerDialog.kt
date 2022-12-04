package ca.sfu.minerva

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import ca.sfu.minerva.ui.profile.EditProfileActivity
import java.util.*


class DatePickerDialog: DialogFragment(), DatePickerDialog.OnDateSetListener {
    private lateinit var editProfileActivity: EditProfileActivity
    private lateinit var editTextBirthdate: EditText
    private val calendar = Calendar.getInstance()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var datePickerDialog: Dialog

        editProfileActivity = activity as EditProfileActivity
        editTextBirthdate = editProfileActivity.findViewById(R.id.edit_text_birthdate)

        val currentYear = calendar[Calendar.YEAR]
        val currentMonth = calendar[Calendar.MONTH]
        val currentDay = calendar[Calendar.DAY_OF_MONTH]

        datePickerDialog = DatePickerDialog(
            editProfileActivity,
            R.style.CustomDatePickerDialog,
            this,
            currentYear - 22,
            currentMonth,
            currentYear
        )

        val minYear: Int = currentYear - 16
        val minMonth: Int = currentMonth
        val minDay: Int = currentDay

        calendar.set(minYear, minMonth, minDay)
        val minDateInMilliSeconds = calendar.timeInMillis

        // Set 16 years from today as max limit of date picker
        datePickerDialog.datePicker.maxDate = minDateInMilliSeconds

        return datePickerDialog
    }

    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val formattedMonth = if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"
        val formattedDay = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"

        editTextBirthdate.setText("$year-${formattedMonth}-$formattedDay")
    }
}