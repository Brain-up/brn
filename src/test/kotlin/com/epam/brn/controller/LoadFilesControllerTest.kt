package com.epam.brn.controller

import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.ResourceService
import com.epam.brn.service.StudyHistoryService
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.SubGroupService
import com.epam.brn.service.statistic.UserPeriodStatisticService
import com.epam.brn.upload.CsvUploadService
import io.kotest.matchers.shouldBe
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import java.io.File
import java.io.FileInputStream

@ExtendWith(MockKExtension::class)
internal class LoadFilesControllerTest {

    @InjectMockKs
    lateinit var adminController: AdminController

    @RelaxedMockK
    lateinit var csvUploadService: CsvUploadService

    @MockK
    lateinit var studyHistoryService: StudyHistoryService

    @MockK
    lateinit var userAccountService: UserAccountService

    @MockK
    lateinit var exerciseService: ExerciseService

    @MockK
    lateinit var resourceService: ResourceService

    @MockK
    lateinit var subGroupService: SubGroupService

    @MockK
    lateinit var userDayStatisticService: UserPeriodStatisticService<DayStudyStatistic>

    @MockK
    lateinit var userMonthStatisticService: UserPeriodStatisticService<MonthStudyStatistic>

    @Test
    fun `should call upload service to load file for 1 series`() {
        // GIVEN
        val taskFile = MockMultipartFile(
            "series_words_en.csv",
            FileInputStream("src${File.separator}test${File.separator}resources${File.separator}inputData${File.separator}tasks${File.separator}series_words_en.csv")
        )

        // WHEN
        val result = adminController.loadExercises(1, taskFile)

        // THEN
        verify(exactly = 1) { csvUploadService.loadExercises(1, taskFile) }
        result.statusCode shouldBe HttpStatus.CREATED
    }
}
