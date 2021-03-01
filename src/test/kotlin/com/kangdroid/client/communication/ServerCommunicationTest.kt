package com.kangdroid.client.communication

import com.fasterxml.jackson.databind.ObjectMapper
import com.kangdroid.client.communication.dto.ErrorResponse
import com.kangdroid.client.communication.dto.UserLoginRequestDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators

@RunWith(SpringRunner::class)
@SpringBootTest
class ServerCommunicationTest {
    @Autowired
    private lateinit var serverCommunication: ServerCommunication

    // Global UserLoginRequestDto
    private val userLoginRequestDto: UserLoginRequestDto = UserLoginRequestDto(
        userName = "TEST_USERNAME",
        userPassword = "TEST_PASSWORD"
    )

    // Object Mapper
    private val objectMapper: ObjectMapper = ObjectMapper()

    // Mock Server
    private val serverAddress: String = "http://localhost:8080"
    private lateinit var mockServer: MockRestServiceServer
    private lateinit var clientHttpRequestFactory: ClientHttpRequestFactory

    @Test
    fun checkingServerAliveWorkingWell() {
        // Backup Original Server Communication
        clientHttpRequestFactory = serverCommunication.restTemplate.requestFactory

        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()

        mockServer.expect(
            ExpectedCount.min(1),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/alive")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withSuccess(
                    "true",
                    MediaType.TEXT_PLAIN
                )
            )

        assertThat(serverCommunication.isServerAlive()).isEqualTo(true)
        mockServer.verify()

        // Restore RequestFactory
        serverCommunication.restTemplate.requestFactory = clientHttpRequestFactory
    }

    @Test
    fun loginIsSuccessful() {
        // Backup Original Server Communication
        clientHttpRequestFactory = serverCommunication.restTemplate.requestFactory

        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/login")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withSuccess(
                    "{\"token\": \"TEST_TOKEN\"}",
                    MediaType.APPLICATION_JSON
                )
            )

        // Successful Test
        assertThat(serverCommunication.login(userLoginRequestDto)).isEqualTo(true)
        mockServer.verify()

        // Restore RequestFactory
        serverCommunication.restTemplate.requestFactory = clientHttpRequestFactory
    }

    @Test
    fun isLoginReturnsFalseSuccessCodeWrongBody() {
        // Backup Original Server Communication
        clientHttpRequestFactory = serverCommunication.restTemplate.requestFactory

        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/login")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withSuccess(
                    "wrong-body",
                    MediaType.TEXT_PLAIN
                )
            )

        // Successful Test
        assertThat(serverCommunication.login(userLoginRequestDto)).isEqualTo(false)
        mockServer.verify()

        // Restore RequestFactory
        serverCommunication.restTemplate.requestFactory = clientHttpRequestFactory
    }

    @Test
    fun isLoginReturnsFalseFailedServer() {
        // Backup Original Server Communication
        clientHttpRequestFactory = serverCommunication.restTemplate.requestFactory

        // Without mock server
        assertThat(serverCommunication.login(userLoginRequestDto)).isEqualTo(false)

        // Restore RequestFactory
        serverCommunication.restTemplate.requestFactory = clientHttpRequestFactory
    }

    @Test
    fun isLoginReturnsFalseWithoutBody() {
        // Backup Original Server Communication
        clientHttpRequestFactory = serverCommunication.restTemplate.requestFactory

        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/login")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withSuccess(
                    "",
                    MediaType.TEXT_PLAIN
                )
            )

        // Failure Test
        assertThat(serverCommunication.login(userLoginRequestDto)).isEqualTo(false)
        mockServer.verify()

        // Restore RequestFactory
        serverCommunication.restTemplate.requestFactory = clientHttpRequestFactory
    }

    @Test
    fun isLoginReturnsFalseWithWrongID() {
        // Backup Original Server Communication
        clientHttpRequestFactory = serverCommunication.restTemplate.requestFactory

        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/login")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.NOT_FOUND)
                    .body(
                        objectMapper.writeValueAsString(
                            ErrorResponse(
                                errorMessage = "testing_error",
                                statusCode = "404",
                                statusMessage = "NOT_FOUND"
                            )
                        )
                    )
            )

        // Failure Test
        assertThat(serverCommunication.login(userLoginRequestDto)).isEqualTo(false)
        mockServer.verify()

        // Restore RequestFactory
        serverCommunication.restTemplate.requestFactory = clientHttpRequestFactory
    }

    @Test
    fun isLoginReturnsFalseWithWrongPassword() {
        // Backup Original Server Communication
        clientHttpRequestFactory = serverCommunication.restTemplate.requestFactory

        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/login")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.FORBIDDEN)
                    .body(
                        objectMapper.writeValueAsString(
                            ErrorResponse(
                                errorMessage = "testing_error",
                                statusCode = "403",
                                statusMessage = "FORBIDDEN"
                            )
                        )
                    )
            )

        // Failure Test
        assertThat(serverCommunication.login(userLoginRequestDto)).isEqualTo(false)
        mockServer.verify()

        // Restore RequestFactory
        serverCommunication.restTemplate.requestFactory = clientHttpRequestFactory
    }

    @Test
    fun isLoginReturnsFalseInternalUnknownError() {
        // Backup Original Server Communication
        clientHttpRequestFactory = serverCommunication.restTemplate.requestFactory

        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/login")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.TOO_MANY_REQUESTS)
                    .body(
                        "Whatevberrrr"
                    )
            )

        // Failure Test
        assertThat(serverCommunication.login(userLoginRequestDto)).isEqualTo(false)
        mockServer.verify()

        // Restore RequestFactory
        serverCommunication.restTemplate.requestFactory = clientHttpRequestFactory
    }
}