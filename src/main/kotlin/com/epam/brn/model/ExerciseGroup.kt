package com.epam.brn.model

import com.epam.brn.dto.ExerciseGroupDto
import com.epam.brn.enums.BrnLocale
import com.epam.brn.upload.csv.group.GroupRecord
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

// The discrepancy in naming with "Groups" endpoint and "ExerciseGroup" entity is due to group being a reserved word in db.
@Entity
@Table(name = "exercise_group")
class ExerciseGroup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    var code: String,
    @Column(nullable = false)
    val locale: String = BrnLocale.RU.name,
    @Column(nullable = false, unique = true)
    val name: String,
    @Column
    val description: String? = "",
    @OneToMany(mappedBy = "exerciseGroup", fetch = FetchType.LAZY, cascade = [(CascadeType.ALL)])
    val series: MutableList<Series> = ArrayList(),
) {
    constructor(record: GroupRecord) : this(
        code = record.code,
        locale = record.locale,
        name = record.name,
        description = record.description,
    )

    fun toDto() = ExerciseGroupDto(
        id = id,
        locale = locale,
        name = name,
        description = description,
        series = series.map { series -> series.id }.toMutableList(),
    )

    fun toDtoWithoutSeries() = ExerciseGroupDto(
        id = id,
        locale = locale,
        name = name,
        description = description,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ExerciseGroup
        if (id != other.id) return false
        if (locale != other.locale) return false
        if (name != other.name) return false
        if (description != other.description) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + locale.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }
}
