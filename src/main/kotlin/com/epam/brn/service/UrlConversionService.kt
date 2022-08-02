package com.epam.brn.service

import com.epam.brn.service.cloud.CloudService
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

    fun makeUrlForNoise(noiseUrl: String?): String {
        if (noiseUrl.isNullOrEmpty())
            return ""
        return cloudService.baseFileUrl() + noiseUrl
    }

    fun makeUrlForSubGroupPicture(subGroupCode: String): String =
        cloudService.baseFileUrl() + folderForThemePictures + "/" + subGroupCode + ".svg"

    fun makeUrlForTaskPicture(word: String): String {
        listOf(defaultPicturesPath, unverifiedPicturesPath).forEach { picturesPath ->
            pictureExtensions.forEach { ext ->
                val fileName = "$word.$ext"
                if (cloudService.isFileExist(picturesPath, fileName))
                    return cloudService.createFullFileName(picturesPath, fileName)
            }
        }
        return ""
    }
}
