package dev.joon.s2geometryjpa

import com.google.common.geometry.S2CellId
import com.google.common.geometry.S2LatLng
import jakarta.persistence.*

@Table(name = "t_location")
@Entity
class LocationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    val latitude: Double

    val longitude: Double

    val s2CellId: Long

    constructor(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
        this.s2CellId = S2CellId.fromLatLng(S2LatLng.fromDegrees(latitude, longitude)).id();
    }

}
