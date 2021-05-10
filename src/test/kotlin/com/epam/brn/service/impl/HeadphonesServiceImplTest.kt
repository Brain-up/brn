package com.epam.brn.service.impl

import com.epam.brn.enums.HeadphonesType
import com.epam.brn.model.Headphones
import com.epam.brn.repo.HeadphonesRepository
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.natpryce.hamkrest.sameInstance
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
@DisplayName("Headphone service test using mockito")
internal class HeadphonesServiceImplTest {
    @InjectMocks
    lateinit var headphonesServiceImpl: HeadphonesServiceImpl

    @Mock
    lateinit var headphonesRepository: HeadphonesRepository
    private val headphonesName = "test"
    private val headphonesEntity = Headphones(name = headphonesName, type = HeadphonesType.IN_EAR_BLUETOOTH)
    private val headphonesEntitySecond = Headphones(name = headphonesName, type = HeadphonesType.ON_EAR_NO_BLUETOOTH)

    @Test
    fun `should save to the repository and return dto`() {
        `when`(headphonesRepository.save(headphonesEntity)).thenReturn(headphonesEntity)
        val headphonesDto = headphonesServiceImpl.save(headphonesEntity)
        assertThat(headphonesDto.name, sameInstance(headphonesName))
    }

    @Test
    fun getAllHeadphonesForUser() {
        `when`(headphonesRepository.getHeadphonesForUser(1L)).thenReturn(
            listOf(
                headphonesEntity,
                headphonesEntitySecond
            )
        )

        val allHeadphonesForUser = headphonesServiceImpl.getAllHeadphonesForUser(1L)
        assertThat(allHeadphonesForUser, hasSize(equalTo(2)))
    }
}
