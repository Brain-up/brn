package com.epam.brn.upload.csv.seriesWordsKoroleva

import com.fasterxml.jackson.annotation.JsonProperty

data class SeriesWordsKorolevaRecord(
    @JsonProperty("level")
    val level: Int,
    @JsonProperty("code")
    val code: String,
    @JsonProperty("exerciseName")
    val exerciseName: String,
    @JsonProperty("words")
    val words: List<String>,
    @JsonProperty("playWordsCount")
    val playWordsCount: Int,
    @JsonProperty("wordsColumns")
    val wordsColumns: Int,
) {
    companion object {
        const val FORMAT = "level,code,exerciseName,words,playWordsCount, wordsColumns"
    }
}
