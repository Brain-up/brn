package com.epam.brn.dto

import com.epam.brn.dto.azure.tts.AzureRates

data class AudioFileMetaData(
    var text: String,
    val locale: String,
    var voice: String,
    var speedFloat: String = "1",
    var speedCode: AzureRates = AzureRates.DEFAULT,
    val gender: String? = null,
    val pitch: String? = null,
    val style: String? = null,
    val styleDegree: String = "1",
) {
    fun setSpeedSlow() {
        this.speedCode = AzureRates.SLOW
        this.speedFloat = "0.8"
    }

    fun setSpeedSlowest() {
        this.speedCode = AzureRates.X_SLOW
        this.speedFloat = "0.65"
    }

    fun setSpeedNormal() {
        this.speedCode = AzureRates.DEFAULT
        this.speedFloat = "1"
    }

    fun setSpeedFaster() {
        this.speedCode = AzureRates.FAST
        this.speedFloat = "1.2"
    }
}
