package com.epam.brn.controller

import com.epam.brn.upload.CsvUploadService
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
    lateinit var csvUploadService: CsvUploadService

    @Test
    fun `should call upload service to load file for 1 series`() {
        // GIVEN
        val taskFile = MockMultipartFile(
            "1_series.csv",
            FileInputStream("src${File.separator}test${File.separator}resources${File.separator}inputData${File.separator}tasks${File.separator}1_series.csv")
        )
        // WHEN
        loadFilesController.loadExercises(1, taskFile)
        // THEN
        verify(csvUploadService, times(1)).loadExercises(1, taskFile)
    }
}
