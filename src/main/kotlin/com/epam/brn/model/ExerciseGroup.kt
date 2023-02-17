package com.epam.brn.model

import com.epam.brn.dto.ExerciseGroupDto
import com.epam.brn.enums.BrnLocale
import com.epam.brn.upload.csv.group.GroupRecord
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
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
    @OneToMany(mappedBy = "exerciseGroup", cascade = [(CascadeType.ALL)])
    val series: MutableList<Series> = ArrayList()
) {
    constructor(record: GroupRecord) : this(
        code = record.code,
        locale = record.locale,
        name = record.name,
        description = record.description
    )

    fun toDto() = ExerciseGroupDto(
        id = id,
        locale = locale,
        name = name,
        description = description,
        series = series.map { series -> series.id }.toMutableList()
    )

    override fun toString() = "ExerciseGroup(id=$id, name='$name', locale = $locale, description=$description)"
}
