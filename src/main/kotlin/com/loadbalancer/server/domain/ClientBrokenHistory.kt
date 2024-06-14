package com.loadbalancer.server.domain

import jakarta.persistence.*

@Entity
@Table(name = "CLIENT_BROKEN_HISTORY")
class ClientBrokenHistory(
    @Column(name = "CLIENT_IP", nullable = false)
    var addr : String,
    @Column(name = "AS_IS_BACKEND", nullable = false)
    var asIsBackend : String = "",
    @Column(name = "TO_BE_BACKEND", nullable = false)
    var toBeBackend : String = "",
){
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private var id : Long = 0
}
