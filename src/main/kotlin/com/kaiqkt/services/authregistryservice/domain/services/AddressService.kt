package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.services.authregistryservice.domain.entities.Address
import com.kaiqkt.services.authregistryservice.domain.repositories.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class AddressService(
    private val userRepository: UserRepository,
) {

    fun createOrUpdate(userId: String, address: Address) {
        userRepository.createOrUpdateAddress(userId, address)
        logger.info("Address ${address.id} for user $userId persisted successfully")
    }

    fun delete(userId: String, addressId: String) {
        userRepository.deleteAddress(userId, addressId)
        logger.info("Delete address $addressId for user $userId")
    }

    fun find(userId: String, addressId: String): Address? {
        val addresses = userRepository.findByAddressId(userId, addressId)?.addresses
        return addresses?.firstOrNull { it.id == addressId }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun findAll(userId: String): List<Address>? {
        val addresses = userRepository.findById(userId).getOrNull()?.addresses

        if (addresses.isNullOrEmpty()) {
            return null
        }

        return addresses
    }


    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}