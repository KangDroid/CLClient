package com.kangdroid.client.communication

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.fasterxml.jackson.module.kotlin.readValue
import com.kangdroid.client.communication.dto.*
import com.kangdroid.client.error.FunctionResponse
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
    private var containerList: MutableList<UserImageListResponseDto> = mutableListOf()

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

    fun login(userLoginRequestDto: UserLoginRequestDto, isLogin: Boolean = true): FunctionResponse {
        val finalAddress: String = if (isLogin) {
            "$serverAddress/api/client/login"
        } else {
            "$serverAddress/api/client/register"
        }

        // Communicate with server
        val loginResponseEntity: ResponseEntity<String> = getResponseEntityInStringFormat {
            restTemplate.exchange(finalAddress, HttpMethod.POST, HttpEntity(userLoginRequestDto))
        } ?: return FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX

        // Get Body
        val body: String = loginResponseEntity.body ?: run {
            KDRPrinter.printError("Cannot get body part from server.")
            return FunctionResponse.SERVER_RESPONSE_OK_BUT_NO_BODY
        }

        if (isLogin) {
            // Object string to UserLoginResponseDto
            val userLoginResponseDto: UserLoginResponseDto = getObjectValues<UserLoginResponseDto>(body)
                ?: return FunctionResponse.SERVER_RESPONSE_OK_BUT_WRONG_FORMAT

            // Finally use token
            token = userLoginResponseDto.token
            KDRPrinter.printNormal("Login Succeed!")
        } else {
            val userRegisterResponseDto: UserRegisterResponseDto = getObjectValues<UserRegisterResponseDto>(body)
                ?: return FunctionResponse.SERVER_RESPONSE_OK_BUT_WRONG_FORMAT
            KDRPrinter.printNormal("Successfully registered user: ${userRegisterResponseDto.registeredId}")
        }
        return FunctionResponse.SUCCESS
    }

    fun showRegion(): FunctionResponse {
        val finalUrl: String = "$serverAddress/api/client/node"
        if (!checkToken()) return FunctionResponse.CLIENT_NO_TOKEN

        // Request!
        val responseEntity: ResponseEntity<String> = getResponseEntityInStringFormat {
            val httpHeaders: HttpHeaders = HttpHeaders().apply {
                add("X-AUTH-TOKEN", token)
            }
            restTemplate.exchange(finalUrl, HttpMethod.GET, HttpEntity<Void>(httpHeaders))
        } ?: return FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX

        val body: String = responseEntity.body ?: run {
            KDRPrinter.printError("Cannot get body part from server.")
            return FunctionResponse.SERVER_RESPONSE_OK_BUT_NO_BODY
        }

        // Get List of Values
        val listNode: Array<NodeInformationResponseDto> = getObjectValues<Array<NodeInformationResponseDto>>(body)
            ?: return FunctionResponse.SERVER_RESPONSE_OK_BUT_WRONG_FORMAT

        if (listNode.isEmpty()) {
            KDRPrinter.printNormal("There is NO registered node on server!")
        } else {
            KDRPrinter.printNormal("Total Nodes: ${listNode.size}")
            for (node in listNode) {
                KDRPrinter.printNormal("Region: ${node.regionName}")
                KDRPrinter.printNormal("Load: ${node.nodeLoadPercentage}\n")
            }
        }
        return FunctionResponse.SUCCESS
    }

    fun showClientContainer(): FunctionResponse {
        val clientContainerShowUrl: String = "$serverAddress/api/client/container"
        if (!checkToken()) return FunctionResponse.CLIENT_NO_TOKEN

        // Request!
        val responseEntity: ResponseEntity<String> = getResponseEntityInStringFormat {
            val httpHeaders: HttpHeaders = HttpHeaders().apply {
                add("X-AUTH-TOKEN", token)
            }
            restTemplate.exchange(clientContainerShowUrl, HttpMethod.GET, HttpEntity<Void>(httpHeaders))
        } ?: return FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX

        // get Response Body
        val responseBody: String = responseEntity.body ?: run {
            KDRPrinter.printError("Cannot get body part from server.")
            return FunctionResponse.SERVER_RESPONSE_OK_BUT_NO_BODY
        }

        // Parse as Objects
        val userImageResponseList: Array<UserImageListResponseDto> =
            getObjectValues<Array<UserImageListResponseDto>>(responseBody) ?: return FunctionResponse.SERVER_RESPONSE_OK_BUT_WRONG_FORMAT

        // Clear container
        containerList.clear()

        if (userImageResponseList.isEmpty()) {
            KDRPrinter.printNormal("There is NO created/registered container on server!")
        } else {
            KDRPrinter.printNormal("Container information for: ${userImageResponseList[0].userName}\n")
            KDRPrinter.printNormal("Total Containers: ${userImageResponseList.size}")
            for (userImageResponse in userImageResponseList) {
                containerList.add(userImageResponse)
                KDRPrinter.printNormal("Docker ID: ${userImageResponse.dockerId}")
                KDRPrinter.printNormal("Compute Region: ${userImageResponse.computeRegion}\n")
            }
        }
        return FunctionResponse.SUCCESS
    }

    fun createClientContainer(region: String): FunctionResponse {
        if (!checkToken()) return FunctionResponse.CLIENT_NO_TOKEN
        val clientCreateContainerUrl: String = "$serverAddress/api/client/container"

        // Temp DTO Declare
        class UserImageSaveRequestDto(
            var userToken: String = "",
            var dockerId: String = "",
            var computeRegion: String
        )

        // Request!
        val responseEntity: ResponseEntity<String> = getResponseEntityInStringFormat {
            val httpHeaders: HttpHeaders = HttpHeaders().apply {
                add("X-AUTH-TOKEN", token)
            }
            restTemplate.exchange(clientCreateContainerUrl, HttpMethod.POST, HttpEntity<UserImageSaveRequestDto>(UserImageSaveRequestDto(computeRegion = region), httpHeaders))
        } ?: return FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX

        // get Response Body
        val responseBody: String = responseEntity.body ?: run {
            KDRPrinter.printError("Cannot get body part from server.")
            return FunctionResponse.SERVER_RESPONSE_OK_BUT_NO_BODY
        }

        println(responseBody)

        // Parse as Objects
        val userImageResponseList: UserImageResponseDto =
            getObjectValues<UserImageResponseDto>(responseBody) ?: return FunctionResponse.SERVER_RESPONSE_OK_BUT_WRONG_FORMAT

        KDRPrinter.printNormal("Container-ID: ${userImageResponseList.containerId}")
        KDRPrinter.printNormal("Successfully created on: ${userImageResponseList.regionLocation}")
        KDRPrinter.printNormal("You can ssh within \"ssh root@${userImageResponseList.targetIpAddress} -p ${userImageResponseList.targetPort}\"")

        return FunctionResponse.SUCCESS
    }
}