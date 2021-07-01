package com.epam.brn.service.impl

import com.epam.brn.enums.HeadphonesType
import com.epam.brn.model.Headphones
import com.epam.brn.repo.HeadphonesRepository
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.natpryce.hamkrest.sameInstance
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class HeadphonesServiceImplTest {

    @InjectMockKs
    lateinit var headphonesServiceImpl: HeadphonesServiceImpl

    @MockK
    lateinit var headphonesRepository: HeadphonesRepository

    private val headphonesName = "test"
    private val headphonesEntity = Headphones(name = headphonesName, type = HeadphonesType.IN_EAR_BLUETOOTH)
    private val headphonesEntitySecond = Headphones(name = headphonesName, type = HeadphonesType.ON_EAR_NO_BLUETOOTH)

    @Test
    fun `should save to the repository and return dto`() {
        // GIVEN
        every { headphonesRepository.save(headphonesEntity) } returns headphonesEntity

        // WHEN
        val headphonesDto = headphonesServiceImpl.save(headphonesEntity)

        // THEN
        assertThat(headphonesDto.name, sameInstance(headphonesName))
    }

    @Test
    fun `should get all headphones for user`() {
        // GIVEN
        every { headphonesRepository.getHeadphonesForUser(1L) } returns listOf(headphonesEntity, headphonesEntitySecond)

        // WHEN
        val allHeadphonesForUser = headphonesServiceImpl.getAllHeadphonesForUser(1L)

        // THEN
        assertThat(allHeadphonesForUser, hasSize(equalTo(2)))
    }
}
