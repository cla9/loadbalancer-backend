package com.loadbalancer.server.controller

import com.loadbalancer.server.config.ServerConfig
import com.loadbalancer.server.exception.BrokenException
import com.loadbalancer.server.service.ConnectionService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
class ClientController(
    private val service : ConnectionService,
    private val config : ServerConfig,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/")
    fun lbTest(req: HttpServletRequest) : ResponseEntity<String>{
        val ipAddresses: String? = req.getHeader("X-Forwarded-For")
        val clientIp = if (ipAddresses == null){
            req.remoteAddr
        }else{
            Arrays.stream(ipAddresses.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()) // Client IP
                .findFirst()
                .orElseGet { req.remoteAddr  }
        }


        log.info("Client 요청 IP $clientIp")
        if(!service.process(clientIp)){
            throw BrokenException()
        }
        return ResponseEntity.ok().body("담당 Backend : ${config.getId()}")
    }

    @GetMapping("/health")
    fun health() : ResponseEntity<String> = ResponseEntity.ok().body("ACTIVE")
}