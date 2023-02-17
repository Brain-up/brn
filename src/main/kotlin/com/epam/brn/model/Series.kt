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

@Entity
class Series(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @OneToMany(mappedBy = "series", cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    val subGroups: MutableSet<SubGroup> = LinkedHashSet()
) {

    constructor(record: SeriesGenericRecord, exerciseGroup: ExerciseGroup) : this(
        exerciseGroup = exerciseGroup,
        level = record.level,
        type = ExerciseType.valueOf(record.type).toString(),
        name = record.name,
        description = record.description
    )

    fun toDto() = SeriesDto(
        group = exerciseGroup.id!!,
        id = id,
        level = level,
        name = name,
        type = ExerciseType.valueOf(type),
        description = description,
        subGroups = subGroups
            .sortedBy { it.withPictures }
            .map { subGroup -> subGroup.id!! }
    )

    override fun toString() = "Series(id=$id, type=$type, level=$level, name='$name', description='$description')"
}
