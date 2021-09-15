package com.epam.brn.dto.request.exercise

import com.epam.brn.enums.Locale
import com.epam.brn.upload.csv.seriesMatrix.SeriesMatrixRecord
import com.epam.brn.upload.csv.seriesPhrases.SeriesPhrasesRecord
import com.epam.brn.upload.csv.seriesWords.SeriesWordsRecord
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "typeOfExercise"
)
@JsonSubTypes(
    JsonSubTypes.Type(ExerciseWordsCreateDto::class, name = "SINGLE_SIMPLE_WORDS"),
    JsonSubTypes.Type(ExercisePhrasesCreateDto::class, name = "PHRASES"),
    JsonSubTypes.Type(ExerciseSentencesCreateDto::class, name = "SENTENCE")
)
sealed class ExerciseCreateDto(
    @ApiModelProperty(value = "type of exercise", required = true, example = "phrases", allowableValues = "SINGLE_SIMPLE_WORDS, PHRASES, SENTENCE")
    @field:NotNull
    open val typeOfExercise: ExerciseType,
    @ApiModelProperty(value = "Locale", required = true, example = "RU")
    @field:NotNull
    open val locale: Locale,
    @ApiModelProperty(value = "Subgroup code", required = true, example = "longShortPhrases")
    @field:NotBlank
    open val subGroup: String,
    @ApiModelProperty(value = "Level", required = true, example = "1")
    @field:NotNull
    open val level: Int?,
    @ApiModelProperty(value = "Exercise name", required = true, example = "Фразы разной длительности")
    @field:NotBlank
    open val exerciseName: String
)

@ApiModel(value = "ExerciseWordsCreateDto", description = "Request dto for create exercise 'words'")
data class ExerciseWordsCreateDto(
    @ApiModelProperty(value = "Locale", required = true, example = "RU")
    @field:NotNull
    override val locale: Locale,
    @ApiModelProperty(value = "Subgroup code", required = true, example = "family")
    @field:NotBlank
    override val subGroup: String,
    @ApiModelProperty(value = "Level", required = true, example = "1")
    @field:NotNull
    override val level: Int,
    @ApiModelProperty(value = "Exercise name", required = true, example = "Семья /+голоса/")
    @field:NotBlank
    override val exerciseName: String,
    @ApiModelProperty(value = "Words for creating exercise", required = true, example = "[сын, ребёнок, мама]")
    @field:NotEmpty
    val words: List<String>,
    @ApiModelProperty(value = "Noise level", required = true, example = "50")
    @field:NotNull
    val noiseLevel: Int,
    @ApiModelProperty(value = "Noise url", required = false, example = "voices")
    val noiseUrl: String? = null
) : ExerciseCreateDto(ExerciseType.SINGLE_SIMPLE_WORDS, locale, subGroup, level, exerciseName) {
    fun toSeriesWordsRecord() = SeriesWordsRecord(
        level = level,
        code = subGroup,
        exerciseName = exerciseName,
        words = words,
        noiseLevel = noiseLevel,
        noiseUrl = noiseUrl.orEmpty()
    )
}

@ApiModel(value = "ExercisePhrasesCreateDto", description = "Request dto for create exercise 'phrases'")
data class ExercisePhrasesCreateDto(
    @ApiModelProperty(value = "Locale", required = true, example = "RU")
    @field:NotNull
    override val locale: Locale,
    @ApiModelProperty(value = "Subgroup code", required = true, example = "longShortPhrases")
    @field:NotBlank
    override val subGroup: String,
    @ApiModelProperty(value = "Level", required = true, example = "1")
    @field:NotNull
    override val level: Int,
    @ApiModelProperty(value = "Exercise name", required = true, example = "Фразы разной длительности")
    @field:NotBlank
    override val exerciseName: String,
    @ApiModelProperty(value = "Phrases for creating exercise. Phrases should not contain punctuation marks.", required = true)
    @field:Valid
    val phrases: Phrases,
    @ApiModelProperty(value = "noise level", required = true, example = "50")
    @field:NotNull
    val noiseLevel: Int,
    @ApiModelProperty(value = "noise url", required = false, example = "voices")
    val noiseUrl: String? = null
) : ExerciseCreateDto(ExerciseType.PHRASES, locale, subGroup, level, exerciseName) {
    fun toSeriesPhrasesRecord() = SeriesPhrasesRecord(
        level = level,
        code = subGroup,
        exerciseName = exerciseName,
        phrases = phrases.toListWithDot(),
        noiseLevel = noiseLevel,
        noiseUrl = noiseUrl.orEmpty()
    )
}

@ApiModel(value = "ExerciseSentencesCreateDto", description = "Request dto for create exercise 'sentences'")
data class ExerciseSentencesCreateDto(
    @ApiModelProperty(value = "Locale", required = true, example = "RU")
    @field:NotNull
    override val locale: Locale,
    @ApiModelProperty(value = "Subgroup code", required = true, example = "sentence_with_6_words")
    @field:NotBlank
    override val subGroup: String,
    @field:NotNull
    @ApiModelProperty(value = "Level", required = true, example = "1")
    override val level: Int,
    @field:NotBlank
    @ApiModelProperty(value = "Exercise name", required = true, example = "Пойми предложение из 6 слов из 18")
    override val exerciseName: String,
    @field:NotNull
    @ApiModelProperty(value = "Order number", required = true, example = "1")
    val orderNumber: Int,
    @ApiModelProperty(value = "Sets of words for creating sentences", required = true)
    @field:NotNull
    val words: SetOfWords
) : ExerciseCreateDto(ExerciseType.SENTENCE, locale, subGroup, level, exerciseName) {
    fun toSeriesMatrixRecord() = SeriesMatrixRecord(
        level = level,
        code = subGroup,
        exerciseName = exerciseName,
        orderNumber = orderNumber,
        words = words.toRecordList()
    )
}
