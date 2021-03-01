package com.kangdroid.client.communication

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class ServerCommunicationTest {
    @Autowired
    private lateinit var serverCommunication: ServerCommunication

    @Test
    fun checkingServerAliveWorkingWell() {
        assertThat(serverCommunication.isServerAlive()).isEqualTo(true)
    }
}