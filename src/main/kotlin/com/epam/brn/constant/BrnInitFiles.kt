package com.epam.brn.constant

enum class BrnInitFiles {
    GROUPS("groups.csv"),
    SERIES("series.csv"),
    EXERCISES("exercises.csv"),
    SERIES_ONE(1),
    SERIES_TWO(2),
    SERIES_THREE(3);

    private var fileName: String? = null
    private var seriesId: Long? = null

    constructor(fileName: String) {
        this.fileName = fileName
    }

    constructor(seriesId: Long) {
        this.seriesId = seriesId
    }

    fun getFileName(): String {
        return this.fileName ?: getFileNameForSeriesId(this.seriesId!!)
    }

    companion object {
        fun getFileNameForSeriesId(seriesId: Long): String {
            return "${seriesId}_series.csv"
        }
    }
}
