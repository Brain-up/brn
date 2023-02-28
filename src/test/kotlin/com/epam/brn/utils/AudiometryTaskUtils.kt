package com.epam.brn.utils

import com.epam.brn.enums.FrequencyZone
import com.epam.brn.model.AudiometryTask
import com.epam.brn.model.Resource

fun AudiometryTask.copy(id: Long? = this.id, answerOptions: MutableList<Resource> = this.answerOptions) = AudiometryTask(
    id = id,
    level = 1,
    audiometryGroup = "A",
    frequencyZone = FrequencyZone.LOW.name,
    minFrequency = 200,
    maxFrequency = 400,
    answerOptions = answerOptions
)
