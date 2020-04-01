package com.epam.brn.integration.configuration

import com.epam.brn.job.UploadFileJobRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("integration-tests")
class UploadFileJobRunnerStub : UploadFileJobRunner {
    override fun perform() {
        // Do nothing
    }
}
