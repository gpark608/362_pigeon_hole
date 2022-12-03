package ca.sfu.minerva.data

data class Crime(
    val id: Int,
    val count: Int,
    val hundredBlock: String,
    val latitude: Double,
    val longitude: Double,
    val neighbourhood: String
)
