package com.kangdroid.client.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@ConfigurationProperties(prefix = "kdr")
@Component
class ServerConfigurationComponent {

    // Server host, i.e localhost or 127.0.0.1
    lateinit var masterServerHost: String

    // Server port, i.e 8080
    lateinit var masterServerPort: String

    // Server Scheme, i.e http or https
    lateinit var masterServerScheme: String

    // Final Server Address: To be init-ed by postconstruct
    lateinit var masterServerAddress: String

    @PostConstruct
    fun initServerAddress() {
        masterServerAddress = "$masterServerScheme://$masterServerHost:$masterServerPort"
    }
}