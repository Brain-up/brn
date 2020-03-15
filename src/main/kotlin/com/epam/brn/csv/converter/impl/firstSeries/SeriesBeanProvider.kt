package com.epam.brn.csv.converter.impl.firstSeries

import com.epam.brn.csv.converter.CsvToEntityConverter
import com.epam.brn.csv.converter.ObjectReaderProvider
import com.epam.brn.csv.converter.DataLoadingBeanProvider
import com.epam.brn.csv.dto.SeriesCsv
import com.epam.brn.model.Series
import com.epam.brn.repo.SeriesRepository
import org.springframework.stereotype.Component

@Component
class SeriesBeanProvider(
    private val repository: SeriesRepository,
    private val objectReaderProvider: ObjectReaderProvider<SeriesCsv>,
    private val converter: CsvToEntityConverter<SeriesCsv, Series>
) : DataLoadingBeanProvider<SeriesCsv, Series> {
    override fun shouldProcess(fileName: String) = "series.csv" == fileName
    override fun repository() = repository
    override fun objectReaderProvider() = objectReaderProvider
    override fun converter() = converter
}
