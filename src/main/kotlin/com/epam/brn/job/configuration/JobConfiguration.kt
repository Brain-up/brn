package com.epam.brn.job.configuration

import com.epam.brn.upload.CsvUploadService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JobConfiguration(private val csvUploadService: CsvUploadService) {

    companion object {
        const val PATH_TO_PRECESSED_RESOURCES = "\${brn.processed.files.path}"
        const val PATH_TO_TASK_CSV_RESOURCES = "\${brn.task.files.path}"
    }

    @Bean
    fun sourcesWithJobs(): LinkedHashMap<String, CsvUploadService> {
        return linkedMapOf(
            PATH_TO_TASK_CSV_RESOURCES to csvUploadService
        )
    }
}
