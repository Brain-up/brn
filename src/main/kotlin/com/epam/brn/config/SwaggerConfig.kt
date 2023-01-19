package com.epam.brn.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springdoc.core.customizers.OpenApiCustomiser
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.security.RolesAllowed

@Configuration
class SwaggerConfig {

    @Bean
    fun openApi() = OpenAPI().info(apiInfo())

    private fun apiInfo() = Info()
        .title("Brain Up project")
        .description("REST API for brn")
        .contact(
            Contact()
                .name("Elena.Moshnikova")
                .url("https://www.epam.com/")
                .email("brainupproject@yandex.ru")
        )

    @Bean
    fun rolesAllowedCustomizer(): OperationCustomizer? {
        return OperationCustomizer { operation, handlerMethod ->
            var allowedRoles: Array<String>? = null
            var rolesAllowedAnnotation = handlerMethod.getMethodAnnotation(RolesAllowed::class.java)
            if (rolesAllowedAnnotation != null)
                allowedRoles = rolesAllowedAnnotation.value
            else {
                rolesAllowedAnnotation = handlerMethod.method.declaringClass.getAnnotation(RolesAllowed::class.java)
                if (rolesAllowedAnnotation != null)
                    allowedRoles = rolesAllowedAnnotation.value
            }

            val sb = StringBuilder("Roles: ")
            if (allowedRoles != null)
                sb.append("**${allowedRoles.joinToString(",")}**")
            else
                sb.append("**PUBLIC**")

            operation.description?.let {
                sb.append("<br/>")
                sb.append(it)
            }

            operation.description = sb.toString()
            operation
        }
    }

    @Bean
    fun sortTagsCustomiser(): OpenApiCustomiser = OpenApiCustomiser { openApi -> openApi.tags.sortBy { it.name } }
}
