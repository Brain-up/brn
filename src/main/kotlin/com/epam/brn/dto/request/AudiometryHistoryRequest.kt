package com.epam.brn.dto.request

import com.epam.brn.model.AudiometryHistory
import com.epam.brn.model.AudiometryTask
import com.epam.brn.model.Headphones
import com.epam.brn.model.UserAccount
import java.time.LocalDateTime

data class AudiometryHistoryRequest(
    var audiometryTaskId: Long,
    var startTime: LocalDateTime,
    var endTime: LocalDateTime?,
    var executionSeconds: Int,
    var tasksCount: Short,
    var rightAnswers: Int? = 0,
    var headphones: Long,
    var sinAudiometryResults: Map<Int, Int>? = mutableMapOf(),
) {
    fun toEntity(
        userAccount: UserAccount,
        audiometryTask: AudiometryTask,
        headphones: Headphones?,
    ) = AudiometryHistory(
        userAccount = userAccount,
        audiometryTask = audiometryTask,
        startTime = this.startTime,
        endTime = this.endTime,
        executionSeconds = this.executionSeconds,
        tasksCount = this.tasksCount!!,
        rightAnswers = this.rightAnswers!!,
        rightAnswersIndex = rightAnswers!!.toFloat().div(tasksCount!!),
        headphones = headphones,
    )
}
