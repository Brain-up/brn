package com.epam.brn.job

import com.epam.brn.service.ResourceService
import com.epam.brn.service.cloud.CloudService
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.system.measureTimeMillis

@Component
class ResourceUrlUpdateJob(
    private val cloudService: CloudService,
    private val resourceService: ResourceService
) {

    @Value("\${brn.resources.default-pictures.path}")
    lateinit var defaultPicturesPath: String

    @Value("\${brn.resources.unverified-pictures.path}")
    lateinit var unverifiedPicturesPath: String

    private val log = logger()

    @Scheduled(cron = "\${brn.resources.pictures.update-job.cron}")
    fun updatePictureUrl(): ResourceUrlUpdateJobResponse {
        log.info("Start Job: update picture URLs")
        val response = ResourceUrlUpdateJobResponse()
        val executionTime = measureTimeMillis {
            try {
                val defaultFolderPictures = cloudService.getFilePathMap(defaultPicturesPath)
                val unverifiedFolderPictures = cloudService.getFilePathMap(unverifiedPicturesPath)

                val resources = resourceService.findAll()
                for (resource in resources) {
                    var fileUrl = String()
                    if (defaultFolderPictures.containsKey(resource.word)) {
                        fileUrl = defaultFolderPictures[resource.word].toString()
                        response.defaultCount++
                    } else if (unverifiedFolderPictures.containsKey(resource.word)) {
                        fileUrl = unverifiedFolderPictures[resource.word].toString()
                        response.unverifiedCount++
                    } else {
                        log.debug("No picture for ${resource.word} found")
                        response.notFoundCount++
                    }

                    if (fileUrl.isNotEmpty()) {
                        resource.pictureFileUrl = fileUrl
                        resourceService.save(resource)
                    }
                }
            } catch (e: Exception) {
                response.success = false
                response.errorMessage = e.message
                log.error("Error updating picture URLs for resources", e)
            }
        }
        val logStatement =
            """
                End Job: update picture URLs
                Success: ${response.success}
                Used default URLs count: ${response.defaultCount}
                Used unverified URLs count: ${response.unverifiedCount}
                Not found count: ${response.notFoundCount}
                Execution Time: ${executionTime / 1000}s
            """
        log.info(logStatement)
        response.executionTime = executionTime
        return response
    }
}

data class ResourceUrlUpdateJobResponse(
    var success: Boolean = true,
    var defaultCount: Int = 0,
    var unverifiedCount: Int = 0,
    var notFoundCount: Int = 0,
    var executionTime: Long = 0,
    var errorMessage: String? = null
)
