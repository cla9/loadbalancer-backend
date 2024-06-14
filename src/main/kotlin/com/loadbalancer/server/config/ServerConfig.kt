package com.loadbalancer.server.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.InetAddress

@Component
class ServerConfig(
    @Value("\${server.port}")
    private val port : String
) {
    private val ip : String = InetAddress.getLocalHost().hostAddress
    fun getId() : String = "$ip:$port"
}