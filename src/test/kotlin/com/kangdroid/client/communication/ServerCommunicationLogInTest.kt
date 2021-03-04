/**
 * Server Communication Test, for logging in!
 */
package com.kangdroid.client.communication

import com.fasterxml.jackson.databind.ObjectMapper
import com.kangdroid.client.communication.dto.ErrorResponse
import com.kangdroid.client.communication.dto.UserLoginRequestDto
import com.kangdroid.client.communication.dto.UserRegisterResponseDto
import com.kangdroid.client.error.FunctionResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
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
class ServerCommunicationLogInTest {
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

    @Before
    fun backupRequestFactory() {
        // Backup Original Server Communication
        clientHttpRequestFactory = serverCommunication.restTemplate.requestFactory
    }

    @After
    fun restoreRequestFactory() {
        // Restore RequestFactory
        serverCommunication.restTemplate.requestFactory = clientHttpRequestFactory
    }

    @Test
    fun checkingServerAliveWorkingWell() {
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
    }

    @Test
    fun checkingServerAliveFailingWell() {
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()

        mockServer.expect(
            ExpectedCount.min(1),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/alive")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.FORBIDDEN)
            )

        assertThat(serverCommunication.isServerAlive()).isEqualTo(false)
        mockServer.verify()
    }

    @Test
    fun login_returns_success_when_succeeds() {
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
        assertThat(serverCommunication.login(userLoginRequestDto)).isEqualTo(FunctionResponse.SUCCESS)
        mockServer.verify()
    }

    @Test
    fun login_returns_SERVER_RESPONSE_OK_BUT_WRONG_FORMAT_when_ok_but_wrong_format() {
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
        assertThat(serverCommunication.login(userLoginRequestDto)).isEqualTo(FunctionResponse.SERVER_RESPONSE_OK_BUT_WRONG_FORMAT)
        mockServer.verify()
    }

    @Test
    fun login_returns_4xx_5xx_response_when_internal_server_error() {
        // Without mock server
        assertThat(serverCommunication.login(userLoginRequestDto)).isEqualTo(FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX)
    }

    @Test
    fun login_returns_SERVER_RESPONSE_OK_BUT_NO_BODY_when_ok_but_empty_body() {
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
        assertThat(serverCommunication.login(userLoginRequestDto)).isEqualTo(FunctionResponse.SERVER_RESPONSE_OK_BUT_NO_BODY)
        mockServer.verify()
    }

    @Test
    fun login_returns_4xx_5xx_when_ID_incorrect() {
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
        assertThat(serverCommunication.login(userLoginRequestDto)).isEqualTo(FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX)
        mockServer.verify()
    }

    @Test
    fun login_returns_4xx_5xx_when_PW_incorrect() {
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
        assertThat(serverCommunication.login(userLoginRequestDto)).isEqualTo(FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX)
        mockServer.verify()
    }

    @Test
    fun login_returns_4xx_5xx_when_internal_error() {
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
        assertThat(serverCommunication.login(userLoginRequestDto)).isEqualTo(FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX)
        mockServer.verify()
    }

    @Test
    fun register_returns_success_normal_case() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/register")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .body(
                        objectMapper.writeValueAsString(
                            UserRegisterResponseDto(
                                registeredId = userLoginRequestDto.userName
                            )
                        )
                    )
            )

        // Successful Test
        assertThat(serverCommunication.login(userLoginRequestDto, false)).isEqualTo(FunctionResponse.SUCCESS)
        mockServer.verify()
    }

    @Test
    fun register_returns_SERVER_RESPONSE_OK_BUT_WRONG_FORMAT_when_ok_but_wrong_body() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/register")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .body(
                        "Wrong Body!"
                    )
            )

        // Successful Test
        assertThat(serverCommunication.login(userLoginRequestDto, false)).isEqualTo(FunctionResponse.SERVER_RESPONSE_OK_BUT_WRONG_FORMAT)
        mockServer.verify()
    }

    @Test
    fun register_returns_4xx_5xx_when_internal_error() {
        // Without mock server
        assertThat(serverCommunication.login(userLoginRequestDto, false)).isEqualTo(FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX)
    }

    @Test
    fun register_returns_SERVER_RESPONSE_OK_BUT_NO_BODY_when_ok_but_no_body() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/register")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withSuccess(
                    "",
                    MediaType.TEXT_PLAIN
                )
            )

        // Failure Test
        assertThat(serverCommunication.login(userLoginRequestDto, false)).isEqualTo(FunctionResponse.SERVER_RESPONSE_OK_BUT_NO_BODY)
        mockServer.verify()
    }

    @Test
    fun register_returns_4xx_5xx_when_conflict() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/register")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.CONFLICT)
                    .body(
                        objectMapper.writeValueAsString(
                            ErrorResponse(
                                errorMessage = "testing_error",
                                statusCode = "409",
                                statusMessage = "CONFLICT"
                            )
                        )
                    )
            )

        // Failure Test
        assertThat(serverCommunication.login(userLoginRequestDto, false)).isEqualTo(FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX)
        mockServer.verify()
    }

    @Test
    fun register_returns_4xx_5xx_when_internal_error_response() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/register")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                        objectMapper.writeValueAsString(
                            ErrorResponse(
                                errorMessage = "Internal Unknown!",
                                statusCode = "5XX",
                                statusMessage = "INTERNAL"
                            )
                        )
                    )
            )

        // Failure Test
        assertThat(serverCommunication.login(userLoginRequestDto, false)).isEqualTo(FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX)
        mockServer.verify()
    }
}