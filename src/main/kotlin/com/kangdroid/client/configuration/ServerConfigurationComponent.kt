package com.kangdroid.client.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import javax.annotation.PostConstruct

@ConstructorBinding
@ConfigurationProperties("kdr")
data class ServerConfigurationComponent(val master: MasterServerSettings) {
    data class MasterServerSettings(
        // Server host, i.e localhost or 127.0.0.1
        var masterServerHost: String = "",

        // Server port, i.e 8080
        var masterServerPort: String = "",

        // Server Scheme, i.e http or https
        var masterServerScheme: String = "",
    )

    // Final Server Address: To be init-ed by postconstruct
    lateinit var masterServerAddress: String

    @PostConstruct
    fun initServerAddress() {
        masterServerAddress =
            "${master.masterServerScheme}://${master.masterServerHost}:${master.masterServerPort}"
    }
}