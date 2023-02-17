package com.epam.brn.model

import com.epam.brn.dto.response.AudiometryResponse
import com.epam.brn.enums.AudiometryType
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["locale", "name", "audiometryType"])])
class Audiometry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val locale: String,
    @Column(nullable = false, unique = true)
    val name: String,
    @Column(nullable = false)
    val audiometryType: String,
    var description: String? = "",
    @OneToMany(mappedBy = "audiometry", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val audiometryTasks: MutableList<AudiometryTask> = ArrayList()
) {

    override fun toString() =
        "Audiometry(id=$id, name='$name', audiometryType=$audiometryType, description=$description)"

    fun toDtoWithoutTasks() = AudiometryResponse(
        id,
        locale,
        name,
        AudiometryType.valueOf(audiometryType),
        description,
        emptyList<String>()
    )

    fun toDtoWithTasks(tasks: List<AudiometryTask>): AudiometryResponse {
        return AudiometryResponse(
            id,
            locale,
            name,
            AudiometryType.valueOf(audiometryType),
            description,
            tasks.map { it.toDto() }
        )
    }
}
