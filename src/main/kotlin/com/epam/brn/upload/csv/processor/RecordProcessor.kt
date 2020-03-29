package com.epam.brn.upload.csv.processor

interface RecordProcessor<Record, Entity> {

    fun isApplicable(record: Any): Boolean

    fun process(records: List<Record>): List<Entity>
}
