package com.kaiqkt.services.authregistryservice.application.controllers

import com.kaiqkt.services.authregistryservice.application.dto.AddressV1Sampler
import com.kaiqkt.services.authregistryservice.application.security.CustomAuthenticationSampler
import com.kaiqkt.services.authregistryservice.domain.entities.AddressSampler
import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.domain.services.AddressService
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder

class AddressControllerTest{
    private val addressService: AddressService = mockk(relaxed = true)
    private val controller: AddressController = AddressController(addressService)

    @Test
    fun `given a address to delete, should delete successfully and return http status 204`() {
        val user = UserSampler.sample()
        val addressId = user.addresses.first().id

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { addressService.delete(any(), any()) } just runs

        val response = controller.delete(addressId)

        verify { addressService.delete(user.id, addressId) }

        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `given a address to create or update, should create successfully successfully and return http status http 204`() {
        val user = UserSampler.sample()
        val request = AddressV1Sampler.sample()

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { addressService.createOrUpdate(any(), any()) } just runs

        val response = controller.createOrUpdate(request)

        verify { addressService.createOrUpdate(user.id, any()) }

        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `given an address id, when exist, should return him`() {
        val user = UserSampler.sample()
        val address = AddressSampler.sample()

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { addressService.find(any(), any()) } returns address

        val response = controller.find(address.id)

        verify { addressService.find(user.id, address.id) }

        Assertions.assertEquals(response.statusCode, HttpStatus.OK)
    }

    @Test
    fun `given an address id, when not exist, should return http status 404`() {
        val user = UserSampler.sample()
        val address = AddressSampler.sample()

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { addressService.find(any(), any()) } returns null

        val response = controller.find(address.id)

        verify { addressService.find(user.id, address.id) }

        Assertions.assertEquals(response.statusCode, HttpStatus.NOT_FOUND)
    }

    @Test
    fun `given an user id, when exist addresses, should return it`() {
        val user = UserSampler.sample()
        val addresses = listOf(AddressSampler.sample())

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { addressService.findAll(any()) } returns addresses

        val response = controller.findAll()

        verify { addressService.findAll(user.id) }

        Assertions.assertEquals(response.statusCode, HttpStatus.OK)
        Assertions.assertEquals(1,response.body?.size)
    }

    @Test
    fun `given an user id, when not exist addresses, should return http status 204`() {
        val user = UserSampler.sample()

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { addressService.findAll(any()) } returns null

        val response = controller.findAll()

        verify { addressService.findAll(user.id) }

        Assertions.assertEquals(response.statusCode, HttpStatus.NOT_FOUND)
    }
}