package com.loadbalancer.server.config

import com.loadbalancer.server.exception.BrokenException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class GlobalControllerAdvice(private val config : ServerConfig) {
    @ExceptionHandler(BrokenException::class)
    protected fun handleBrokenException(e : BrokenException, request : HttpServletRequest) : ResponseEntity<String>{
        return ResponseEntity.badRequest().body("Connection broken is occurred from ${config.getId()}")
    }
}