package dev.joon.s2geometryjpa

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param


interface LocationRepository : JpaRepository<LocationEntity, Long> {

    @Query(
        """
        SELECT l FROM LocationEntity l 
        WHERE l.s2CellId BETWEEN :minCellId AND :maxCellId
        """
    )
    fun findLocationsWithinS2CellRange(
        @Param("minCellId") minCellId: Long,
        @Param("maxCellId") maxCellId: Long
    ): List<LocationEntity>

    @Query(
        """
        SELECT l FROM LocationEntity l 
        WHERE l.s2CellId BETWEEN :minCellId AND :maxCellId
        """
    )
    fun findLocationsWithinS2CellRange(
        @Param("minCellId") minCellId: Long,
        @Param("maxCellId") maxCellId: Long,
        pageable: Pageable
    ): Page<LocationEntity>

    @Query(
        """
        SELECT l FROM LocationEntity l 
        WHERE l.latitude BETWEEN :latMin AND :latMax 
          AND l.longitude BETWEEN :lngMin AND :lngMax
        """
    )
    fun findWithinBoundingBox(
        @Param("latMin") latMin: Double, @Param("latMax") latMax: Double,
        @Param("lngMin") lngMin: Double, @Param("lngMax") lngMax: Double
    ): List<LocationEntity>

    @Query(
        """
        SELECT l FROM LocationEntity l 
        WHERE l.latitude BETWEEN :latMin AND :latMax 
          AND l.longitude BETWEEN :lngMin AND :lngMax
        """
    )
    fun findWithinBoundingBox(
        @Param("latMin") latMin: Double, @Param("latMax") latMax: Double,
        @Param("lngMin") lngMin: Double, @Param("lngMax") lngMax: Double,
        pageable: Pageable
    ): Page<LocationEntity>
}
