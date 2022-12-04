package ca.sfu.minerva.data

data class BikeTheft(
    val id: Int,
    val count: Int,
    val hundredBlock: String,
    val latitude: Double,
    val longitude: Double,
    val neighbourhood: String
)
