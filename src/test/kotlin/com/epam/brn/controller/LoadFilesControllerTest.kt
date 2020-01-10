package com.epam.brn.controller

import com.epam.brn.job.csv.task.UploadFromCsvJob
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import java.io.File
import java.io.FileInputStream
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mock.web.MockMultipartFile

@ExtendWith(MockitoExtension::class)
internal class LoadFilesControllerTest {

    @InjectMocks
    lateinit var loadFilesController: LoadFilesController

    @Mock
    lateinit var uploadFromCsvJob: UploadFromCsvJob

    @Test
    fun `should upload tasks from task csv file`() {
        // GIVEN
        val taskFile = MockMultipartFile(
            "tasks_for_single_words_series.csv",
            FileInputStream("src${File.separator}test${File.separator}resources${File.separator}inputData${File.separator}tasks${File.separator}tasks_for_single_words_series.csv")
        )
        // WHEN
        loadFilesController.addTasks(taskFile)
        // THEN
        verify(uploadFromCsvJob, times(1)).uploadTasks(taskFile)
    }
}
