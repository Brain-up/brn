package com.epam.brn.model

import com.epam.brn.dto.SeriesDto
import com.epam.brn.enums.ExerciseType
import com.epam.brn.upload.csv.series.SeriesGenericRecord
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.SequenceGenerator

@Entity
data class Series(
    @Id
    @GeneratedValue(generator = "series_id_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(
        name = "series_id_seq",
        sequenceName = "series_id_seq",
        allocationSize = 1
    )
    var id: Long? = null,
    var type: String,
    @Column(nullable = false, unique = true)
    var name: String,
    @Column
    var level: Int,
    var description: String? = "",
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_group_id")
    var exerciseGroup: ExerciseGroup,
) {
    @OneToMany(mappedBy = "series", cascade = [(CascadeType.ALL)], fetch = FetchType.EAGER)
    val subGroups: MutableSet<SubGroup> = LinkedHashSet()

    constructor(record: SeriesGenericRecord, exerciseGroup: ExerciseGroup) : this(
        exerciseGroup = exerciseGroup,
        level = record.level,
        type = ExerciseType.valueOf(record.type).toString(),
        name = record.name,
        description = record.description
    )

    fun toDto() = SeriesDto(
        group = exerciseGroup.id,
        id = id,
        level = level,
        name = name,
        type = ExerciseType.valueOf(type),
        description = description,
        subGroups = subGroups.map { subGroup -> subGroup.id }.toMutableSet()
    )
}
