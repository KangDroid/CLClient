package com.kangdroid.client.main

import com.kangdroid.client.communication.ServerCommunication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class MainEntry {
    @Autowired
    private lateinit var serverCommunication: ServerCommunication

    /**
     * Main Entry starts here!
     */
    @PostConstruct
    fun startMain() {
    }
}