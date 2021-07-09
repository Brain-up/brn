package com.epam.brn.upload.csv.seriesWords

import com.epam.brn.enums.FrequencyZone
import com.epam.brn.enums.Locale
import com.fasterxml.jackson.annotation.JsonProperty

data class LopotkoRecord(
    @JsonProperty("locale")
    val locale: Locale,
    @JsonProperty("type")
    val type: String,
    @JsonProperty("order")
    val order: Int,
    @JsonProperty("group")
    val group: String,
    @JsonProperty("frequencyZone")
    val frequencyZone: FrequencyZone,
    @JsonProperty("minFrequency")
    val minFrequency: Int,
    @JsonProperty("maxFrequency")
    val maxFrequency: Int,
    @JsonProperty("words")
    val words: List<String>,
) {
    companion object {
        const val FORMAT = "locale,type,order,group,frequencyZone,minFrequency,maxFrequency,words"
    }
}
