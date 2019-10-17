package com.epam.brn.service

import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.Task
import com.epam.brn.repo.ExerciseGroupRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Service

@Service
class LoadHandBook(@Autowired private val exerciseGroupRepository: ExerciseGroupRepository) :
    ApplicationListener<ContextRefreshedEvent> {

    override fun onApplicationEvent(event: ContextRefreshedEvent) {

        val group = ExerciseGroup(name = "речевые упражения", description = "речевые упражения")
        val series1 =
            Series(name = "распознование слов", description = "распознование слов", exerciseGroup = group)
        val series2 = Series(
            name = "диахоничкеское слушание",
            description = "диахоничкеское слушание",
            exerciseGroup = group
        )
        group.series.addAll(setOf(series1, series2))
        val exercise = Exercise(name = "First", description = "desc", level = 0, series = series1)
        series1.exercises.add(exercise)
        val firstResource =
            Resource(audioFileUrl = "audio_f", word = "slon", pictureFileUrl = "picture_f", soundsCount = 0)
        val secondResource =
            Resource(audioFileUrl = "audio_s", word = "lon", pictureFileUrl = "picture_s", soundsCount = 0)
        val thirdResource =
            Resource(audioFileUrl = "audio_t", word = "slo", pictureFileUrl = "picture_t", soundsCount = 0)
        val task = Task(
            name = "task",
            serialNumber = 1,
            exercise = exercise,
            correctAnswer = firstResource
        )
        task.answerOptions.addAll(setOf(firstResource, secondResource, thirdResource))
        exercise.tasks.add(task)
        exerciseGroupRepository.save(group)
    }
}