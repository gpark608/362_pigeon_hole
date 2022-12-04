package ca.sfu.minerva.data

data class BikeRack(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val numberOfRacks: Int,
    val skytrainStationName: String,
    val streetName: String,
    val streetNumber: String,
    val streetSide: String
)