package com.kangdroid.client

import com.kangdroid.client.configuration.ServerConfigurationComponent
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(ServerConfigurationComponent::class)
class ClClientApplication

fun main(args: Array<String>) {
    runApplication<ClClientApplication>(*args)
}
