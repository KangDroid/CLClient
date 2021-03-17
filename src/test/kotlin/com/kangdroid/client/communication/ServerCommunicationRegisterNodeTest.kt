package com.kangdroid.client.communication

import com.fasterxml.jackson.databind.ObjectMapper
import com.kangdroid.client.communication.dto.ErrorResponse
import com.kangdroid.client.communication.dto.NodeSaveRequestDto
import com.kangdroid.client.communication.dto.NodeSaveResponseDto
import com.kangdroid.client.error.FunctionResponse
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
class ServerCommunicationRegisterNodeTest {
    @Autowired
    private lateinit var serverCommunication: ServerCommunication

    // Object Mapper
    private val objectMapper: ObjectMapper = ObjectMapper()

    // Mock Server
    private val serverAddress: String = "http://localhost:8080"
    private lateinit var mockServer: MockRestServiceServer
    private lateinit var clientHttpRequestFactory: ClientHttpRequestFactory

    private val nodeSaveRequestDto: NodeSaveRequestDto = NodeSaveRequestDto(
        id = 0,
        hostName = "testHost",
        hostPort = "testPort",
        ipAddress = "testIPAddress"
    )

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
    fun is_registerNode_returns_CLIENT_NO_TOKEN_no_token() {
        // De-Init Token
        ReflectionTestUtils.setField(serverCommunication, "token", null)

        assertThat(serverCommunication.registerNode(nodeSaveRequestDto)).isEqualTo(FunctionResponse.CLIENT_NO_TOKEN)
    }

    @Test
    fun is_registerNode_returns_SERVER_FAIL_INTERNAL_ERROR() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/admin/node/register")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
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

        assertThat(serverCommunication.registerNode(nodeSaveRequestDto))
            .isEqualTo(FunctionResponse.SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX)
    }

    @Test
    fun is_registerNode_returns_NO_BODY_empty_body() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/admin/node/register")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .body(
                        ""
                    )
            )

        assertThat(serverCommunication.registerNode(nodeSaveRequestDto))
            .isEqualTo(FunctionResponse.SERVER_RESPONSE_OK_BUT_NO_BODY)
    }

    @Test
    fun is_registerNode_returns_WRONG_FORMAT_crazy_body() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/admin/node/register")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .body(
                        "what?"
                    )
            )

        assertThat(serverCommunication.registerNode(nodeSaveRequestDto))
            .isEqualTo(FunctionResponse.SERVER_RESPONSE_OK_BUT_WRONG_FORMAT)
    }

    @Test
    fun is_registerNode_returns_SUCCESS() {
        // Setup mockServer
        mockServer = MockRestServiceServer.bindTo(serverCommunication.restTemplate)
            .ignoreExpectOrder(true).build()
        mockServer.expect(
            ExpectedCount.manyTimes(),
            MockRestRequestMatchers.requestTo("$serverAddress/api/admin/node/register")
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .body(
                        objectMapper.writeValueAsString(
                            NodeSaveResponseDto(
                                ipAddress = "ipAddress",
                                hostPort = "TestHostPort",
                                regionName = "Region-1",
                            )
                        )
                    )
            )

        assertThat(serverCommunication.registerNode(nodeSaveRequestDto))
            .isEqualTo(FunctionResponse.SUCCESS)
    }
}