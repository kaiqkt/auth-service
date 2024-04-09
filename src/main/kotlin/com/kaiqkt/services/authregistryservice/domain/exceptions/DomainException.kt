package com.kaiqkt.services.authregistryservice.domain.exceptions

open class DomainException(
    open val type: ErrorType,
    override val message: String
) : Exception(message)