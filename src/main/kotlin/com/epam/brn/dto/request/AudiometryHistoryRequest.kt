package com.epam.brn.dto.request

import com.epam.brn.model.AudiometryHistory
import com.epam.brn.model.AudiometryTask
import com.epam.brn.model.UserAccount
import java.time.LocalDateTime
import javax.validation.constraints.NotNull

data class AudiometryHistoryRequest(
    var id: Long? = null,
    @NotNull
    var audiometryTaskId: Long,
    @NotNull
    var startTime: LocalDateTime,
    var endTime: LocalDateTime?,
    @NotNull
    var executionSeconds: Int?,
    @NotNull
    var tasksCount: Short,
    @NotNull
    var rightAnswers: Int // -- right answers --
) {
    fun toEntity(userAccount: UserAccount, audiometryTask: AudiometryTask) = AudiometryHistory(
        userAccount = userAccount,
        audiometryTask = audiometryTask,
        startTime = this.startTime,
        endTime = this.endTime,
        executionSeconds = this.executionSeconds,
        tasksCount = this.tasksCount,
        rightAnswers = this.rightAnswers,
    )
}
