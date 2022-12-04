package ca.sfu.minerva

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider

class GalleryAndCameraDialog: DialogFragment() {
    private lateinit var galleryAndCameraDialogViewModel: GalleryAndCameraDialogViewModel
    private lateinit var btnCamera: Button
    private lateinit var btnGallery: Button

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var dialog: Dialog
        galleryAndCameraDialogViewModel = ViewModelProvider(requireActivity())[GalleryAndCameraDialogViewModel::class.java]


        val builder = AlertDialog.Builder(requireActivity())
        val view: View = requireActivity().layoutInflater.inflate(R.layout.gallery_and_camera_dialog, null)

        btnCamera = view.findViewById(R.id.button_camera)
        btnGallery = view.findViewById(R.id.button_gallery)

        btnCamera.setOnClickListener { onClickCamera() }
        btnGallery.setOnClickListener { onClickGallery() }

        builder.setView(view)
        dialog = builder.create()

        return dialog
    }

    private fun onClickCamera() {
        galleryAndCameraDialogViewModel.openCamera.value = true
    }

    private fun onClickGallery() {
        galleryAndCameraDialogViewModel.openCamera.value = false
    }
}