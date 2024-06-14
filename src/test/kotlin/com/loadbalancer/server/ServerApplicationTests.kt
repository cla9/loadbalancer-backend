package com.loadbalancer.server

import com.loadbalancer.server.config.ServerConfig
import com.loadbalancer.server.repository.BackendRepository
import com.loadbalancer.server.repository.ClientConnectionBrokenHistoryRepository
import com.loadbalancer.server.repository.ClientConnectionRepository
import com.loadbalancer.server.repository.ClientRepository
import com.loadbalancer.server.service.ConnectionService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest(properties = ["classpath:/application-test.yaml"])
@Transactional
@TestPropertySource(locations = ["classpath:application-test.yaml"])
class ServerApplicationTests {
	@Autowired
	lateinit var serverConfig: ServerConfig

	@Autowired
	lateinit var backendRepo : BackendRepository

	@Autowired
	lateinit var clientConnectionRepo : ClientConnectionRepository

	@Autowired
	lateinit var clientConnectionBrokenHistoryRepo : ClientConnectionBrokenHistoryRepository

	@Autowired
	lateinit var clientRepo : ClientRepository

	@Autowired
	lateinit var service : ConnectionService

	@Test
	fun `최초 1회 요청에 대한 정상 처리`() {
		//given
		val clientAddr = "127.0.0.1"

		//when
		service.process(clientAddr)

		//then
		assertEquals(backendRepo.findById(serverConfig.getId()).get().showCount(), 1)
		assertEquals(clientConnectionRepo.findDistinctByClientAndClosed(clientAddr,false).get().showCount(), 1)
		assertEquals(clientRepo.findById(clientAddr).get().showReqCount(),1)
		assertEquals(clientConnectionBrokenHistoryRepo.findAllByAddr(clientAddr).isEmpty(), true)
	}

	@Test
	fun `여러번 요청에 대한 정상 처리`(){
		//given
		val clientAddr = "127.0.0.1"

		//when
		service.process(clientAddr)
		service.process(clientAddr)

		//then
		assertEquals(backendRepo.findById(serverConfig.getId()).get().showCount(), 2)
		assertEquals(clientConnectionRepo.findDistinctByClientAndClosed(clientAddr,false).get().showCount(), 2)
		assertEquals(clientRepo.findById(clientAddr).get().showReqCount(),2)
		assertEquals(clientConnectionBrokenHistoryRepo.findAllByAddr(clientAddr).isEmpty(), true)
	}

	@Test
	fun `다른 Backend 할당된 Client 접속 테스트`(){
		//given
		val clientAddr = "127.0.0.1"

		//when
		service.process(clientAddr)
		val client = clientConnectionRepo.findDistinctByClientAndClosed(clientAddr,false).get()
		client.changeBackend("127.0.0.2:8082")
		clientConnectionRepo.save(client)
		service.process(clientAddr)

		//then
		assertEquals(clientConnectionRepo.countAllByClient(clientAddr), 2)
		val clientResult = clientRepo.findById(clientAddr).get()
		assertEquals(clientResult.showReqCount(),2)
		assertEquals(clientResult.showBrokenCount(),1)
		val brokenHistory = clientConnectionBrokenHistoryRepo.findAllByAddr(clientAddr)
		assertEquals(brokenHistory.isEmpty(), false)
	}

	@Test
	fun `SessionTimeout Client 접속 테스트`(){
		//given
		val clientAddr = "127.0.0.1"

		//when
		service.process(clientAddr)
		val client = clientRepo.findById(clientAddr).get()
		val sessionTimeField = client.javaClass.getDeclaredField("sessionRenewTime")
		sessionTimeField.isAccessible = true
		sessionTimeField.set(client, LocalDateTime.now().minusMinutes(10L))
		sessionTimeField.isAccessible = false
		clientRepo.save(client)
		service.process(clientAddr)

		//then
		assertEquals(clientConnectionRepo.findDistinctByClientAndClosed(clientAddr,false).get().showCount(), 2)
		assertEquals(clientConnectionRepo.countAllByClient(clientAddr), 1)
		val clientResult = clientRepo.findById(clientAddr).get()
		assertEquals(clientResult.showReqCount(),2)
		assertEquals(clientResult.showBrokenCount(),0)
		val brokenHistory = clientConnectionBrokenHistoryRepo.findAllByAddr(clientAddr)
		assertEquals(brokenHistory.isEmpty(), true)
	}

	@Test
	fun `Backend Expired 접속 테스트`(){
		//given
		val clientAddr = "127.0.0.1"

		//when
		service.process(clientAddr)
		val backend = backendRepo.findById(serverConfig.getId()).get()
		backend.expired()
		backendRepo.save(backend)
		service.process(clientAddr)

		//then
		assertEquals(clientConnectionRepo.findDistinctByClientAndClosed(clientAddr,false).get().showCount(), 2)
		val clientResult = clientRepo.findById(clientAddr).get()
		assertEquals(clientResult.showReqCount(),2)
		assertEquals(clientResult.showBrokenCount(),1)
		val brokenHistory = clientConnectionBrokenHistoryRepo.findAllByAddr(clientAddr)
		assertEquals(brokenHistory.isEmpty(), false)
	}
}
