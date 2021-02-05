package com.epam.brn.model

import com.epam.brn.dto.ExerciseGroupDto
import com.epam.brn.upload.csv.group.GroupRecord
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.SequenceGenerator

// The discrepancy in naming with "Groups" endpoint and "ExerciseGroup" entity is due to group being a reserved word in db.
@Entity
data class ExerciseGroup(
    @Id
    @GeneratedValue(generator = "exercise_group_id_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(
        name = "exercise_group_id_seq",
        sequenceName = "exercise_group_id_seq",
        allocationSize = 1
    )
    val id: Long? = null,
    @Column(nullable = false)
    val locale: String = "ru",
    @Column(nullable = false, unique = true)
    val name: String,
    @Column
    val description: String? = "",

) {
    @OneToMany(mappedBy = "exerciseGroup", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val series: MutableList<Series> = ArrayList()

    constructor(record: GroupRecord) : this(
        id = record.groupId,
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
}
