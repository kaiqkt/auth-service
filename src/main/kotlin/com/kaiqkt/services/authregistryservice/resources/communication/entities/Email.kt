package com.kaiqkt.services.authregistryservice.resources.communication.entities

data class Email(
    val subject: String,
    val recipient: String,
    val template: Template
)
