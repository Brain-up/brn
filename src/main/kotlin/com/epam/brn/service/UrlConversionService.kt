package com.epam.brn.service

import com.epam.brn.service.cloud.CloudService
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class UrlConversionService(private val cloudService: CloudService) {
    @Value("\${aws.folderForThemePictures}")
    private lateinit var folderForThemePictures: String

    @Value("\${brn.resources.default-pictures.path}")
    lateinit var defaultPicturesPath: String

    @Value("\${brn.resources.unverified-pictures.path:}")
    lateinit var unverifiedPicturesPath: String

    @Value("\${brn.resources.pictures.ext}")
    lateinit var pictureExtensions: Set<String>

    private val log = logger()

    fun makeUrlForNoise(noiseUrl: String?): String {
        if (noiseUrl.isNullOrEmpty())
            return ""
        return cloudService.baseFileUrl() + noiseUrl
    }

    fun makeUrlForSubGroupPicture(subGroupCode: String): String =
        cloudService.baseFileUrl() + folderForThemePictures + "/" + subGroupCode + ".svg"

    fun makeUrlsForTaskPictures(words: List<String?>): Map<String, String> {
        val result = mutableMapOf<String, String>()
        listOf(defaultPicturesPath) // ,unverifiedPicturesPath - when on UI part will be implement saving pictures
            .forEach { picturesPath ->
                result.putAll(cloudService.findExistingFiles(picturesPath, words, pictureExtensions))
            }
        return result
    }
}
