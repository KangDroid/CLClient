package com.kangdroid.client.communication

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.fasterxml.jackson.module.kotlin.readValue
import com.kangdroid.client.communication.dto.*
import com.kangdroid.client.printer.KDRPrinter
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.*

@Component
class ServerCommunication {
    val restTemplate: RestTemplate = RestTemplate()
    private val serverAddress: String = "http://localhost:8080"
    private var token: String? = null
    private val objectMapper: ObjectMapper = ObjectMapper()

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

    fun checkToken(): Boolean {
        return if (token == null) {
            KDRPrinter.printError("User did not logged into server!")
            KDRPrinter.printError("Please login first.")
            false
        } else {
            true
        }
    }

    // Range from 4xx to 5xx
    fun handleServerClientError(httpStatusCodeException: HttpStatusCodeException) {
        // With 4xx Codes!
        val body: String = httpStatusCodeException.responseBodyAsString

        // meaning error!
        val errorResponse: ErrorResponse = runCatching {
            objectMapper.readValue(body, ErrorResponse::class.java)
        }.onFailure { innerIt ->
            // If body type is NOT matching with errorResponse
            KDRPrinter.printError("Exception Occurred!")
            KDRPrinter.printError(innerIt.message ?: "No message available")
        }.getOrNull() ?: return

        KDRPrinter.printError("Server responded with: ${errorResponse.statusCode} - ${errorResponse.statusMessage}")
        KDRPrinter.printError("Message is: ${errorResponse.errorMessage}")
    }

    fun getResponseEntityInStringFormat(toExecute: () -> ResponseEntity<String>): ResponseEntity<String>? {
        return try {
            toExecute()
        } catch (resourceAccessException: ResourceAccessException) {
            KDRPrinter.printError("Error communicating with server. Check server address and internet connection.")
            return null
        } catch (httpClientErrorException: HttpClientErrorException) {
            handleServerClientError(httpClientErrorException)
            return null
        } catch (httpServerErrorException: HttpServerErrorException) {
            handleServerClientError(httpServerErrorException)
            return null
        }
    }

    private inline fun <reified T> getObjectValues(body: String): T? {
        return runCatching {
            objectMapper.readValue<T>(body)
        }.onFailure {
            // If Body type is NOT matching
            KDRPrinter.printError("Server responded with correct code, but it did not sent body!")
        }.getOrNull()
    }

    fun login(userLoginRequestDto: UserLoginRequestDto, isLogin: Boolean = true): Boolean {
        val finalAddress: String = if (isLogin) {
            "$serverAddress/api/client/login"
        } else {
            "$serverAddress/api/client/register"
        }

        // Communicate with server
        val loginResponseEntity: ResponseEntity<String> = getResponseEntityInStringFormat {
            restTemplate.exchange(finalAddress, HttpMethod.POST, HttpEntity(userLoginRequestDto))
        } ?: return false

        // Get Body
        val body: String = loginResponseEntity.body ?: run {
            KDRPrinter.printError("Cannot get body part from server.")
            return false
        }

        if (isLogin) {
            // Object string to UserLoginResponseDto
            val userLoginResponseDto: UserLoginResponseDto = getObjectValues<UserLoginResponseDto>(body) ?: return false

            // Finally use token
            token = userLoginResponseDto.token
            KDRPrinter.printNormal("Login Succeed!")
        } else {
            val userRegisterResponseDto: UserRegisterResponseDto = getObjectValues<UserRegisterResponseDto>(body) ?: return false
            KDRPrinter.printNormal("Successfully registered user: ${userRegisterResponseDto.registeredId}")
        }
        return true
    }

    fun showRegion(): Boolean {
        val finalUrl: String = "$serverAddress/api/client/node"
        if (!checkToken()) return false

        // Request!
        val responseEntity: ResponseEntity<String> = getResponseEntityInStringFormat {
            val httpHeaders: HttpHeaders = HttpHeaders().apply {
                add("X-AUTH-TOKEN", token)
            }
            restTemplate.exchange(finalUrl, HttpMethod.GET, HttpEntity<Void>(httpHeaders))
        } ?: return false

        val body: String = responseEntity.body ?: run {
            KDRPrinter.printError("Cannot get body part from server.")
            return false
        }

        // Get List of Values
        val listNode: Array<NodeInformationResponseDto> = getObjectValues<Array<NodeInformationResponseDto>>(body) ?: return false

        if (listNode.isEmpty()) {
            KDRPrinter.printNormal("There is NO registered node on server!")
        } else {
            KDRPrinter.printNormal("Total Nodes: ${listNode.size}")
            for (node in listNode) {
                KDRPrinter.printNormal("Region: ${node.regionName}")
                KDRPrinter.printNormal("Load: ${node.nodeLoadPercentage}\n")
            }
        }
        return true
    }
}