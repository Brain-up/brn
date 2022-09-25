package com.epam.brn.config

import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.OperationBuilderPlugin
import springfox.documentation.spi.service.contexts.OperationContext
import springfox.documentation.spring.web.DescriptionResolver
import springfox.documentation.swagger.common.SwaggerPluginSupport
import javax.annotation.security.RolesAllowed

@Component
class OperationRolesAllowedReader(
    @Autowired private val descriptions: DescriptionResolver
) : OperationBuilderPlugin {

    private val log = logger()

    override fun apply(context: OperationContext) {
        try {
            var allowedRoles: Array<String>? = null
            val rolesAllowedAnnotation = context.findAnnotation(RolesAllowed::class.java)
            if (rolesAllowedAnnotation.isPresent)
                allowedRoles = rolesAllowedAnnotation.get().value
            else {
                val rolesAllowedControllerAnnotation = context.findControllerAnnotation(RolesAllowed::class.java)
                if (rolesAllowedControllerAnnotation.isPresent) {
                    allowedRoles = rolesAllowedControllerAnnotation.get().value
                }
            }

            if (allowedRoles != null) {
                val apiRoleNote = StringBuilder("Roles Allowed: ")
                apiRoleNote.append(allowedRoles.joinToString(","))
                context.operationBuilder().notes(descriptions.resolve(apiRoleNote.toString()))
            }
        } catch (e: Exception) {
            log.error("Error while creating Swagger docs for allowed roles", e)
        }
    }

    override fun supports(delimiter: DocumentationType?) = SwaggerPluginSupport.pluginDoesApply(delimiter)
}
