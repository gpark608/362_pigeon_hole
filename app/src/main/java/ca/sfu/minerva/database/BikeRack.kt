package ca.sfu.minerva.database

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.google.type.DateTime

class BikeRack(location: LatLng): ClusterItem {
    private var latLng: LatLng = location

    override fun getPosition(): LatLng {
        return latLng
    }

    override fun getTitle(): String? {
        return "BikeRack TITLE"
    }

    override fun getSnippet(): String? {
        return "BikeRack SNIPPET"
    }

}