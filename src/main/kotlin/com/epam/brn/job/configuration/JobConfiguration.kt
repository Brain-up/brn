package com.epam.brn.job.configuration

import com.epam.brn.constant.BrnJob.PATH_TO_TASK_CSV_RESOURCES
import com.epam.brn.csv.CsvUploadService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JobConfiguration(private val csvUploadService: CsvUploadService) {

    @Bean
    fun sourcesWithJobs(): LinkedHashMap<String, CsvUploadService> {
        return linkedMapOf(
            PATH_TO_TASK_CSV_RESOURCES to csvUploadService
        )
    }
}
