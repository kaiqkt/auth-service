package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.domain.entities.Address
import com.kaiqkt.services.authregistryservice.generated.application.dto.AddressV1

fun Address.toV1() = AddressV1(
    id = this.id,
    zipCode = this.zipCode,
    street = this.street,
    number = this.number,
    district = this.district,
    city = this.city,
    state = this.state,
    complement = this.complement
)

fun AddressV1.toDomain() = Address(
    id = this.id,
    zipCode = this.zipCode,
    street = this.street,
    number = this.number,
    district = this.district,
    city = this.city,
    state = this.state,
    complement = this.complement
)