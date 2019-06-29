package com.doubleshadow.fiw

import com.google.android.gms.maps.model.LatLng

data class Location (
    var locationId: String = "",
    var locationName: String = "",
    var locationLatLng: LatLng
    )