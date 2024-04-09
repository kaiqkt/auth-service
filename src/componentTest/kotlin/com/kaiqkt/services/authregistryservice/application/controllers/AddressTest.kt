package com.kaiqkt.services.authregistryservice.application.controllers

import com.github.kittinunf.fuel.core.Headers
import com.kaiqkt.commons.crypto.jwt.JWTUtils
import com.kaiqkt.commons.security.auth.ROLE_USER
import com.kaiqkt.services.authregistryservice.ApplicationIntegrationTest
import com.kaiqkt.services.authregistryservice.application.dto.AddressV1Sampler
import com.kaiqkt.services.authregistryservice.domain.entities.AddressSampler
import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.generated.application.dto.AddressV1
import io.azam.ulidj.ULID
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.jvm.optionals.getOrNull

class AddressTest: ApplicationIntegrationTest() {

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `given a address to delete, when exist, should update successfully and return http status 204`() {
        val user = UserSampler.sample()
        user.addresses.add(AddressSampler.sample().copy(id = ULID.random()))
        val address = AddressV1Sampler.sample()

        val token = JWTUtils.generateToken(user.id, customerSecret, listOf(ROLE_USER), ULID.random(), sessionExpiration.toLong())

        userRepository.save(user)

        webTestClient
            .delete()
            .uri("/address/${address.id}")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus()
            .isNoContent

        val userUpdated = userRepository.findById(user.id).getOrNull()!!

        Assertions.assertEquals(1, userUpdated.addresses.size)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `given a address to create, when not exists, should persist successfully and return http status 200`() {
        val request = AddressV1Sampler.sample().copy(id = ULID.random(), street = "Rua abc")
        val user = UserSampler.sample()

        val token =
            JWTUtils.generateToken(
                user.id,
                customerSecret,
                listOf(ROLE_USER),
                ULID.random(),
                sessionExpiration.toLong()
            )

        userRepository.save(user)

        webTestClient
            .put()
            .uri("/address")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_address_v1+json"))
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isNoContent

        val userUpdated = userRepository.findById(user.id).getOrNull()!!

        Assertions.assertEquals(userUpdated.addresses.size, 2)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `given a address to update, when exist, should update successfully and return http status 200`() {
        val request = AddressV1Sampler.sample().copy(street = "Rua abc")
        val anotherAddress = AddressSampler.sample().copy(id = ULID.random())
        val user = UserSampler.sample().apply {
            addresses.add(anotherAddress)
        }

        val token =
            JWTUtils.generateToken(
                user.id,
                customerSecret,
                listOf(ROLE_USER),
                ULID.random(),
                sessionExpiration.toLong()
            )

        userRepository.save(user)

        webTestClient
            .put()
            .uri("/address")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_address_v1+json"))
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isNoContent

        val userUpdated = userRepository.findById(user.id).getOrNull()!!
        val updated = userUpdated.addresses.first { it.id == request.id }

        Assertions.assertEquals(userUpdated.addresses.size, 2)
        Assertions.assertEquals(request.street, updated.street)
    }

    @Test
    fun `given a address to update, when not exist, should return http status 404`() {
        val address = AddressV1Sampler.sample()
        val user = UserSampler.sample()
        user.addresses.add(AddressSampler.sample().copy(id = ULID.random()))

        val token =
            JWTUtils.generateToken(
                user.id,
                customerSecret,
                listOf(ROLE_USER),
                ULID.random(),
                sessionExpiration.toLong()
            )

        userRepository.save(user)

        webTestClient
            .put()
            .uri("/address")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_address_v1+json"))
            .bodyValue(address)
            .exchange()
            .expectStatus()
            .isNoContent
    }

    @Test
    fun `given a address to find, when exist, should return http status 200 and him`() {
        val user = UserSampler.sample()

        val token =
            JWTUtils.generateToken(
                user.id,
                customerSecret,
                listOf(ROLE_USER),
                ULID.random(),
                sessionExpiration.toLong()
            )

        userRepository.save(user)

        webTestClient
            .get()
            .uri("/address/01GKNAXJG7T7QPRC8JT87DVHT8")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(AddressV1::class.java)
            .consumeWith { response ->
                val body = response.responseBody
                Assertions.assertNotNull(body)
            }
    }

    @Test
    fun `given a address to find, when not exist, should return http status 404`() {
        val user = UserSampler.sample().copy(addresses = mutableListOf())

        val token =
            JWTUtils.generateToken(
                user.id,
                customerSecret,
                listOf(ROLE_USER),
                ULID.random(),
                sessionExpiration.toLong()
            )

        userRepository.save(user)

        webTestClient
            .get()
            .uri("/address/01GKNAXJG7T7QPRC8JT87DVHT8")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `given a user id, when exist addresses for him, should return http status 200 and the list of addresses`() {
        val user = UserSampler.sample()

        val token =
            JWTUtils.generateToken(
                user.id,
                customerSecret,
                listOf(ROLE_USER),
                ULID.random(),
                sessionExpiration.toLong()
            )

        userRepository.save(user)

        webTestClient
            .get()
            .uri("/address")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList(AddressV1::class.java)
            .consumeWith<WebTestClient.ListBodySpec<AddressV1>> {
                val addresses = it.responseBody

                Assertions.assertEquals(1, addresses?.size)
            }
    }

    @Test
    fun `given a user id, when exist not addresses for him, should return http status 404 `() {
        val user = UserSampler.sample().copy(addresses = mutableListOf())

        val token =
            JWTUtils.generateToken(
                user.id,
                customerSecret,
                listOf(ROLE_USER),
                ULID.random(),
                sessionExpiration.toLong()
            )

        userRepository.save(user)

        webTestClient
            .get()
            .uri("/address")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus()
            .isNotFound
    }

}