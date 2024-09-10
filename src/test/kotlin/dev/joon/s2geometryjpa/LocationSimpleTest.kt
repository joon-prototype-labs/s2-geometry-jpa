package dev.joon.s2geometryjpa

import com.google.common.geometry.S2CellId
import com.google.common.geometry.S2LatLng
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName

@SpringBootTest
@Transactional
class LocationSimpleTest {

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

    @Autowired
    lateinit var locationRepository: LocationRepository

    @BeforeEach
    fun setUp() {
        // 테스트용으로 작은 데이터 세트를 설정
        val locations = listOf(
            LocationEntity(37.7749, -122.4194),  // 샌프란시스코
            LocationEntity(34.0522, -118.2437),  // 로스앤젤레스
            LocationEntity(40.7128, -74.0060),   // 뉴욕
            LocationEntity(35.6895, 139.6917)    // 도쿄
        )
        locationRepository.saveAll(locations)
    }

    @Test
    fun `S2CellId 범위로 사용자 검색`() {
        val center = S2CellId.fromLatLng(S2LatLng.fromDegrees(37.7749, -122.4194))
        val minCellId = center.prev().id()
        val maxCellId = center.next().id()

        val nearbyLocations = locationRepository.findLocationsWithinS2CellRange(minCellId, maxCellId)
        Assertions.assertEquals(1, nearbyLocations.size)
        Assertions.assertEquals(37.7749, nearbyLocations[0].latitude)
    }

    @Test
    fun `경계 범위로 사용자 검색`() {
        val latMin = 37.0
        val latMax = 38.0
        val lngMin = -123.0
        val lngMax = -122.0

        val nearbyLocations = locationRepository.findWithinBoundingBox(latMin, latMax, lngMin, lngMax)
        Assertions.assertEquals(1, nearbyLocations.size)
        Assertions.assertEquals(37.7749, nearbyLocations[0].latitude)
    }
}
