package com.epam.brn.job

import com.epam.brn.model.Resource
import com.epam.brn.service.ResourceService
import com.epam.brn.service.cloud.CloudService
import io.micrometer.core.instrument.util.StringUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.system.measureTimeMillis

@Component
class ResourcePictureUrlUpdateJob(
    private val cloudService: CloudService,
    private val resourceService: ResourceService
) {

    @Value("\${brn.resources.default-pictures.path}")
    lateinit var defaultPicturesPath: String

    @Value("\${brn.resources.unverified-pictures.path}")
    lateinit var unverifiedPicturesPath: String

    private val log = logger()

    @Scheduled(cron = "\${brn.resources.pictures.update-job.cron}")
    fun updatePictureUrl(): ResourcePictureUrlUpdateJobResponse {
        log.info("Start Job: update picture URLs")
        val response = ResourcePictureUrlUpdateJobResponse()
        val executionTime = measureTimeMillis {
            try {
                val updatedResources = arrayListOf<Resource>()

                val defaultFolderPictures: Map<String, String> = cloudService.getFilePathMap(defaultPicturesPath)
                val unverifiedFolderPictures: Map<String, String> = cloudService.getFilePathMap(unverifiedPicturesPath)

                val resources = resourceService.findAll()
                for (resource in resources) {
                    var fileUrl = String()
                    if (defaultFolderPictures.containsKey(resource.word)) {
                        fileUrl = defaultFolderPictures[resource.word].toString()
                        response.inDefaultFolderPicturesCount++
                    } else if (unverifiedFolderPictures.containsKey(resource.word)) {
                        fileUrl = unverifiedFolderPictures[resource.word].toString()
                        response.inUnverifiedFolderPicturesCount++
                    } else {
                        log.debug("No picture for ${resource.word} found")
                        response.notFoundPictureForWordCount++
                    }

                    val shouldUpdateUrl = fileUrl.isNotEmpty() && !fileUrl.equals(resource.pictureFileUrl)
                    val shouldCleanUrl = fileUrl.isEmpty() && StringUtils.isNotEmpty(resource.pictureFileUrl)
                    if (shouldUpdateUrl || shouldCleanUrl) {
                        resource.pictureFileUrl = fileUrl
                        updatedResources.add(resource)
                    }
                }
                if (updatedResources.isNotEmpty()) {
                    resourceService.saveAll(updatedResources)
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
                Used default URLs count: ${response.inDefaultFolderPicturesCount}
                Used unverified URLs count: ${response.inUnverifiedFolderPicturesCount}
                Not found count: ${response.notFoundPictureForWordCount}
                Execution Time: ${executionTime / 1000}s
            """
        log.info(logStatement)
        response.executionTime = executionTime
        return response
    }
}

data class ResourcePictureUrlUpdateJobResponse(
    var success: Boolean = true,
    var inDefaultFolderPicturesCount: Int = 0,
    var inUnverifiedFolderPicturesCount: Int = 0,
    var notFoundPictureForWordCount: Int = 0,
    var executionTime: Long = 0,
    var errorMessage: String? = null
)
