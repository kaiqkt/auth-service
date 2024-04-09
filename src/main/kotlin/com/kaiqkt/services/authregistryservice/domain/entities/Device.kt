package com.kaiqkt.services.authregistryservice.domain.entities

import ua_parser.Parser
import java.io.Serializable

data class Device(
    val os: String,
    val osVersion: String,
    val model: String,
    val appVersion: String
) : Serializable {

    constructor(userAgent: String, appVersion: String) : this(
        os = parseOs(userAgent),
        osVersion = parseOsVersion(userAgent),
        model = parseModel(userAgent),
        appVersion = appVersion
    )

    companion object {
        private const val UNKNOWN = "UNKNOWN"

        private fun parseOs(userAgent: String): String {
            return try {
                Parser().parse(userAgent).os.family
            } catch (e: Exception) {
                UNKNOWN
            }
        }

        private fun parseOsVersion(userAgent: String): String {
            return try {
                Parser().parse(userAgent).os.major
            } catch (e: Exception) {
                UNKNOWN
            }
        }

        private fun parseModel(userAgent: String): String {
            return try {
                Parser().parse(userAgent).device.family
            } catch (e: Exception) {
                UNKNOWN
            }
        }
    }
}
