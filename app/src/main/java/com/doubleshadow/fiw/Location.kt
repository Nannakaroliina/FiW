package com.doubleshadow.fiw


class Location {
    companion object Factory {
        fun create(): Location = Location()
    }

    var locationId: String? = null
    var locationName: String? = null
    var lat: Double? = null
    var lgn: Double? = null
}