package com.loadbalancer.server.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "CLIENT")
class Client(
    @Id
    @Column(name = "IP", nullable = false)
    var addr : String,
    @Column(name = "SESSION_RENEW_TIME", nullable = false)
    private var sessionRenewTime : LocalDateTime,
    @Column(name = "REQUEST_COUNT", nullable = false)
    private var reqCount : Long,
    @Column(name = "CONNECTION_BROKEN_COUNT", nullable = false)
    private var brokenCount: Long,
) {
    fun increaseBrokenCount(){
        brokenCount++
    }
    fun renewSessionTimeout(){
        sessionRenewTime = LocalDateTime.now()
    }
    fun isExpired(time : LocalDateTime) = sessionRenewTime.isBefore(time)
    fun increaseReqCount() {
        reqCount++
    }
    fun showReqCount(): Long {
        return reqCount
    }
    fun showBrokenCount() : Long{
        return brokenCount
    }
}