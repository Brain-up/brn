package com.epam.brn.job

import com.epam.brn.csv.CsvUploadService
import com.epam.brn.job.impl.UploadFileJobRunnerImpl
import com.nhaarman.mockito_kotlin.anyOrNull
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
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

@Disabled
@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UploadFileJobRunnerTest {

    private val pathToTaskFiles = "src${File.separator}test${File.separator}resources${File.separator}inputData${File.separator}tasks"

    private val pathToProcessedTaskFiles = "$pathToTaskFiles\\processed"

    @InjectMocks
    lateinit var uploadFileJobRunner: UploadFileJobRunnerImpl

    @Mock
    lateinit var csvUploadService: CsvUploadService

    @Spy
    var sourcesWithJobs: LinkedHashMap<String, CsvUploadService> = LinkedHashMap()

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
            FileUtils.getFile("$pathToTaskFiles\\1_series.csv")
        )
    }

    @Test
    fun `should upload tasks from csv file during job`() {
        // GIVEN
        sourcesWithJobs[pathToTaskFiles] = csvUploadService

        // WHEN
        uploadFileJobRunner.perform()

        // THEN
        verify(csvUploadService, times(1)).loadTasks(anyOrNull<File>())
    }
}
