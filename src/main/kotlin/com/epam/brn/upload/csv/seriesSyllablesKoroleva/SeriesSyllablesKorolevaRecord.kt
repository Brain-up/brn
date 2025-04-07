package com.epam.brn.upload.csv.seriesSyllablesKoroleva

import com.fasterxml.jackson.annotation.JsonProperty

data class SeriesSyllablesKorolevaRecord(
    @JsonProperty("level")
    val level: Int,
    @JsonProperty("code")
    val code: String,
    @JsonProperty("exerciseName")
    val exerciseName: String,
    @JsonProperty("words")
    val words: List<String>,
    @JsonProperty("wordsColumns")
    val wordsColumns: Int,
) {
    companion object {
        const val FORMAT = "level,code,exerciseName,words,wordsColumns"
    }
}
