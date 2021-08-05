package com.epam.brn.dto.request

import com.epam.brn.model.AudiometryHistory
import com.epam.brn.model.AudiometryTask
import com.epam.brn.model.Headphones
import com.epam.brn.model.UserAccount
import java.time.LocalDateTime
import javax.annotation.Nullable
import javax.validation.constraints.NotNull

data class AudiometryHistoryRequest(
    @NotNull
    var audiometryTaskId: Long,
    @NotNull
    var startTime: LocalDateTime,
    var endTime: LocalDateTime?,
    @NotNull
    var executionSeconds: Int?,
    @NotNull
    var tasksCount: Short,
    @Nullable
    var rightAnswers: Int = 0,
    @NotNull
    var headphones: Long? = null,
    var sinAudiometryResults: Map<Int, Int>? = mutableMapOf()
) {
    fun toEntity(userAccount: UserAccount, audiometryTask: AudiometryTask, headphones: Headphones?) = AudiometryHistory(
        userAccount = userAccount,
        audiometryTask = audiometryTask,
        startTime = this.startTime,
        endTime = this.endTime,
        executionSeconds = this.executionSeconds,
        tasksCount = this.tasksCount,
        rightAnswers = this.rightAnswers,
        rightAnswersIndex = rightAnswers.toFloat().div(tasksCount),
        headphones = headphones,
    )
}
