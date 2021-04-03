package com.epam.brn.service

import com.epam.brn.config.AwsConfig
import org.springframework.stereotype.Service

@Service
class UrlConversionService(private val awsConfig: AwsConfig) {
    fun makeUrlForNoise(noiseUrl: String?): String {
        if (noiseUrl.isNullOrEmpty())
            return ""
        return awsConfig.baseFileUrl + noiseUrl
    }
}
