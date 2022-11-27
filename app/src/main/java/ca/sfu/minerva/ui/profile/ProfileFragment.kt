package ca.sfu.minerva.ui.profile

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ca.sfu.minerva.MainActivity
import ca.sfu.minerva.R
import ca.sfu.minerva.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor:SharedPreferences.Editor


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this)[ProfileViewModel::class.java]
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root


        sharedPreferences = activity?.getSharedPreferences("LoginInfo", AppCompatActivity.MODE_PRIVATE)!!
        editor = sharedPreferences.edit()
        val logOutBtn = root.findViewById<TextView>(R.id.text_logout)
        logOutBtn.setOnClickListener {
            editor.putString("email", "")
            editor.putString("pass", "")
            editor.apply()
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)

        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}