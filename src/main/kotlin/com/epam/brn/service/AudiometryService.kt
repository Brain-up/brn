package com.epam.brn.service

import com.epam.brn.dto.AudiometryDto
import com.epam.brn.enums.Locale
import com.epam.brn.repo.AudiometryRepository
import org.springframework.stereotype.Service

@Service
class AudiometryService(private val audiometryRepository: AudiometryRepository) {
    fun getAudiometrics(locale: String = Locale.RU.locale): List<AudiometryDto> {
        val audiometrics = audiometryRepository.findByLocale(locale).toMutableList()
        audiometrics.addAll(audiometryRepository.findByLocaleIsNull())
        return audiometrics.map { a -> a.toDto() }
    }
}
