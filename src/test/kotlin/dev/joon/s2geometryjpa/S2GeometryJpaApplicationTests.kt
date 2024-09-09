package dev.joon.s2geometryjpa

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class S2GeometryJpaApplicationTests {

    @Test
    fun contextLoads() {
    }

}
