package com.epam.brn.model

import com.epam.brn.dto.response.SubGroupResponse
import com.epam.brn.upload.csv.subgroup.SubgroupGenericRecord
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
import javax.persistence.Table
import javax.persistence.UniqueConstraint

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
    val exercises: MutableList<Exercise> = ArrayList()
) {

    constructor(record: SubgroupGenericRecord, series: Series) : this(
        series = series,
        level = record.level,
        code = record.code,
        name = record.name,
        description = record.description
    )

    fun toResponse(pictureUrl: String) = SubGroupResponse(
        seriesId = series.id!!,
        id = id!!,
        name = name,
        pictureUrl = pictureUrl,
        description = description,
        level = level,
        withPictures = withPictures,
        exercises = exercises.map { exercise -> exercise.id }.toMutableList()
    )

    override fun toString() =
        "SubGroup(id=$id, name='$name', code='$code', description=$description, level=$level, " +
            "withPictures=$withPictures)"
}
