package com.epam.brn.model

import com.epam.brn.dto.SeriesDto
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
data class Series(
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
        subGroups = subGroups.map { subGroup -> subGroup.id }.toMutableSet()
    )

    override fun toString() = "Series(id=$id, type=$type, level=$level, name='$name', description='$description')"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Series

        if (id != other.id) return false
        if (name != other.name) return false
        if (type != other.type) return false
        if (description != other.description) return false
        if (exerciseGroup != other.exerciseGroup) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + exerciseGroup.hashCode()
        return result
    }
}
