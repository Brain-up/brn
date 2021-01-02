package com.epam.brn.upload.csv.nonspeech

import com.epam.brn.model.ExerciseType
import com.fasterxml.jackson.annotation.JsonProperty

data class SignalSeriesRecord(
    @JsonProperty("level")
    val level: Int,
    @JsonProperty("code")
    val code: String,
    @JsonProperty("exerciseName")
    val exerciseName: String,
    @JsonProperty("exerciseType")
    val exerciseType: ExerciseType,
    @JsonProperty("signals")
    val signals: List<String>
) {
    companion object {
        const val FORMAT = "level,code,exerciseName,exerciseType,signals"
    }
}
