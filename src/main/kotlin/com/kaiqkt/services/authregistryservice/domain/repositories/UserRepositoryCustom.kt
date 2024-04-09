package com.kaiqkt.services.authregistryservice.domain.repositories

import com.kaiqkt.commons.crypto.encrypt.Password
import com.kaiqkt.services.authregistryservice.domain.entities.Address
import com.kaiqkt.services.authregistryservice.domain.entities.User

interface UserRepositoryCustom {
    fun createOrUpdateAddress(userId: String, address: Address)
    fun deleteAddress(userId: String, addressId: String)
    fun findByAddressId(userId: String, addressId: String): User?
    fun updatePassword(userId: String, newPassword: Password)
    fun updateEmail(userId: String, email: String)
}