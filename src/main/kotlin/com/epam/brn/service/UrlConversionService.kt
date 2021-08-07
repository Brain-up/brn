package com.epam.brn.service

import com.epam.brn.config.AwsConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class UrlConversionService(private val awsConfig: AwsConfig) {
    @Value(value = "\${aws.folderForThemePictures}")
    private lateinit var folderForThemePictures: String

    fun makeUrlForNoise(noiseUrl: String?): String {
        if (noiseUrl.isNullOrEmpty())
            return ""
        return awsConfig.baseFileUrl + noiseUrl
    }

    fun makeUrlForSubGroupPicture(subGroupCode: String): String =
        awsConfig.baseFileUrl + folderForThemePictures + "/" + subGroupCode + ".svg"
}
