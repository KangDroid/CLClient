package com.kangdroid.client.communication

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity

@Component
class ServerCommunication {
    val restTemplate: RestTemplate = RestTemplate()
    private val serverAddress: String = "http://localhost:8080"

    fun isServerAlive(): Boolean {
        val finalAddress: String = "$serverAddress/api/client/alive"
        lateinit var responseEntity: ResponseEntity<String>
        runCatching {
            responseEntity = restTemplate.getForEntity(finalAddress)
        }.onFailure {
            return false
        }

        return responseEntity.statusCode.is2xxSuccessful
    }
}