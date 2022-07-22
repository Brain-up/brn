package com.epam.brn.service

import com.epam.brn.service.cloud.CloudService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class UrlConversionService(private val cloudService: CloudService) {
    @Value(value = "\${aws.folderForThemePictures}")
    private lateinit var folderForThemePictures: String

    @Value("\${brn.resources.default-pictures.path}")
    lateinit var defaultPicturesPath: String

    @Value("\${brn.resources.unverified-pictures.path:}")
    lateinit var unverifiedPicturesPath: String

    fun makeUrlForNoise(noiseUrl: String?): String {
        if (noiseUrl.isNullOrEmpty())
            return ""
        return cloudService.baseFileUrl() + noiseUrl
    }

    fun makeUrlForSubGroupPicture(subGroupCode: String): String =
        cloudService.baseFileUrl() + folderForThemePictures + "/" + subGroupCode + ".svg"

    fun makeUrlForTaskPicture(pictureTaskUrl: String?): String {
        val fileName = getFileNameFromPictureUrl(pictureTaskUrl)
        return if (cloudService.isFileExist(defaultPicturesPath, fileName))
            createFullPictureTaskUrl(defaultPicturesPath, fileName)
        else if (cloudService.isFileExist(unverifiedPicturesPath, fileName))
            createFullPictureTaskUrl(unverifiedPicturesPath, fileName)
        else
            ""
    }

    private fun getFileNameFromPictureUrl(pictureTaskUrl: String?): String =
        pictureTaskUrl?.substring(pictureTaskUrl.lastIndexOf("/") + 1).orEmpty()

    private fun createFullPictureTaskUrl(filePath: String, fileName: String): String =
        cloudService.baseFileUrl() + "/" + filePath + "/" + fileName.replace("jpg", "png").lowercase()
}
