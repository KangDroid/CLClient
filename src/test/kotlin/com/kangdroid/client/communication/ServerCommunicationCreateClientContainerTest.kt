package com.kangdroid.client.communication

import com.fasterxml.jackson.databind.ObjectMapper
import com.kangdroid.client.communication.dto.ErrorResponse
import com.kangdroid.client.communication.dto.UserImageResponseDto
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
class ServerCommunicationCreateClientContainerTest {
    @Autowired
    private lateinit var serverCommunication: ServerCommunication

    // Object Mapper
    private val objectMapper: ObjectMapper = ObjectMapper()

    // Mock Server
    private val serverAddress: String = "http://localhost:8080"
    private lateinit var mockServer: MockRestServiceServer
    private lateinit var clientHttpRequestFactory: ClientHttpRequestFactory

    // Constant Variables
    private val correctRegion: String = "Region-0"
    private val wrongRegion: String = "Wrong Region"

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
    fun is_returning_false_no_token() {
        deInitTest()
        // We do not need to make a mock server
        // since it will throw error as soon as check token does not pass
        assertThat(serverCommunication.createClientContainer(correctRegion)).isEqualTo(FunctionResponse.CLIENT_NO_TOKEN)
    }

    @Test
    fun is_returning_false_wrong_region() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/container")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.NOT_FOUND)
                    .body(
                        objectMapper.writeValueAsString(
                            ErrorResponse(
                                "Cannot find Compute Region!"
                            )
                        )
                    )
            )

        assertThat(serverCommunication.createClientContainer(wrongRegion)).isEqualTo(FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX)
    }

    @Test
    fun is_returning_false_compute_node_down() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/container")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                        objectMapper.writeValueAsString(
                            ErrorResponse(
                                "Cannot communicate with Compute node!"
                            )
                        )
                    )
            )

        assertThat(serverCommunication.createClientContainer(wrongRegion)).isEqualTo(FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX)
    }

    @Test
    fun is_returning_false_compute_node_comm_error() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/container")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                        objectMapper.writeValueAsString(
                            ErrorResponse(
                                "Random Compute Node Error"
                            )
                        )
                    )
            )

        assertThat(serverCommunication.createClientContainer(wrongRegion)).isEqualTo(FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX)
    }

    @Test
    fun is_returning_false_master_server_no_body() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/container")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .body(
                        ""
                    )
            )

        assertThat(serverCommunication.createClientContainer(wrongRegion)).isEqualTo(FunctionResponse.SERVER_RESPONSE_OK_BUT_NO_BODY)
    }

    @Test
    fun is_returning_false_master_server_ok_but_wrong_body() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/container")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .body(
                        "Wrong Body!"
                    )
            )

        assertThat(serverCommunication.createClientContainer(wrongRegion)).isEqualTo(FunctionResponse.SERVER_RESPONSE_OK_BUT_WRONG_FORMAT)
    }

    @Test
    fun is_returning_true_master_normal_work() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/container")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .body(
                        objectMapper.writeValueAsString(
                            UserImageResponseDto(
                                targetIpAddress = "random_ip",
                                targetPort = "random_port",
                                containerId = "random_container_id",
                                regionLocation = "random_location"
                            )
                        )
                    )
            )

        assertThat(serverCommunication.createClientContainer(correctRegion)).isEqualTo(FunctionResponse.SUCCESS)
    }
}