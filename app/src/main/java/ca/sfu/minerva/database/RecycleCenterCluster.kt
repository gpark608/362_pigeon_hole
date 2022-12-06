package ca.sfu.minerva.database

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class RecycleCenterCluster(location: LatLng, title: String, snippet: String): ClusterItem {
    private var latLng: LatLng = location
    private var locationTitle: String = title
    private var phone: String = snippet


    override fun getPosition(): LatLng {
        return latLng
    }

    override fun getTitle(): String? {
        return locationTitle
    }

    override fun getSnippet(): String? {
        return phone
    }


}