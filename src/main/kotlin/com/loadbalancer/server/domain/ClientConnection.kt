package com.loadbalancer.server.domain

import jakarta.persistence.*

@Entity
@Table(name = "CLIENT_CONNECT", indexes = [
    Index(name = "CLIENT_CONNECT_IX01", columnList = "CLIENT_IP,CLOSED")
])
class ClientConnection(
    @Column(name = "CLIENT_IP", nullable = false)
    val client: String,
    @Column(name = "BACKEND_ID", nullable = false)
    var backendId: String,
    @Column(name = "CONNECTION_COUNT", nullable = false)
    private var count: Long,
    @Column(name = "CLOSED", nullable = false)
    private var closed : Boolean = false,
) {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id : Long = 0

    fun increaseCount() {
        count++
    }

    fun changeBackend(backendId: String) {
        this.backendId = backendId
    }

    fun showCount(): Long {
        return count
    }
    fun closeConnection(){
        closed = true
    }
}