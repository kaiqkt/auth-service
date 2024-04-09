package com.kaiqkt.services.authregistryservice.resources.swagger

import org.springdoc.core.SpringDocConfigProperties
import org.springdoc.core.SpringDocConfiguration
import org.springdoc.core.providers.ObjectMapperProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SpringDocsConfiguration {
    @Bean
    fun springDocConfiguration(): SpringDocConfiguration? {
        return SpringDocConfiguration()
    }

    @Bean
    fun springDocConfigProperties(): SpringDocConfigProperties? {
        return SpringDocConfigProperties()
    }

    @Bean
    fun objectMapperProvider(springDocConfigProperties: SpringDocConfigProperties?): ObjectMapperProvider? {
        return ObjectMapperProvider(springDocConfigProperties)
    }
}
