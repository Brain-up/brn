package com.epam.brn.service

import com.epam.brn.dto.TaskDto
import com.epam.brn.repo.TaskRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TaskService(@Autowired val taskRepository: TaskRepository) {

    private val log = logger()

    fun findTasks(exerciseId: String): List<TaskDto> {
        // todo: get from db
        if (exerciseId.equals("1"))
            return listOf(
                TaskDto("1", "бал", 1, "no_noise/бал.mp3", listOf("бал", "бум", "быль", "боль", "зал", "мал"), "1"),
                TaskDto("2", "бум", 2, "no_noise/бум.mp3", listOf("бум", "зум", "кум", "лунь", "дума", "куб"), "1"),
                TaskDto(
                    "3",
                    "быль",
                    3,
                    "no_noise/быль.mp3",
                    listOf("быль", "даль", "жаль", "лунь", "сеть", "топь"),
                    "1"
                ),
                TaskDto(
                    "4",
                    "вить",
                    4,
                    "no_noise/вить.mp3",
                    listOf("вить", "быть", "ныть", "жить", "сеть", "пить"),
                    "1"
                ),
                TaskDto("5", "гад", 5, "no_noise/гад.mp3", listOf("гад", "мат", "клад", "пат", "дать", "спать"), "1")
            )
        else if (exerciseId.equals("2"))
            return listOf(
                TaskDto("1", "бал", 1, "noise_0db/бал.mp3", listOf("бал", "бум", "быль", "боль", "зал", "мал"), "1"),
                TaskDto("2", "бум", 2, "noise_0db/бум.mp3", listOf("бум", "зум", "кум", "лунь", "дума", "куб"), "1"),
                TaskDto(
                    "3",
                    "быль",
                    3,
                    "noise_0db/быль.mp3",
                    listOf("быль", "даль", "жаль", "лунь", "сеть", "топь"),
                    "1"
                ),
                TaskDto(
                    "4",
                    "вить",
                    4,
                    "noise_0db/вить.mp3",
                    listOf("вить", "быть", "ныть", "жить", "сеть", "пить"),
                    "1"
                ),
                TaskDto("5", "гад", 5, "noise_0db/гад.mp3", listOf("гад", "мат", "клад", "пат", "дать", "спать"), "1")
            )
        else if (exerciseId.equals("3"))
            return listOf(
                TaskDto("1", "бал", 1, "noise_6db/бал.mp3", listOf("бал", "бум", "быль", "боль", "зал", "мал"), "1"),
                TaskDto("2", "бум", 2, "noise_6db/бум.mp3", listOf("бум", "зум", "кум", "лунь", "дума", "куб"), "1"),
                TaskDto(
                    "3",
                    "быль",
                    3,
                    "noise_6db/быль.mp3",
                    listOf("быль", "даль", "жаль", "лунь", "сеть", "топь"),
                    "1"
                ),
                TaskDto(
                    "4",
                    "вить",
                    4,
                    "noise_6db/вить.mp3",
                    listOf("вить", "быть", "ныть", "жить", "сеть", "пить"),
                    "1"
                ),
                TaskDto("5", "гад", 5, "noise_6db/гад.mp3", listOf("гад", "мат", "клад", "пат", "дать", "спать"), "1")
            )
        return emptyList()
    }
}