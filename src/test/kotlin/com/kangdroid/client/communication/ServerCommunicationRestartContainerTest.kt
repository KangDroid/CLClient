package com.kangdroid.client.communication

import com.fasterxml.jackson.databind.ObjectMapper
import com.kangdroid.client.communication.dto.ErrorResponse
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
class ServerCommunicationRestartContainerTest {
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

        // Setup mock list
        val mockContainerList: MutableList<UserImageListResponseDto> =
            mutableListOf(
                UserImageListResponseDto(),
                UserImageListResponseDto()
            )
        ReflectionTestUtils.setField(serverCommunication, "containerList", mockContainerList)
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
        assertThat(serverCommunication.restartClientContainer(10)).isEqualTo(FunctionResponse.CLIENT_NO_TOKEN)
    }

    @Test
    fun is_returning_false_negative_range() {
        assertThat(serverCommunication.restartClientContainer(-1)).isEqualTo(FunctionResponse.WRONG_NUMBER_INPUT)
    }

    @Test
    fun is_returning_false_wrong_positive_range() {
        assertThat(serverCommunication.restartClientContainer(5)).isEqualTo(FunctionResponse.WRONG_NUMBER_INPUT)
    }

    @Test
    fun is_returning_false_wrong_token() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/restart")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.NOT_FOUND)
                    .body(
                        objectMapper.writeValueAsString(
                            ErrorResponse(
                                "Cannot find user with token!"
                            )
                        )
                    )
            )


        assertThat(serverCommunication.restartClientContainer(1)).isEqualTo(FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX)
    }

    @Test
    fun is_returning_false_node_error() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/client/restart")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                        objectMapper.writeValueAsString(
                            ErrorResponse(
                                "Something went wrong when communicating node server!"
                            )
                        )
                    )
            )


        assertThat(serverCommunication.restartClientContainer(1)).isEqualTo(FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX)
    }
}