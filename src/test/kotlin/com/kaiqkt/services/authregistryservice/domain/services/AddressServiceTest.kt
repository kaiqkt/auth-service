package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.services.authregistryservice.domain.entities.AddressSampler
import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.domain.repositories.UserRepository
import io.azam.ulidj.ULID
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class AddressServiceTest {
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val addressService: AddressService = AddressService(userRepository)

    @Test
    fun `given an address to create, should persist successfully`() {
        val user = UserSampler.sample()
        val address = user.addresses.first()

        every { userRepository.createOrUpdateAddress(any(), any()) } just runs

        addressService.createOrUpdate(user.id, address)

        verify { userRepository.createOrUpdateAddress(user.id, address) }
    }

    @Test
    fun `given an address to delete, should delete successfully`() {
        val user = UserSampler.sample()
        val addressId = user.addresses.first().id

        every { userRepository.deleteAddress(any(), any()) } just runs

        addressService.delete(user.id, addressId)

        verify { userRepository.deleteAddress(user.id, addressId) }
    }

    @Test
    fun `given an address to update, should update the fields which is not null`() {
        val user = UserSampler.sample()
        val address = AddressSampler.sample()

        every { userRepository.createOrUpdateAddress(any(), any()) } just runs

        addressService.createOrUpdate(user.id, address)

        verify { userRepository.createOrUpdateAddress(user.id, address) }
    }

    @Test
    fun `given an address to update, when is all null, should update with the values with is already saved`() {
        val user = UserSampler.sample()
        val address = AddressSampler.sample()

        every { userRepository.createOrUpdateAddress(any(), any()) } just runs

        addressService.createOrUpdate(user.id, address)

        verify { userRepository.createOrUpdateAddress(user.id, address) }
    }

    @Test
    fun `given an address to find, when exists, should return him`() {
        val user = UserSampler.sample()
        val address = AddressSampler.sample()

        every { userRepository.findByAddressId(any(), any()) } returns user

        val response = addressService.find(user.id, address.id)

        verify { userRepository.findByAddressId(user.id, address.id) }

        Assertions.assertNotNull(response)
    }

    @Test
    fun `given an address to find, when not exists the user, should return null`() {
        val user = UserSampler.sample()
        val address = AddressSampler.sample()

        every { userRepository.findByAddressId(any(), any()) } returns null

        val response = addressService.find(user.id, address.id)

        verify { userRepository.findByAddressId(user.id, address.id) }

        Assertions.assertNull(response)
    }

    @Test
    fun `given an address to find, when not exists the address, should return null`() {
        val user = UserSampler.sample().copy(addresses = mutableListOf())
        val id = ULID.random()

        every { userRepository.findByAddressId(any(), any()) } returns null

        val response = addressService.find(user.id, id)

        verify { userRepository.findByAddressId(user.id, id) }

        Assertions.assertNull(response)
    }

    @Test
    fun `given an address to find, when not exists based on the address id, should return null`() {
        val user = UserSampler.sample()
        val id = ULID.random()

        every { userRepository.findByAddressId(any(), any()) } returns user

        val response = addressService.find(user.id, id)

        verify { userRepository.findByAddressId(user.id,id) }

        Assertions.assertNull(response)
    }

    @Test
    fun `given an addresses to find, when not exists the user, should return null`() {
        val user = UserSampler.sample()

        every { userRepository.findById(any()) } returns Optional.empty()

        val response = addressService.findAll(user.id)

        every { userRepository.findById(user.id) }

        Assertions.assertNull(response)
    }

    @Test
    fun `given an addresses to find, when exist address, should return a list with him`() {
        val user = UserSampler.sample()

        every { userRepository.findById(any()) } returns Optional.of(user)

        val response = addressService.findAll(user.id)

        every { userRepository.findById(user.id) }

        Assertions.assertNotNull(response)
    }

    @Test
    fun `given an addresses to find, when not addresses, should return null`() {
        val user = UserSampler.sample().copy(addresses = mutableListOf())

        every { userRepository.findById(any()) } returns Optional.of(user)

        val response = addressService.findAll(user.id)

        every { userRepository.findById(user.id) }

        Assertions.assertNull(response)
    }
}