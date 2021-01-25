package com.epam.brn.upload.csv.series3

import com.fasterxml.jackson.annotation.JsonProperty

data class SeriesThreeRecord(
    @JsonProperty("level")
    val level: Int,
    @JsonProperty("exerciseName")
    val exerciseName: String,
    @JsonProperty("code")
    val code: String,
    @JsonProperty("words")
    val words: List<String>,
    @JsonProperty("answerAudioFile")
    val answerAudioFile: String,
    @JsonProperty("answerParts")
    val answerParts: String
) {
    companion object {
        const val FORMAT = "level,code,exerciseName,words,answerAudioFile,answerParts"
    }
}
