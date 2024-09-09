package dev.joon.s2geometryjpa

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<S2GeometryJpaApplication>().with(TestcontainersConfiguration::class).run(*args)
}
