package com.kaiqkt.services.authregistryservice.domain.exceptions

class AlreadyInUseException(override var type: ErrorType) : DomainException(type, "Already in use")