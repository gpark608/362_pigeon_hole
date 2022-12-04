package ca.sfu.minerva

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GalleryAndCameraDialogViewModel: ViewModel() {
    val openCamera = MutableLiveData<Boolean>()
}