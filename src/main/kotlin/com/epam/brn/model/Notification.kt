package com.epam.brn.model

import com.epam.brn.dto.notification.NotificationDto
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table
data class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    var userAccount: UserAccount? = null,
    var text: String,
    var scheduledDate: LocalDateTime,
    var checked: Boolean = false
) {
    fun toDto() = NotificationDto(
        id = id,
        userAccount = userAccount,
        text = text,
        date = scheduledDate,
        checked = checked
    )
}
