package com.loadbalancer.server.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "BACKEND")
class Backend(
    @Id
    @Column(name = "ID", nullable = false)
    val id : String,
    @Column(name = "EXPIRED", nullable = false)
    private var isExpired : Boolean,
    @Column(name = "CONNECTION_COUNT", nullable = false)
    private var count : Long
) {
    fun expired(){
        isExpired = true
    }
    fun reset(){
        isExpired = false
        count = 0
    }
    fun isExpired() = isExpired
    fun increaseCount(){
        count++;
    }
    fun showCount(): Long {
        return count
    }
}