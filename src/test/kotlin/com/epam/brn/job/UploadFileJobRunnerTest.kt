package com.epam.brn.job

import com.epam.brn.job.csv.task.UploadFromCsvJob
import com.epam.brn.job.impl.UploadFileJobRunnerImpl
import com.nhaarman.mockito_kotlin.anyOrNull
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock

import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.util.ReflectionTestUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UploadFileJobRunnerTest {

    private val pathToTaskFiles = "src\\test\\resources\\inputData\\tasks"

    private val pathToProcessedTaskFiles = "$pathToTaskFiles\\processed"

    @InjectMocks
    lateinit var uploadFileJobRunner: UploadFileJobRunnerImpl

    @Mock
    lateinit var uploadTaskFromCsvJob: UploadFromCsvJob

    @Spy
    var sourcesWithJobs: LinkedHashMap<String, UploadFromCsvJob> = LinkedHashMap()

    @BeforeAll
    fun init() {
        MockitoAnnotations.initMocks(this)

        ReflectionTestUtils.setField(uploadFileJobRunner, "pathToProcessedResources", pathToProcessedTaskFiles)
    }

    @AfterEach
    fun after() {
        val oldFile = Files.walk(Paths.get(pathToProcessedTaskFiles))
            .filter { file -> Files.isRegularFile(file) }
            .collect(Collectors.toList())[0]
            .toFile()

        FileUtils.moveFile(
            oldFile,
            FileUtils.getFile("$pathToTaskFiles\\tasks.csv")
        )
    }

    @Test
    fun `should upload tasks from csv file during job`() {
        // GIVEN
        sourcesWithJobs[pathToTaskFiles] = uploadTaskFromCsvJob

        // WHEN
        uploadFileJobRunner.perform()

        // THEN
        verify(uploadTaskFromCsvJob, times(1)).uploadTasks(anyOrNull<File>())
    }
}