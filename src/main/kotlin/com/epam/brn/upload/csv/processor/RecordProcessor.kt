package com.epam.brn.upload.csv.processor

import com.epam.brn.upload.csv.record.CsvRecord
import com.epam.brn.upload.csv.record.GroupRecord
import com.epam.brn.upload.csv.record.SeriesGenericRecord
import com.epam.brn.upload.csv.record.SeriesOneRecord
import com.epam.brn.upload.csv.record.SeriesThreeRecord
import com.epam.brn.upload.csv.record.SeriesTwoRecord
import org.springframework.stereotype.Component

@Component
class RecordProcessor(
    private val groupRecordProcessor: GroupRecordProcessor,
    private val seriesGenericRecordProcessor: SeriesGenericRecordProcessor,
    private val seriesOneRecordProcessor: SeriesOneRecordProcessor,
    private val seriesTwoRecordProcessor: SeriesTwoRecordProcessor,
    private val seriesThreeRecordProcessor: SeriesThreeRecordProcessor
) {
    @Suppress("UNCHECKED_CAST")
    fun process(records: List<CsvRecord>): List<Any> {
        return when (records.first()) {
            is GroupRecord -> groupRecordProcessor.process(records as List<GroupRecord>)
            is SeriesGenericRecord -> seriesGenericRecordProcessor.process(records as List<SeriesGenericRecord>)
            is SeriesOneRecord -> seriesOneRecordProcessor.process(records as List<SeriesOneRecord>)
            is SeriesTwoRecord -> seriesTwoRecordProcessor.process(records as List<SeriesTwoRecord>)
            is SeriesThreeRecord -> seriesThreeRecordProcessor.process(records as List<SeriesThreeRecord>)
            else -> throw RuntimeException("Unknown CsvRecord type. Cannot be processed.")
        }
    }
}
