package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.generated.application.dto.LoginV1

object LoginV1Sampler {
    fun sample() = LoginV1(
        email = "shinji@eva01.com",
        password = "1234657"
    )

    fun sampleInvalidPassword() = LoginV1(
        email = "shinji@eva01.com",
        password = "Aa#3457"
    )
}