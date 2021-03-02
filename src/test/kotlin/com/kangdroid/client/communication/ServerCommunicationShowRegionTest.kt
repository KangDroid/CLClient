/**
 * Server Communication Test, for Showing Region
 */
package com.kangdroid.client.communication

import com.fasterxml.jackson.databind.ObjectMapper
import com.kangdroid.client.communication.dto.ErrorResponse
import com.kangdroid.client.communication.dto.NodeInformationResponseDto
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
    fun backupRequestFactory() {
        // Backup Original Server Communication
        clientHttpRequestFactory = serverCommunication.restTemplate.requestFactory
    }

    @After
    fun restoreRequestFactory() {
        // Restore RequestFactory
        serverCommunication.restTemplate.requestFactory = clientHttpRequestFactory
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
        Assertions.assertThat(serverCommunication.showRegion()).isEqualTo(true)
        mockServer.verify()

        // De-Init Token
        ReflectionTestUtils.setField(serverCommunication, "token", null)
    }

    @Test
    fun is_returning_false_token_null() {
        // Without setting fields
        Assertions.assertThat(serverCommunication.showRegion()).isEqualTo(false)
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
        Assertions.assertThat(serverCommunication.showRegion()).isEqualTo(false)
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
        Assertions.assertThat(serverCommunication.showRegion()).isEqualTo(false)
        mockServer.verify()

        // De-Init Token
        ReflectionTestUtils.setField(serverCommunication, "token", null)
    }

    @Test
    fun is_returning_false_without_server() {
        // Setup token
        ReflectionTestUtils.setField(serverCommunication, "token", "TEST_TOKEN")

        // Failure Test
        Assertions.assertThat(serverCommunication.showRegion()).isEqualTo(false)

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
        Assertions.assertThat(serverCommunication.showRegion()).isEqualTo(false)
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
        Assertions.assertThat(serverCommunication.showRegion()).isEqualTo(false)
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
        Assertions.assertThat(serverCommunication.showRegion()).isEqualTo(true)
        mockServer.verify()

        // De-Init Token
        ReflectionTestUtils.setField(serverCommunication, "token", null)
    }
}