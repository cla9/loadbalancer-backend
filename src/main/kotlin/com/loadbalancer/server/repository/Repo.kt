package com.loadbalancer.server.repository

import com.loadbalancer.server.domain.*
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BackendRepository : JpaRepository<Backend, String>{
    fun findBackendById(id : String) : Optional<Backend>
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findById(id: String): Optional<Backend>
}

@Repository
interface ClientConnectionBrokenHistoryRepository : JpaRepository<ClientBrokenHistory, String>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findAllByAddr(addr : String) : List<ClientBrokenHistory>
}


@Repository
interface ClientConnectionRepository : JpaRepository<ClientConnection, Long>{
    fun findDistinctByClientAndClosed(client : String, closed : Boolean)  : Optional<ClientConnection>
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByClientAndBackendIdAndClosed(client: String, backendId : String, closed: Boolean)  : Optional<ClientConnection>

    fun countAllByClient(client : String) : Long
}

@Repository
interface ClientRepository : JpaRepository<Client, String>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findById(id: String): Optional<Client>
}
