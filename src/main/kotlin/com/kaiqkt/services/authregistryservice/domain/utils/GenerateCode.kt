package com.kaiqkt.services.authregistryservice.domain.utils

import java.util.*

fun randomSixCharNumber(): String {
    val number: Int = Random().nextInt(999999)

    return String.format("%06d", number)
}