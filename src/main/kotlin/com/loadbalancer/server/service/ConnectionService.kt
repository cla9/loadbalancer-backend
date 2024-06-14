package com.loadbalancer.server.service

import com.loadbalancer.server.config.ServerConfig
import com.loadbalancer.server.domain.Client
import com.loadbalancer.server.domain.ClientBrokenHistory
import com.loadbalancer.server.domain.ClientConnection
import com.loadbalancer.server.repository.BackendRepository
import com.loadbalancer.server.repository.ClientConnectionBrokenHistoryRepository
import com.loadbalancer.server.repository.ClientConnectionRepository
import com.loadbalancer.server.repository.ClientRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ConnectionService(
    private val repo: ClientRepository,
    private val connectionRepo: ClientConnectionRepository,
    private val backendRepo: BackendRepository,
    private val brokenHistoryRepository: ClientConnectionBrokenHistoryRepository,
    private val config: ServerConfig,
) {
    @Transactional
    fun process(clientAddr: String): Boolean {
        val remoteClient = repo.findById(clientAddr)
        val backendId = config.getId()
        val backend = backendRepo.findById(config.getId()).get()
        var isFailed = false

        val client = if (remoteClient.isPresent) {
            val client = remoteClient.get()
            val isClientSessionTimeout = isExpiredSessionTimeout(client)

            if (!isClientSessionTimeout && backend.isExpired()) {
                isFailed = true
            }

            val matchedBackend = findMatchedBackend(client)
            if (matchedBackend.isNotEmpty() && matchedBackend != backendId) {
                isFailed = true
                connectionRepo.findDistinctByClientAndClosed(client.addr, false)
                    .get()
                    .also { it.closeConnection() }
                    .also { connectionRepo.save(it) }

                connectionRepo.save(
                    ClientConnection(client = clientAddr, backendId = backendId, count = 0)
                )
            }

            if (isFailed) {
                client.increaseBrokenCount()
                brokenHistoryRepository.save(
                    ClientBrokenHistory(
                        addr = clientAddr,
                        asIsBackend = matchedBackend.ifEmpty { backendId },
                        toBeBackend = backendId
                    )
                )

            }

            client.renewSessionTimeout()
            client.increaseReqCount()
            client
        } else {
            Client(
                addr = clientAddr,
                sessionRenewTime = LocalDateTime.now(),
                reqCount = 1,
                brokenCount = 0,
            )
        }


        val connection = connectionRepo.findByClientAndBackendIdAndClosed(client = clientAddr, backendId = backendId, closed = false)
            .orElse(ClientConnection(client = clientAddr, backendId = backendId, count = 0))
        connection.changeBackend(backendId)
        connection.increaseCount()
        backend.increaseCount()

        repo.save(client)
        connectionRepo.save(connection)
        backendRepo.save(backend)

        return !isFailed
    }

    private fun isExpiredSessionTimeout(client: Client): Boolean {
        return client.isExpired(LocalDateTime.now().minusMinutes(5L))
    }

    private fun findMatchedBackend(client: Client): String {
        return connectionRepo.findDistinctByClientAndClosed(client.addr, false)
            .map { it.backendId }
            .orElseGet { "" }
    }
}