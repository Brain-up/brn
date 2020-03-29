package com.epam.brn.upload.csv.record

import com.fasterxml.jackson.annotation.JsonProperty

data class SeriesThreeRecord(
    @JsonProperty("level")
    val level: Int,
    @JsonProperty("exerciseName")
    val exerciseName: String,
    @JsonProperty("orderNumber")
    val orderNumber: Int,
    @JsonProperty("words")
    val words: List<String>,
    @JsonProperty("answerAudioFile")
    val answerAudioFile: String,
    @JsonProperty("answerParts")
    val answerParts: String
) : CsvRecord() {
    companion object {
        const val HEADER = "level,exerciseName,orderNumber,words,answerAudioFile,answerParts"
    }
}
