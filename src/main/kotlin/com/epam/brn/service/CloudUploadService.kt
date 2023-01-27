package com.epam.brn.service

import com.epam.brn.service.cloud.CloudService
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.util.unit.DataSize
import org.springframework.web.multipart.MultipartFile

@Service
class CloudUploadService(
    @Autowired private val cloudService: CloudService,
    @Autowired private val resourceService: ResourceService
) {

    @Value("\${brn.resources.default-pictures.path}")
    lateinit var defaultPicturesPath: String

    @Value("\${brn.resources.unverified-pictures.path:}")
    lateinit var unverifiedPicturesPath: String

    @Value("\${brn.resources.contributor-pictures.path}")
    lateinit var contributorPicturesPath: String

    @Value("\${brn.resources.unverified-pictures.ext:}")
    lateinit var unverifiedPictureExtensions: Set<String>

    @Value("\${brn.resources.pictures.ext:}")
    lateinit var contributorPictureExtensions: Set<String>

    @Value("\${brn.resources.unverified-pictures.max-size:}")
    lateinit var unverifiedPictureMaxSize: DataSize

    @Value("\${brn.resources.contributor-pictures.max-size:}")
    lateinit var contributorPictureMaxSize: DataSize

    private val log = logger()

    fun uploadUnverifiedPictureFile(multipartFile: MultipartFile) {
        val fileName = FilenameUtils.getBaseName(multipartFile.originalFilename)
        val fileExtension = FilenameUtils.getExtension(multipartFile.originalFilename)
        val fullFileName = "$fileName.$fileExtension"
        val fileSize = multipartFile.size
        log.debug("File info: \"$fullFileName\". Size: $fileSize")

        verifyFileExtension(multipartFile, unverifiedPictureExtensions)
        verifyPictureSize(multipartFile, unverifiedPictureMaxSize)

        resourceService.findFirstResourceByWord(fileName)
            ?: throw IllegalArgumentException("The world \"$fileName\" is not found in database")
        if (cloudService.isFileExist(defaultPicturesPath, fileName)) {
            throw IllegalArgumentException("File \"$fullFileName\" is exist in cloud default path")
        } else if (cloudService.isFileExist(unverifiedPicturesPath, fileName)) {
            throw IllegalArgumentException("File \"$fullFileName\" is exist in cloud path \"$unverifiedPicturesPath\"")
        }

        cloudService.uploadFile(unverifiedPicturesPath, fullFileName, multipartFile.inputStream)
    }

    fun uploadContributorPicture(multipartFile: MultipartFile, fileName: String): String {
        val fileExtension = FilenameUtils.getExtension(multipartFile.originalFilename)
        val fullFileName = "$fileName.$fileExtension"
        val fileSize = multipartFile.size
        log.debug("File info: \"$fullFileName\". Size: $fileSize")

        verifyFileExtension(multipartFile, contributorPictureExtensions)
        verifyPictureSize(multipartFile, contributorPictureMaxSize)

        cloudService.uploadFile(contributorPicturesPath, fullFileName, multipartFile.inputStream)
        return "${cloudService.baseFileUrl()}/$contributorPicturesPath/$fullFileName"
    }

    private fun verifyFileExtension(file: MultipartFile, extensions: Set<String>) {
        if (!extensions.contains(FilenameUtils.getExtension(file.originalFilename))) {
            throw IllegalArgumentException("File extension should be one of $extensions")
        }
    }

    private fun verifyPictureSize(file: MultipartFile, maxSize: DataSize) {
        val fileSize = file.size
        if (fileSize > maxSize.toBytes()) {
            val humanReadablePictureMaxSize = FileUtils.byteCountToDisplaySize(maxSize.toBytes())
            throw IllegalArgumentException(
                "File size [$fileSize] should be less than max file size [$humanReadablePictureMaxSize]"
            )
        }
    }
}
