package com.kangdroid.client.communication

import com.fasterxml.jackson.databind.ObjectMapper
import com.kangdroid.client.communication.dto.ErrorResponse
import com.kangdroid.client.communication.dto.UserLoginRequestDto
import com.kangdroid.client.communication.dto.UserLoginResponseDto
import com.kangdroid.client.communication.dto.UserRegisterResponseDto
import com.kangdroid.client.printer.KDRPrinter
import org.springframework.http.HttpEntity
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

    fun login(userLoginRequestDto: UserLoginRequestDto, isLogin: Boolean = true): Boolean {
        val finalAddress: String = if (isLogin) {
            "$serverAddress/api/client/login"
        } else {
            "$serverAddress/api/client/register"
        }

        // Communicate with server
        val loginResponseEntity: ResponseEntity<String> = try {
            restTemplate.exchange(finalAddress, HttpMethod.POST, HttpEntity(userLoginRequestDto))
        } catch (resourceAccessException: ResourceAccessException) {
            KDRPrinter.printError("Error communicating with server. Check server address and internet connection.")
            return false
        } catch (httpClientErrorException: HttpClientErrorException) {
            handleServerClientError(httpClientErrorException)
            return false
        } catch (httpServerErrorException: HttpServerErrorException) {
            handleServerClientError(httpServerErrorException)
            return false
        }

        // Get Body
        val body: String = loginResponseEntity.body ?: run {
            KDRPrinter.printError("Cannot get body part from server.")
            return false
        }

        if (isLogin) {
            // Object string to UserLoginResponseDto
            val userLoginResponseDto: UserLoginResponseDto = runCatching {
                objectMapper.readValue(body, UserLoginResponseDto::class.java)
            }.onFailure {
                // If Body type is NOT matching
                KDRPrinter.printError("Server responded with correct code, but it did not sent body!")
            }.getOrNull() ?: return false

            // Finally use token
            token = userLoginResponseDto.token
            KDRPrinter.printNormal("Login Succeed!")
        } else {
            val userRegisterResponseDto: UserRegisterResponseDto = runCatching {
                objectMapper.readValue(body, UserRegisterResponseDto::class.java)
            }.onFailure {
                // If Body type is NOT matching
                KDRPrinter.printError("Server responded with correct code, but it did not sent body!")
            }.getOrNull() ?: return false

            KDRPrinter.printNormal("Successfully registered user: ${userRegisterResponseDto.registeredId}")
        }



        return true
    }
}