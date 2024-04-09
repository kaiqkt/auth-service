package com.kaiqkt.services.authregistryservice.domain.entities

object UserSampler {
    fun sample() = User(
        id = "01GFPPTXKZ8ZJRG8MF701M0W99",
        fullName = "Shinji ikari",
        email = "shinji@eva01.com",
        addresses = mutableListOf(AddressSampler.sample()),
        password = PasswordSampler.sample()
    )
}