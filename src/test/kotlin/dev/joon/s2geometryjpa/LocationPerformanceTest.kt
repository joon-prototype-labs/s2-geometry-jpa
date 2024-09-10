package dev.joon.s2geometryjpa

import com.google.common.geometry.S2CellId
import com.google.common.geometry.S2LatLng
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName
import kotlin.system.measureTimeMillis

@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LocationPerformanceTest(
    @Autowired private val locationRepository: LocationRepository
) {

    companion object {
        @Container
        @JvmStatic
        val postgresContainer = PostgreSQLContainer<Nothing>(DockerImageName.parse("postgres:latest"))
            .apply {
                withDatabaseName("testdb")
                withUsername("testuser")
                withPassword("testpass")
                withExposedPorts(5432)
                start()
            }

        @JvmStatic
        @DynamicPropertySource
        fun dynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgresContainer.jdbcUrl }
            registry.add("spring.datasource.username") { postgresContainer.username }
            registry.add("spring.datasource.password") { postgresContainer.password }
        }
    }

    @BeforeAll
    fun setUp() {
        if (locationRepository.count() == 0L) {
            val chunkSize = 10_000
            val totalSize = 1_000_000
            val random = java.util.Random(1234) // 고정된 시드 값 사용

            for (start in 0 until totalSize step chunkSize) {
                val end = (start + chunkSize).coerceAtMost(totalSize)
                val locations = mutableListOf<LocationEntity>()

                for (i in start until end) {
                    val latitude = 35.0 + random.nextDouble() * 10.0
                    val longitude = 125.0 + random.nextDouble() * 10.0
                    locations.add(LocationEntity(latitude, longitude))
                }

                // Save the current chunk
                locationRepository.saveAll(locations)
            }
        }
    }

    @Nested
    inner class PerformanceComparisonTests {
        @Test
        fun `작은 범위 검색 성능 비교`() {
            val latMin = 37.7740
            val latMax = 37.7750
            val lngMin = -122.4200
            val lngMax = -122.4190

            val boundingBoxTime = measureTimeMillis {
                val results = locationRepository.findWithinBoundingBox(latMin, latMax, lngMin, lngMax, Pageable.ofSize(10))
                println("Bounding Box 검색 결과 수: ${results.size}")
            }

            val minS2CellId = S2CellId.fromLatLng(S2LatLng.fromDegrees(latMin, lngMin)).id()
            val maxS2CellId = S2CellId.fromLatLng(S2LatLng.fromDegrees(latMax, lngMax)).id()

            val s2CellTime = measureTimeMillis {
                val results = locationRepository.findLocationsWithinS2CellRange(minS2CellId, maxS2CellId, Pageable.ofSize(10))
                println("S2 Cell 검색 결과 수: ${results.size}")
            }

            println("작은 범위 - Bounding Box 검색 시간: $boundingBoxTime ms")
            println("작은 범위 - S2 Cell 검색 시간: $s2CellTime ms")
        }

        @Test
        fun `중간 범위 검색 성능 비교`() {
            val latMin = 37.0
            val latMax = 38.0
            val lngMin = -123.0
            val lngMax = -122.0

            val boundingBoxTime = measureTimeMillis {
                val results = locationRepository.findWithinBoundingBox(latMin, latMax, lngMin, lngMax, Pageable.ofSize(10))
                println("Bounding Box 검색 결과 수: ${results.size}")
            }

            val minS2CellId = S2CellId.fromLatLng(S2LatLng.fromDegrees(latMin, lngMin)).id()
            val maxS2CellId = S2CellId.fromLatLng(S2LatLng.fromDegrees(latMax, lngMax)).id()

            val s2CellTime = measureTimeMillis {
                val results = locationRepository.findLocationsWithinS2CellRange(minS2CellId, maxS2CellId, Pageable.ofSize(10))
                println("S2 Cell 검색 결과 수: ${results.size}")
            }

            println("중간 범위 - Bounding Box 검색 시간: $boundingBoxTime ms")
            println("중간 범위 - S2 Cell 검색 시간: $s2CellTime ms")
        }

        @Test
        fun `큰 범위 검색 성능 비교`() {
            val latMin = 30.0
            val latMax = 45.0
            val lngMin = -130.0
            val lngMax = -120.0

            val boundingBoxTime = measureTimeMillis {
                val results = locationRepository.findWithinBoundingBox(latMin, latMax, lngMin, lngMax, Pageable.ofSize(10))
                println("Bounding Box 검색 결과 수: ${results.size}")
            }

            val minS2CellId = S2CellId.fromLatLng(S2LatLng.fromDegrees(latMin, lngMin)).id()
            val maxS2CellId = S2CellId.fromLatLng(S2LatLng.fromDegrees(latMax, lngMax)).id()

            val s2CellTime = measureTimeMillis {
                val results = locationRepository.findLocationsWithinS2CellRange(minS2CellId, maxS2CellId, Pageable.ofSize(10))
                println("S2 Cell 검색 결과 수: ${results.size}")
            }

            println("큰 범위 - Bounding Box 검색 시간: $boundingBoxTime ms")
            println("큰 범위 - S2 Cell 검색 시간: $s2CellTime ms")
        }
    }
}
