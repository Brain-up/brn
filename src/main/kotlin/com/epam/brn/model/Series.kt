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
import javax.persistence.SequenceGenerator

@Entity
data class Series(
    @Id
    @GeneratedValue(generator = "series_id_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(
        name = "series_id_seq",
        sequenceName = "series_id_seq",
        allocationSize = 50
    )
    var id: Long? = null,
    @Column(nullable = false, unique = true)
    var name: String,
    @Column
    var description: String? = "",
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_group_id")
    var exerciseGroup: ExerciseGroup,
    @OneToMany(mappedBy = "series", cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    val exercises: MutableSet<Exercise> = LinkedHashSet()
) {

    constructor(record: SeriesGenericRecord, exerciseGroup: ExerciseGroup) : this(
        id = record.seriesId,
        name = record.name,
        description = record.description,
        exerciseGroup = exerciseGroup
    )

    fun toDto() = SeriesDto(
        group = exerciseGroup.id,
        id = id,
        name = name,
        description = description,
        exercises = exercises.map { exercise -> exercise.id }.toMutableSet()
    )

    override fun toString() = "Series(id=$id, name='$name', description='$description')"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Series

        if (id != other.id) return false
        if (name != other.name) return false
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
