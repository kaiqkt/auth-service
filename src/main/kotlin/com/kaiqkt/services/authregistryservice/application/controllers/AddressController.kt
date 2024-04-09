package com.kaiqkt.services.authregistryservice.application.controllers

import com.kaiqkt.commons.security.auth.AUTHORIZE_USER
import com.kaiqkt.commons.security.auth.getUserId
import com.kaiqkt.services.authregistryservice.application.dto.toDomain
import com.kaiqkt.services.authregistryservice.application.dto.toV1
import com.kaiqkt.services.authregistryservice.domain.services.AddressService
import com.kaiqkt.services.authregistryservice.generated.application.controllers.AddressApi
import com.kaiqkt.services.authregistryservice.generated.application.dto.AddressV1
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController

@RestController
class AddressController(
    private val addressService: AddressService
) : AddressApi {

    @PreAuthorize(AUTHORIZE_USER)
    override fun createOrUpdate(addressV1: AddressV1): ResponseEntity<Unit> {
        addressService.createOrUpdate(getUserId(), addressV1.toDomain())
        return ResponseEntity.noContent().build()
    }

    @PreAuthorize(AUTHORIZE_USER)
    override fun delete(addressId: String): ResponseEntity<Unit> {
        addressService.delete(getUserId(), addressId).also { return ResponseEntity.noContent().build() }
    }

    @PreAuthorize(AUTHORIZE_USER)
    override fun find(addressId: String): ResponseEntity<AddressV1> {
        addressService.find(getUserId(), addressId)?.let {
            return ResponseEntity.ok(it.toV1())
        }

        return ResponseEntity.notFound().build()
    }

    @PreAuthorize(AUTHORIZE_USER)
    override fun findAll(): ResponseEntity<List<AddressV1>> {
        addressService.findAll(getUserId())
            ?.map { it.toV1() }
            ?.also { return ResponseEntity.ok(it) }

        return ResponseEntity.notFound().build()
    }
}