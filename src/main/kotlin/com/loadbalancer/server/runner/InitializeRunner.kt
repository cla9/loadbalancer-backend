package com.loadbalancer.server.runner

import com.loadbalancer.server.config.ServerConfig
import com.loadbalancer.server.domain.Backend
import com.loadbalancer.server.repository.BackendRepository
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class InitializeRunner(private val config : ServerConfig, private val repo : BackendRepository) : ApplicationRunner{
    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun run(args: ApplicationArguments?) {
        log.info("registered backend : ${config.getId()}")
        val id = config.getId()

        val backend = repo.findBackendById(id).map {
            it.reset()
            it
        }.orElse(Backend(id = id, isExpired = false, count = 0))

        repo.save(backend)
    }

    @PreDestroy
    @Transactional
    fun destroy(){
        log.info("expired backend")
        repo.findBackendById(config.getId())
            .ifPresent {
                it.expired()
                repo.save(it)
            }
    }
}