package com.epam.brn.job.configuration

import com.epam.brn.constant.BrnJob.PATH_TO_TASK_CSV_RESOURCES
import com.epam.brn.job.csv.task.UploadFromCsvJob
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JobConfiguration(private val uploadTaskFromCsvJob: UploadFromCsvJob) {

    @Bean
    fun sourcesWithJobs(): LinkedHashMap<String, UploadFromCsvJob> {
        return linkedMapOf(
            PATH_TO_TASK_CSV_RESOURCES to uploadTaskFromCsvJob
        )
    }
}
