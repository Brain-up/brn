package com.epam.brn.dto.notification

import com.epam.brn.model.Notification
import java.time.LocalDateTime

data class NotificationDto(
    var id: Long? = null,
    var userAccount: Long? = null,
    var text: String,
    var date: LocalDateTime,
    var checked: Boolean
) {
    fun toEntity() = Notification(
        id = this.id,
        text = this.text,
        scheduledDate = this.date,
        checked = this.checked
    )
}
