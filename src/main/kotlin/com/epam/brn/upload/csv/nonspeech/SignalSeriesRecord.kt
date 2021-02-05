package com.epam.brn.upload.csv.nonspeech

import com.epam.brn.enums.ExerciseType
import com.fasterxml.jackson.annotation.JsonProperty

data class SignalSeriesRecord(
    @JsonProperty("series")
    val series: String,
    @JsonProperty("level")
    val level: Int,
    @JsonProperty("exerciseName")
    val exerciseName: String,
    @JsonProperty("exerciseType")
    val exerciseType: ExerciseType,
    @JsonProperty("signals")
    val signals: List<String>
) {
    companion object {
        const val FORMAT = "series,level,exerciseName,exerciseType,signals"
    }
}
