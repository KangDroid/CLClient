/**
 * Server Communication Test, for Showing Region
 */
package com.kangdroid.client.communication

import com.fasterxml.jackson.databind.ObjectMapper
import com.kangdroid.client.communication.dto.ErrorResponse
import com.kangdroid.client.communication.dto.NodeInformationResponseDto
import com.kangdroid.client.error.FunctionResponse
import org.assertj.core.api.Assertions
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
class ServerCommunicationShowRegionTest {
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

    /**
     * showRegion Test from this line
     * GET!
     */
    @Test
    fun is_showingRegion_works_normally() {
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
                            arrayOf(
                                NodeInformationResponseDto(
                                regionName = "Region-0",
                                nodeLoadPercentage = "10"
                            )
                            )
                        )
                    )
            )

        // Failure Test
        Assertions.assertThat(serverCommunication.showRegion()).isEqualTo(FunctionResponse.SUCCESS)
        mockServer.verify()
    }

    @Test
    fun is_returning_NO_TOKEN_when_token_null() {
        // Cleanup Token first
        ReflectionTestUtils.setField(serverCommunication, "token", null)

        // Without setting fields
        Assertions.assertThat(serverCommunication.showRegion()).isEqualTo(FunctionResponse.CLIENT_NO_TOKEN)
    }

    @Test
    fun is_returning_4xx_5xx_internal_server_error() {
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
        Assertions.assertThat(serverCommunication.showRegion()).isEqualTo(FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX)
        mockServer.verify()
    }

    @Test
    fun is_returning_4xx_5xx_forbidden() {
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
        Assertions.assertThat(serverCommunication.showRegion()).isEqualTo(FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX)
        mockServer.verify()
    }

    @Test
    fun is_returning_4xx_5xx_without_server() {
        // Failure Test
        Assertions.assertThat(serverCommunication.showRegion()).isEqualTo(FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX)
    }

    @Test
    fun is_returning_SERVER_RESPONSE_OK_BUT_NO_BODY_ok_but_no_body() {
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
        Assertions.assertThat(serverCommunication.showRegion()).isEqualTo(FunctionResponse.SERVER_RESPONSE_OK_BUT_NO_BODY)
        mockServer.verify()
    }

    @Test
    fun is_returning_SERVER_RESPONSE_OK_BUT_WRONG_FORMAT_ok_but_crazy_body() {
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
        Assertions.assertThat(serverCommunication.showRegion()).isEqualTo(FunctionResponse.SERVER_RESPONSE_OK_BUT_WRONG_FORMAT)
        mockServer.verify()
    }

    @Test
    fun is_returning_SUCCESS_empty_node() {
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
        Assertions.assertThat(serverCommunication.showRegion()).isEqualTo(FunctionResponse.SUCCESS)
        mockServer.verify()
    }
}