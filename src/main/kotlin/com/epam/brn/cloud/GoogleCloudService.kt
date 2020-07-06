package com.epam.brn.cloud

import com.epam.brn.config.GoogleCloudConfig
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.HttpMethod
import com.google.cloud.storage.Storage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.net.URL
import java.util.TreeSet
import java.util.concurrent.TimeUnit

@ConditionalOnProperty(name = ["cloud.provider"], havingValue = "google")
@Service
class GoogleCloudService(@Autowired private val cloudConfig: GoogleCloudConfig) : CloudService {

    override fun listBucket(): List<String> {
        val blobs = cloudConfig.storage!!.get(cloudConfig.bucketName).list()

        val folders: MutableSet<String> = TreeSet()
        for (blob in blobs.iterateAll()) {
            var fileName = blob.name.replaceAfterLast("/", "")
            while (fileName.contains("/")) {
                folders.add(fileName)
                fileName = fileName.removeSuffix("/").replaceAfterLast("/", "")
            }
        }
        return ArrayList(folders)
    }

    override fun uploadForm(filePath: String): Map<String, String> {
        val blobInfo: BlobInfo = BlobInfo.newBuilder(BlobId.of(cloudConfig.bucketName, filePath)).build()
        val url: URL = cloudConfig.storage!!.signUrl(
            blobInfo,
            cloudConfig.expireAfter.toMillis(),
            TimeUnit.MILLISECONDS,
            Storage.SignUrlOption.httpMethod(HttpMethod.POST),
            Storage.SignUrlOption.withV4Signature()
        )
        return mapOf("action" to url.toString())
    }

    override fun bucketUrl(): String = cloudConfig.bucketLink
}
