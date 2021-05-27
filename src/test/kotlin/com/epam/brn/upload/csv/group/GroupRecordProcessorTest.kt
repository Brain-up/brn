package com.epam.brn.upload.csv.group

import com.epam.brn.model.ExerciseGroup
import com.epam.brn.repo.ExerciseGroupRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class GroupRecordProcessorTest {

    @InjectMockKs
    private lateinit var groupRecordProcessor: GroupRecordProcessor

    @MockK
    private lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @Test
    fun `should create correct group`() {
        // GIVEN
        val records = createInputGroupRecordList()
        val expected = createActualGroupList()
        val exerciseGroupMock = Mockito.mock(ExerciseGroup::class.java)
        every { exerciseGroupRepository.findByCode(ofType(String::class)) } returns Optional.empty()
        every { exerciseGroupRepository.save(ofType(ExerciseGroup::class)) } returns exerciseGroupMock
        // WHEN
        val actual = groupRecordProcessor.process(records)
        // THEN
        for (i in 0 until expected.size) {
            assertThat(actual[i]).isEqualTo(expected[i])
        }
        verify(exactly = records.size) { exerciseGroupRepository.findByCode(ofType(String::class)) }
        verify(exactly = records.size) { exerciseGroupRepository.save(ofType(ExerciseGroup::class)) }
    }

    @Test
    fun `should not call save group when all records exists in DB`() {
        // GIVEN
        val records = createInputGroupRecordList()
        val expected = createActualGroupList()
        val exerciseGroupMock = Mockito.mock(ExerciseGroup::class.java)
        every { exerciseGroupRepository.findByCode(ofType(String::class)) } returnsMany expected.map { Optional.of(it) }
        every { exerciseGroupRepository.save(ofType(ExerciseGroup::class)) } returns exerciseGroupMock
        // WHEN
        val actual = groupRecordProcessor.process(records)
        // THEN
        for (i in 0 until expected.size) {
            assertThat(actual[i]).isEqualTo(expected[i])
        }
        verify(exactly = records.size) { exerciseGroupRepository.findByCode(ofType(String::class)) }
        verify(inverse = true) { exerciseGroupRepository.save(ofType(ExerciseGroup::class)) }
    }

    private fun createActualGroupList() = mutableListOf(
        ExerciseGroup(
            code = "CODE",
            name = "name",
            locale = "ru-ru",
            description = "desc"
        ),
        ExerciseGroup(
            code = "CODE1",
            name = "name1",
            locale = "ru-ru1",
            description = "desc1"
        ),
        ExerciseGroup(
            code = "CODE2",
            name = "name2",
            locale = "ru-ru2",
            description = "desc2"
        )
    )

    private fun createInputGroupRecordList() = mutableListOf(
        GroupRecord(
            code = "CODE",
            locale = "ru-ru",
            name = "name",
            description = "desc"
        ),
        GroupRecord(
            code = "CODE1",
            locale = "ru-ru1",
            name = "name1",
            description = "desc1"
        ),
        GroupRecord(
            code = "CODE2",
            locale = "ru-ru2",
            name = "name2",
            description = "desc2"
        )
    )
}
