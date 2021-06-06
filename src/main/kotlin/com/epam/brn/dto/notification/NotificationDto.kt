package com.epam.brn.dto.notification

import com.epam.brn.model.UserAccount
import java.time.LocalDateTime

data class NotificationDto(
    var id: Long? = null,
    var userAccount: UserAccount? = null,
    var text: String,
    var date: LocalDateTime,
    var checked: Boolean
)
