package ca.sfu.minerva.database

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class BikeRack(location: LatLng, title: String, snippet: String, numberOfRacks: Int): ClusterItem {
    private var latLng: LatLng = location
    private var locationTitle: String = title
    private var nearbySkytrain: String = snippet
    private var numberOfRacks: Int = numberOfRacks

    override fun getPosition(): LatLng {
        return latLng
    }

    override fun getTitle(): String? {
        return locationTitle
    }

    override fun getSnippet(): String? {
        return nearbySkytrain
    }

    fun getNumberOfRacks(): Int {
        return numberOfRacks
    }
}