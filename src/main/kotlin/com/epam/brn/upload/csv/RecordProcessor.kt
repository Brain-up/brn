package com.epam.brn.upload.csv

interface RecordProcessor<Record, Entity> {

    fun isApplicable(record: Any): Boolean

    fun process(records: List<Record>): List<Entity>
}
