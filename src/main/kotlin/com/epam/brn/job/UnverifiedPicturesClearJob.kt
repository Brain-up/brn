package com.epam.brn.job

import com.epam.brn.service.cloud.CloudService
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UnverifiedPicturesClearJob(
    private val cloudService: CloudService,
) {
    @Value("\${brn.resources.default-pictures.path}")
    lateinit var defaultPicturesPath: String

    @Value("\${brn.resources.unverified-pictures.path}")
    lateinit var unverifiedPicturesPath: String

    private val log = logger()

    @Scheduled(cron = "\${brn.resources.unverified-pictures.clean-job.cron}")
    fun clearUnusedPictures() {
        val unverifiedFolderPictures: List<String> =
            cloudService
                .getFileNames(unverifiedPicturesPath)
                .filter { it != "/" }
        val defaultFolderPictures: List<String> =
            cloudService
                .getFileNames(defaultPicturesPath)
                .filter { it != "/" }

        val fileNamesToDelete =
            defaultFolderPictures
                .intersect(unverifiedFolderPictures)
                .map { unverifiedPicturesPath.plus(it) }

        cloudService.deleteFiles(fileNamesToDelete)
        log.info("Files ${fileNamesToDelete.size} are deleted from \"$unverifiedPicturesPath\"")
    }
}
