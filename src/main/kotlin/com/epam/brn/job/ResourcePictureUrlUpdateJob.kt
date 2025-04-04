package com.epam.brn.job

import com.epam.brn.model.Resource
import com.epam.brn.service.ResourceService
import com.epam.brn.service.cloud.CloudService
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

                response.inDefaultFolderPicturesCount = defaultFolderPictures.size
                response.inUnverifiedFolderPicturesCount = unverifiedFolderPictures.size

                val resources = resourceService.findAll()
                for (resource in resources) {
                    var fileUrl = String()
                    if (defaultFolderPictures.containsKey(resource.word)) {
                        fileUrl = defaultFolderPictures[resource.word].toString()
                        response.withCorrectDefaultUrlResources++
                    } else if (unverifiedFolderPictures.containsKey(resource.word)) {
                        fileUrl = unverifiedFolderPictures[resource.word].toString()
                        response.withUnverifiedUrlResources++
                    } else {
                        log.debug("No picture for ${resource.word} found")
                        response.resourcesWithoutPictures++
                    }

                    val shouldUpdateUrl = fileUrl.isNotEmpty() && fileUrl != resource.pictureFileUrl
                    val shouldCleanUrl = fileUrl.isEmpty() && !resource.pictureFileUrl.isNullOrEmpty()
                    if (shouldUpdateUrl || shouldCleanUrl) {
                        if (shouldUpdateUrl)
                            response.updatedPicturedResources++
                        else
                            response.cleanedResourcePicturesFromResource++
                        resource.pictureFileUrl = fileUrl
                        updatedResources.add(resource)
                    }
                }
                if (updatedResources.isNotEmpty())
                    resourceService.saveAll(updatedResources)
            } catch (e: Exception) {
                response.success = false
                response.errorMessage = e.message.orEmpty()
                log.error("Error updating picture URLs for resources", e)
            }
        }
        val logStatement =
            """
                End Job: update picture URLs
                Success: ${response.success}, message: ${response.errorMessage}
                Pictures in Default folder: ${response.inDefaultFolderPicturesCount}
                Pictures in Unverified folder: ${response.inUnverifiedFolderPicturesCount}
                Used default URLs count: ${response.withCorrectDefaultUrlResources}
                Used unverified URLs count: ${response.withUnverifiedUrlResources}
                Without pictures resources: ${response.resourcesWithoutPictures}
                Updated resources: ${response.updatedPicturedResources}
                Cleaned pictures from resources: ${response.cleanedResourcePicturesFromResource}
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
    var withCorrectDefaultUrlResources: Int = 0,
    var withUnverifiedUrlResources: Int = 0,
    var resourcesWithoutPictures: Int = 0,
    var updatedPicturedResources: Int = 0,
    var cleanedResourcePicturesFromResource: Int = 0,
    var executionTime: Long = 0,
    var errorMessage: String = "No errors!"
)
