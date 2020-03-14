package com.epam.brn.csv.converter.impl

import com.epam.brn.constant.BrnInitFiles
import com.epam.brn.csv.converter.FileNameToUploader
import com.epam.brn.csv.converter.Uploader
import com.epam.brn.csv.converter.impl.firstSeries.ExerciseUploader
import com.epam.brn.csv.converter.impl.firstSeries.GroupUploader
import com.epam.brn.csv.converter.impl.firstSeries.SeriesOneUploader
import com.epam.brn.csv.converter.impl.firstSeries.SeriesUploader
import com.epam.brn.csv.converter.impl.secondSeries.SeriesTwoUploader
import org.springframework.stereotype.Service

@Service
class DefaultFileNameToUploader(
    groupUploader: GroupUploader,
    exerciseUploader: ExerciseUploader,
    seriesUploader: SeriesUploader,
    seriesOneUploader: SeriesOneUploader,
    seriesTwoUploader: SeriesTwoUploader
) : FileNameToUploader {

    val fileNameToUploaders: Map<BrnInitFiles, Uploader<out Any, out Any>?> = mapOf(
        BrnInitFiles.GROUPS to groupUploader,
        BrnInitFiles.EXERCISES to exerciseUploader,
        BrnInitFiles.SERIES to seriesUploader,
        BrnInitFiles.SERIES_ONE to seriesOneUploader,
        BrnInitFiles.SERIES_TWO to seriesTwoUploader,
        BrnInitFiles.SERIES_THREE to null
    )

    override fun getUploaderFor(initFile: BrnInitFiles): Uploader<out Any, out Any>? {
        return fileNameToUploaders[initFile]
    }
}
