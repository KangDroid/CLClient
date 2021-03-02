package com.kangdroid.client.communication

import com.fasterxml.jackson.databind.ObjectMapper
import com.kangdroid.client.communication.dto.ErrorResponse
import com.kangdroid.client.communication.dto.NodeInformationResponseDto
import com.kangdroid.client.communication.dto.UserLoginRequestDto
import com.kangdroid.client.communication.dto.UserRegisterResponseDto
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
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import java.lang.reflect.Field

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
    fun loginIsSuccessful() {
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
    }

    @Test
    fun isLoginReturnsFalseSuccessCodeWrongBody() {
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
    }

    @Test
    fun isLoginReturnsFalseFailedServer() {
        // Without mock server
        assertThat(serverCommunication.login(userLoginRequestDto)).isEqualTo(false)
    }

    @Test
    fun isLoginReturnsFalseWithoutBody() {
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
    }

    @Test
    fun isLoginReturnsFalseWithWrongID() {
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
    }

    @Test
    fun isLoginReturnsFalseWithWrongPassword() {
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
    }

    @Test
    fun isLoginReturnsFalseInternalUnknownError() {
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
    }

    @Test
    fun registerIsSuccessful() {
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
        assertThat(serverCommunication.login(userLoginRequestDto, false)).isEqualTo(true)
        mockServer.verify()
    }

    @Test
    fun isRegisterReturnsFalseSuccessCodeWrongBody() {
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
        assertThat(serverCommunication.login(userLoginRequestDto, false)).isEqualTo(false)
        mockServer.verify()
    }

    @Test
    fun isRegisterReturnsFalseFailedServer() {
        // Without mock server
        assertThat(serverCommunication.login(userLoginRequestDto, false)).isEqualTo(false)
    }

    @Test
    fun isRegisterReturnsFalseWithoutBody() {
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
        assertThat(serverCommunication.login(userLoginRequestDto, false)).isEqualTo(false)
        mockServer.verify()
    }

    @Test
    fun isRegisterReturnsFalseWithDuplicatedID() {
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
        assertThat(serverCommunication.login(userLoginRequestDto, false)).isEqualTo(false)
        mockServer.verify()
    }

    @Test
    fun isRegisterReturnsFalseInternalUnknownError() {
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
        assertThat(serverCommunication.login(userLoginRequestDto, false)).isEqualTo(false)
        mockServer.verify()
    }

    /**
     * showRegion Test from this line
     * GET!
     */
    @Test
    fun is_showingRegion_works_normally() {
        // Setup token
        ReflectionTestUtils.setField(serverCommunication, "token", "TEST_TOKEN")

        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/node")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .body(
                        objectMapper.writeValueAsString(
                            arrayOf(NodeInformationResponseDto(
                                regionName = "Region-0",
                                nodeLoadPercentage = "10"
                            ))
                        )
                    )
            )

        // Failure Test
        assertThat(serverCommunication.showRegion()).isEqualTo(true)
        mockServer.verify()

        // De-Init Token
        ReflectionTestUtils.setField(serverCommunication, "token", null)
    }

    @Test
    fun is_returning_false_token_null() {
        // Without setting fields
        assertThat(serverCommunication.showRegion()).isEqualTo(false)
    }

    @Test
    fun is_returning_false_internal_server_error() {
        // Setup token
        ReflectionTestUtils.setField(serverCommunication, "token", "TEST_TOKEN")

        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/node")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
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
        assertThat(serverCommunication.showRegion()).isEqualTo(false)
        mockServer.verify()

        // De-Init Token
        ReflectionTestUtils.setField(serverCommunication, "token", null)
    }

    @Test
    fun is_returning_false_forbidden() {
        // Setup token
        ReflectionTestUtils.setField(serverCommunication, "token", "TEST_TOKEN")

        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/node")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.FORBIDDEN)
                    .body(
                        objectMapper.writeValueAsString(
                            ErrorResponse(
                                errorMessage = "Forbidden!",
                                statusCode = "4XX",
                                statusMessage = "Forbidden"
                            )
                        )
                    )
            )

        // Failure Test
        assertThat(serverCommunication.showRegion()).isEqualTo(false)
        mockServer.verify()

        // De-Init Token
        ReflectionTestUtils.setField(serverCommunication, "token", null)
    }

    @Test
    fun is_returning_false_without_server() {
        // Setup token
        ReflectionTestUtils.setField(serverCommunication, "token", "TEST_TOKEN")

        // Failure Test
        assertThat(serverCommunication.showRegion()).isEqualTo(false)

        // De-Init Token
        ReflectionTestUtils.setField(serverCommunication, "token", null)
    }

    @Test
    fun is_returning_false_ok_but_no_body() {
        // Setup token
        ReflectionTestUtils.setField(serverCommunication, "token", "TEST_TOKEN")

        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/node")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .body(
                        ""
                    )
            )

        // Failure Test
        assertThat(serverCommunication.showRegion()).isEqualTo(false)
        mockServer.verify()

        // De-Init Token
        ReflectionTestUtils.setField(serverCommunication, "token", null)
    }

    @Test
    fun is_returning_false_ok_but_crazy_body() {
        // Setup token
        ReflectionTestUtils.setField(serverCommunication, "token", "TEST_TOKEN")

        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/node")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .body(
                        "somewhat crazy body?"
                    )
            )

        // Failure Test
        assertThat(serverCommunication.showRegion()).isEqualTo(false)
        mockServer.verify()

        // De-Init Token
        ReflectionTestUtils.setField(serverCommunication, "token", null)
    }

    @Test
    fun is_returning_true_empty_node() {
        // Setup token
        ReflectionTestUtils.setField(serverCommunication, "token", "TEST_TOKEN")

        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/node")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .body(
                        objectMapper.writeValueAsString(
                            emptyArray<NodeInformationResponseDto>()
                        )
                    )
            )

        // Failure Test
        assertThat(serverCommunication.showRegion()).isEqualTo(true)
        mockServer.verify()

        // De-Init Token
        ReflectionTestUtils.setField(serverCommunication, "token", null)
    }
}