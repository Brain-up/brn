package com.epam.brn.model

import com.epam.brn.dto.notification.NotificationDto
import java.time.LocalDateTime
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table
class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    var userAccount: UserAccount? = null,
    var text: String,
    var scheduledDate: LocalDateTime,
    var checked: Boolean = false,
) {
    fun toDto() =
        NotificationDto(
            id = id,
            userAccount = userAccount!!.id,
            text = text,
            date = scheduledDate,
            checked = checked,
        )
}
