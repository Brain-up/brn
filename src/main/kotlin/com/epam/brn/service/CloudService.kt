package com.epam.brn.service

interface CloudService {
    fun signatureForClientDirectUpload(fileName: String?): Map<String, Any>
    fun listBucket(): String
}
