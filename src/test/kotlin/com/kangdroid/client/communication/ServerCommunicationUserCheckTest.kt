package com.kangdroid.client.communication

import com.fasterxml.jackson.databind.ObjectMapper
import com.kangdroid.client.communication.dto.ErrorResponse
import com.kangdroid.client.communication.dto.UserImageListResponseDto
import com.kangdroid.client.communication.dto.UserInformationResponseDto
import com.kangdroid.client.error.UserRoles
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators

@RunWith(SpringRunner::class)
@SpringBootTest
class ServerCommunicationUserCheckTest {

    @Autowired
    private lateinit var serverCommunication: ServerCommunication

    // Object Mapper
    private val objectMapper: ObjectMapper = ObjectMapper()

    // Mock Server
    private val serverAddress: String = "http://localhost:8080"
    private lateinit var mockServer: MockRestServiceServer
    private lateinit var clientHttpRequestFactory: ClientHttpRequestFactory

    @Before
    fun initTest() {
        // Backup Original Server Communication
        clientHttpRequestFactory = serverCommunication.restTemplate.requestFactory

        // Setup token
        ReflectionTestUtils.setField(serverCommunication, "token", "TEST_TOKEN")
    }

    @After
    fun deInitTest() {
        // Restore RequestFactory
        serverCommunication.restTemplate.requestFactory = clientHttpRequestFactory

        // De-Init Token
        ReflectionTestUtils.setField(serverCommunication, "token", null)
    }

    @Test
    fun is_userCheck_returns_ERROR_no_token() {
        // De-Init Token
        ReflectionTestUtils.setField(serverCommunication, "token", null)

        assertThat(serverCommunication.checkUser()).isEqualTo(UserRoles.ERROR)
    }

    @Test
    fun is_userCheck_returns_ERROR_server_failure() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/info")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                        objectMapper.writeValueAsString(
                            ErrorResponse(
                                "Internal Server Error"
                            )
                        )
                    )
            )

        assertThat(serverCommunication.checkUser()).isEqualTo(UserRoles.ERROR)
    }

    @Test
    fun is_userCheck_returns_ERROR_empty_body() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/info")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .body(
                        ""
                    )
            )

        assertThat(serverCommunication.checkUser()).isEqualTo(UserRoles.ERROR)
    }

    @Test
    fun is_userCheck_returns_ERROR_wrong_body() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/info")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .body(
                        "wrong-body-somehow"
                    )
            )

        assertThat(serverCommunication.checkUser()).isEqualTo(UserRoles.ERROR)
    }

    @Test
    fun is_userCheck_returns_USER_OK() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/info")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .body(
                        objectMapper.writeValueAsString(
                            UserInformationResponseDto(
                                userName = "test",
                                userRole = setOf("ROLE_USER")
                            )
                        )
                    )
            )

        assertThat(serverCommunication.checkUser()).isEqualTo(UserRoles.USER)
    }

    @Test
    fun is_userCheck_returns_ADMIN_OK() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/info")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .body(
                        objectMapper.writeValueAsString(
                            UserInformationResponseDto (
                                userName = "test",
                                userRole = setOf("ROLE_ADMIN")
                            )
                        )
                    )
            )

        assertThat(serverCommunication.checkUser()).isEqualTo(UserRoles.ADMIN)
    }

}