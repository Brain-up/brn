package com.epam.brn.upload.csv

import com.epam.brn.enums.Locale

interface RecordProcessor<Record, Entity> {

    fun isApplicable(record: Any): Boolean

    fun process(records: List<Record>, locale: Locale = Locale.RU): List<Entity>
}
