package com.epam.brn.service

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.dto.response.ExerciseWithTasksResponse
import com.epam.brn.dto.request.exercise.ExerciseCreateDto
import com.epam.brn.dto.request.exercise.ExercisePhrasesCreateDto
import com.epam.brn.dto.request.exercise.ExerciseSentencesCreateDto
import com.epam.brn.dto.request.exercise.ExerciseWordsCreateDto
import com.epam.brn.enums.Locale
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.upload.csv.RecordProcessor
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExerciseService(
    private val exerciseRepository: ExerciseRepository,
    private val studyHistoryRepository: StudyHistoryRepository,
    private val userAccountService: UserAccountService,
    private val urlConversionService: UrlConversionService,
    private val recordProcessors: List<RecordProcessor<out Any, out Any>>,
    private val audioFilesGenerationService: AudioFilesGenerationService,
    private val wordsService: WordsService
) {

    @Value(value = "\${minRepetitionIndex}")
    private lateinit var minRepetitionIndex: Number

    @Value(value = "\${minRightAnswersIndex}")
    private lateinit var minRightAnswersIndex: Number

    @Value("#{'\${yandex.speeds}'.split(',')}")
    lateinit var speeds: List<String>

    @Value(value = "\${brn.audio.file.getFromStorage}")
    private var getAudioFileFromStorage: Boolean = false

    private val log = logger()

    fun findExerciseById(exerciseID: Long): ExerciseDto {
        val exercise = exerciseRepository.findById(exerciseID)
        return exercise.map { e -> updateExerciseDto(e.toDto()) }
            .orElseThrow { EntityNotFoundException("Could not find requested exerciseID=$exerciseID") }
    }

    fun findExerciseByNameAndLevel(name: String, level: Int): Exercise {
        return exerciseRepository.findExerciseByNameAndLevel(name, level)
            .orElseThrow { EntityNotFoundException("Exercise was not found by name=$name and level=$level") }
    }

    fun findExercisesByUserId(userId: Long): List<ExerciseDto> {
        log.info("Searching available exercises for user=$userId")
        val exercisesIdList = studyHistoryRepository.getDoneExercisesIdList(userId)
        val history = exerciseRepository.findAll()
        return history.map { x -> updateExerciseDto(x.toDto(exercisesIdList.contains(x.id))) }
    }

    fun findExercisesBySubGroupForCurrentUser(subGroupId: Long): List<ExerciseDto> {
        val currentUser = userAccountService.getUserFromTheCurrentSession()
        return findExercisesByUserIdAndSubGroupId(currentUser.id!!, subGroupId)
    }

    fun findExercisesByUserIdAndSubGroupId(userId: Long, subGroupId: Long): List<ExerciseDto> {
        log.info("Searching exercises for user=$userId with subGroupId=$subGroupId with Availability")
        val subGroupExercises = exerciseRepository.findExercisesBySubGroupId(subGroupId).sortedBy { s -> s.level }
        log.info("current user is admin: ${userId == 1L}))")
        if (userId == 1L)
            return subGroupExercises.map { exercise -> updateExerciseDto(exercise.toDto(true)) }
        val doneSubGroupExercises = studyHistoryRepository.getDoneExercises(subGroupId, userId)
        val openSubGroupExercises = getAvailableExercisesForSubGroup(doneSubGroupExercises, subGroupExercises, userId, subGroupId)
        return subGroupExercises.map { exercise ->
            updateExerciseDto(exercise.toDto(openSubGroupExercises.contains(exercise)))
        }
    }

    fun getAvailableExerciseIds(exerciseIds: List<Long>): List<Long> {
        if (exerciseIds.isEmpty()) return emptyList()
        val exercise = exerciseRepository.findById(exerciseIds[0])
        if (!exercise.isPresent) throw EntityNotFoundException("There is no one exercise with id = ${exerciseIds[0]}")
        val currentUser = userAccountService.getUserFromTheCurrentSession()
        return findExercisesByUserIdAndSubGroupId(currentUser.id!!, exercise.get().subGroup!!.id!!)
            .filter(ExerciseDto::available)
            .map { e -> e.id!! }
    }

    fun getAvailableExercisesForSubGroup(
        doneSubGroupExercises: List<Exercise>,
        subGroupExercises: List<Exercise>,
        userId: Long,
        subGroupId: Long
    ): Set<Exercise> {
        if (doneSubGroupExercises.size == subGroupExercises.size)
            return doneSubGroupExercises.toSet()
        val mapDone = doneSubGroupExercises.groupBy({ it.name }, { it })
        val available = mutableSetOf<Exercise>()
        val lastHistoryMap = studyHistoryRepository.findLastBySubGroupAndUserAccount(subGroupId, userId)
            .groupBy({ it.exercise }, { it })
        subGroupExercises
            .groupBy({ it.name }, { it })
            .forEach { (name, currentNameExercises) ->
                run {
                    available.add(currentNameExercises[0])
                    val currentDone = mapDone[name]
                    if (currentDone.isNullOrEmpty()) {
                        available.add(currentNameExercises[0])
                        return@forEach
                    }
                    val lastDone = currentDone.last()
                    val lastHistory = lastHistoryMap[lastDone]
                    if (lastHistory.isNullOrEmpty()) {
                        available.addAll(currentDone)
                        return@forEach
                    }
                    val repetitionIndex =
                        lastHistory[0].tasksCount.toFloat() / (lastHistory[0].replaysCount + lastHistory[0].tasksCount)
                    val rightAnswersIndex = 1F - lastHistory[0].wrongAnswers.toFloat() / lastHistory[0].tasksCount
                    if (repetitionIndex < minRepetitionIndex.toFloat() || rightAnswersIndex < minRightAnswersIndex.toFloat()) {
                        available.addAll(currentDone)
                        return@forEach
                    }
                    available.addAll(currentDone)
                    val closed = currentNameExercises.minus(doneSubGroupExercises)
                    if (closed.isNotEmpty())
                        available.add(closed.first())
                }
            }
        return available
    }

    fun updateExerciseDto(exerciseDto: ExerciseDto): ExerciseDto {
        exerciseDto.noise.url = urlConversionService.makeUrlForNoise(exerciseDto.noise.url)
        exerciseDto.isAudioFileUrlGenerated = getAudioFileFromStorage
        return exerciseDto
    }

    fun updateActiveStatus(exerciseId: Long, active: Boolean) {
        var exercise = exerciseRepository.findById(exerciseId).get()
        exercise.active = active
        exerciseRepository.save(exercise)
    }

    fun findExercisesWithTasksBySubGroup(subGroupId: Long): List<ExerciseWithTasksResponse> {
        val subGroupExercises = exerciseRepository.findExercisesBySubGroupId(subGroupId)
        return subGroupExercises.map { it.toDtoWithTasks() }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun createExercise(exerciseCreateDto: ExerciseCreateDto): ExerciseDto {
        val exercise = when (exerciseCreateDto) {
            is ExerciseWordsCreateDto -> {
                val seriesWordsRecord = exerciseCreateDto.toSeriesWordsRecord()
                val exercise = createExercise(seriesWordsRecord, exerciseCreateDto.locale)
                    ?: throw IllegalArgumentException("Exercise with this name (${exerciseCreateDto.exerciseName}) already exist")
                generateAudioFilesAndSave(exerciseCreateDto.words, exerciseCreateDto.locale)
                exercise
            }
            is ExercisePhrasesCreateDto -> {
                val seriesPhrasesRecord = exerciseCreateDto.toSeriesPhrasesRecord()
                val exercise = createExercise(seriesPhrasesRecord, exerciseCreateDto.locale)
                    ?: throw IllegalArgumentException("Exercise with this name (${exerciseCreateDto.exerciseName}) already exist")
                generateAudioFilesAndSave(exerciseCreateDto.phrases.toList(), exerciseCreateDto.locale)
                exercise
            }
            is ExerciseSentencesCreateDto -> {
                val seriesMatrixRecord = exerciseCreateDto.toSeriesMatrixRecord()
                val exercise = createExercise(seriesMatrixRecord, exerciseCreateDto.locale)
                    ?: throw IllegalArgumentException("Exercise with this name (${exerciseCreateDto.exerciseName}) already exist")
                generateAudioFilesAndSave(exerciseCreateDto.words.toFlattenList(), exerciseCreateDto.locale)
                exercise
            }
        }

        return exercise.toDto()
    }

    private fun createExercise(exerciseRecord: Any, locale: Locale): Exercise? =
        recordProcessors.stream()
            .filter { it.isApplicable(exerciseRecord) }
            .findFirst()
            .orElseThrow { RuntimeException("There is no applicable processor for type '${exerciseRecord.javaClass}'") }
            .process(listOf(exerciseRecord) as List<Nothing>, locale)
            .firstOrNull() as Exercise?

    private fun generateAudioFilesAndSave(words: List<String>, locale: Locale) {
        speeds.forEach { speed ->
            run {
                words.forEach { word ->
                    run {
                        val audioFileMetaData = AudioFileMetaData(
                            text = word,
                            locale = locale.locale,
                            voice = wordsService.getDefaultManVoiceForLocale(locale.locale),
                            speed = speed
                        )
                        log.debug("create and save AudioFile: $audioFileMetaData")
                        audioFilesGenerationService.processWord(audioFileMetaData)
                    }
                }
            }
        }
    }
}
