package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.generated.application.dto.NewUserV1

object NewUserV1Sampler {
    fun sample() = NewUserV1(
        fullName = "Shinji Ikari",
        email = "shinji@eva01.com",
        password = "Aa#34578"
    )
}