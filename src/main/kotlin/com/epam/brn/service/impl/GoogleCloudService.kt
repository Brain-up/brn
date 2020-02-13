package com.epam.brn.service.impl

import com.epam.brn.config.GoogleCloudConfig
import com.epam.brn.service.CloudService
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.HttpMethod
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import java.lang.IllegalArgumentException
import java.net.URL
import java.util.concurrent.TimeUnit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@ConditionalOnProperty(name = ["cloud.provider"], havingValue = "google")
@Service
class GoogleCloudService(@Autowired private val cloudConfig: GoogleCloudConfig) : CloudService {

    override fun signatureForClientDirectUpload(fileName: String?): Map<String, String> {
        if (fileName == null) {
            throw IllegalArgumentException("File name should not be empty")
        }
        val storage: Storage =
            StorageOptions.newBuilder().setCredentials(cloudConfig.credentials).setProjectId(cloudConfig.projectId)
                .build().getService()
        val blobInfo: BlobInfo = BlobInfo.newBuilder(BlobId.of(cloudConfig.bucketName, fileName)).build()
        val url: URL = storage.signUrl(
            blobInfo,
            cloudConfig.expireAfter.toMillis(),
            TimeUnit.MILLISECONDS,
            Storage.SignUrlOption.httpMethod(HttpMethod.POST),
            Storage.SignUrlOption.withV4Signature()
        )

        return mapOf("action" to url.toString())
    }

    override fun listBucket(): String {
        return cloudConfig.bucketLink
    }
}
