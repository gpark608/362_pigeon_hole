package ca.sfu.minerva.util

object Helper {
    private fun getTimeInSeconds(start: Long, end: Long): Double {
        return ((end - start) / 1000).toDouble()
    }

    fun getTimeInMinutes(start: Long, end: Long): Double {
        return getTimeInSeconds(start, end) / 60
    }

    // Might delete later
    fun getTimeInHours(start: Long, end: Long): Double {
        return getTimeInMinutes(start, end) / 60
    }

    fun metersToMiles(meters: Float): Float {
        return (meters / 1609.344).toFloat()
    }
}