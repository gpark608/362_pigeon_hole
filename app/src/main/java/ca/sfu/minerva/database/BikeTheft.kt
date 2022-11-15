package ca.sfu.minerva.database

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.google.type.DateTime

class BikeTheft(location: LatLng): ClusterItem {
    private var latLng: LatLng = location
    //private var dateTime: DateTime

    override fun getPosition(): LatLng {
        return latLng
    }

    override fun getTitle(): String? {
        return "BikeTheft TITLE"
    }

    override fun getSnippet(): String? {
        return "BikeTheft SNIPPET"
    }

}