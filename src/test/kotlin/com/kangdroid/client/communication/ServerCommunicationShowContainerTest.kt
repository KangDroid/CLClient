package com.kangdroid.client.communication

import com.fasterxml.jackson.databind.ObjectMapper
import com.kangdroid.client.communication.dto.ErrorResponse
import com.kangdroid.client.communication.dto.NodeInformationResponseDto
import com.kangdroid.client.communication.dto.UserImageListResponseDto
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
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators

@RunWith(SpringRunner::class)
@SpringBootTest
class ServerCommunicationShowContainerTest {
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
    fun is_returning_SUCCESS_normal_condition() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/container")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .body(
                        objectMapper.writeValueAsString(
                            arrayOf(
                                UserImageListResponseDto(
                                    userName = "TEST_USERNAME",
                                    dockerId = "docker-test-id",
                                    computeRegion = "REGION-1"
                                )
                            )
                        )
                    )
            )

        // do normal work
        assertThat(serverCommunication.showClientContainer()).isEqualTo(FunctionResponse.SUCCESS)
    }

    @Test
    fun is_returning_SUCCESS_without_actual_container() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/container")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .body(
                        objectMapper.writeValueAsString(
                            emptyArray<UserImageListResponseDto>()
                        )
                    )
            )

        // do normal work
        assertThat(serverCommunication.showClientContainer()).isEqualTo(FunctionResponse.SUCCESS)
    }

    @Test
    fun is_returning_NO_TOKEN_without_token() {
        deInitTest() // Null-fy token

        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/container")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .body(
                        objectMapper.writeValueAsString(
                            arrayOf(
                                UserImageListResponseDto(
                                    userName = "TEST_USERNAME",
                                    dockerId = "docker-test-id",
                                    computeRegion = "REGION-1"
                                )
                            )
                        )
                    )
            )

        // do normal work
        assertThat(serverCommunication.showClientContainer()).isEqualTo(FunctionResponse.CLIENT_NO_TOKEN)
    }

    @Test
    fun is_returning_4xx_5xx_without_server() {
        assertThat(serverCommunication.showClientContainer()).isEqualTo(FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX)
    }

    @Test
    fun is_returning_4xx_5xx_with_internal_server_error() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/container")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                        objectMapper.writeValueAsString(
                            ErrorResponse(
                                "INTERNAL_SERVER_ERROR"
                            )
                        )
                    )
            )

        // do normal work
        assertThat(serverCommunication.showClientContainer()).isEqualTo(FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX)
    }

    @Test
    fun is_returning_4xx_5xx_with_404_error() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/container")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.NOT_FOUND)
                    .body(
                        objectMapper.writeValueAsString(
                            ErrorResponse(
                                "Username Not Found!"
                            )
                        )
                    )
            )

        // do normal work
        assertThat(serverCommunication.showClientContainer()).isEqualTo(FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX)
    }

    @Test
    fun is_returning_SERVER_RESPONSE_OK_BUT_NO_BODY_response_ok_but_no_body() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/container")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .body(
                        ""
                    )
            )

        // do normal work
        assertThat(serverCommunication.showClientContainer()).isEqualTo(FunctionResponse.SERVER_RESPONSE_OK_BUT_NO_BODY)
    }

    @Test
    fun is_returning_SERVER_RESPONSE_OK_BUT_WRONG_FORMAT_response_ok_but_wrong_body() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/container")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .body(
                        "somewhat random body!"
                    )
            )

        // do normal work
        assertThat(serverCommunication.showClientContainer()).isEqualTo(FunctionResponse.SERVER_RESPONSE_OK_BUT_WRONG_FORMAT)
    }
}