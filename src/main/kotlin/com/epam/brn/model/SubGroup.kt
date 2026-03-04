package com.epam.brn.model

import com.epam.brn.dto.response.SubGroupResponse
import com.epam.brn.upload.csv.subgroup.SubgroupGenericRecord
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["name", "level"])])
class SubGroup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var name: String,
    @Column(nullable = false)
    var code: String,
    var level: Int,
    var description: String? = "",
    var withPictures: Boolean = false,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_series_id")
    var series: Series,
    @OneToMany(mappedBy = "subGroup", cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    val exercises: MutableList<Exercise> = ArrayList(),
) {
    constructor(record: SubgroupGenericRecord, series: Series) : this(
        series = series,
        level = record.level,
        code = record.code,
        name = record.name,
        description = record.description,
    )

    fun toResponse(pictureUrl: String) = SubGroupResponse(
        seriesId = series.id!!,
        id = id!!,
        name = name,
        pictureUrl = pictureUrl,
        description = description,
        level = level,
        withPictures = withPictures,
        exercises = exercises.map { exercise -> exercise.id }.toMutableList(),
    )
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SubGroup

        if (id != other.id) return false
        if (name != other.name) return false
        if (code != other.code) return false
        if (description != other.description) return false
        if (level != other.level) return false
        if (withPictures != other.withPictures) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + code.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + level
        result = 31 * result + withPictures.hashCode()
        return result
    }
}
