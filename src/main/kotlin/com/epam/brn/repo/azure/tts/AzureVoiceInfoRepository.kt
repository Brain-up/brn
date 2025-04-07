package com.epam.brn.repo.azure.tts

import com.epam.brn.model.azure.tts.AzureVoiceInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AzureVoiceInfoRepository : JpaRepository<AzureVoiceInfo, Long> {
    fun findByShortName(shortName: String): AzureVoiceInfo?
    fun findByLocaleIgnoreCaseAndGenderIgnoreCase(
        locale: String,
        gender: String,
    ): MutableList<AzureVoiceInfo>
}
