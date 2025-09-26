package com.epam.brn.model

import com.epam.brn.dto.response.AudiometryResponse
import com.epam.brn.enums.AudiometryType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

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
    val audiometryTasks: MutableList<AudiometryTask> = ArrayList(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Audiometry
        if (id != other.id) return false
        if (name != other.name) return false
        if (description != other.description) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + audiometryType.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }

    fun toDtoWithoutTasks() =
        AudiometryResponse(
            id,
            locale,
            name,
            AudiometryType.valueOf(audiometryType),
            description,
            emptyList<String>(),
        )

    fun toDtoWithTasks(tasks: List<AudiometryTask>): AudiometryResponse =
        AudiometryResponse(
            id,
            locale,
            name,
            AudiometryType.valueOf(audiometryType),
            description,
            tasks.map { it.toDto() },
        )
}
