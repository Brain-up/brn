package com.epam.brn.upload.csv

import com.epam.brn.enums.BrnLocale

interface RecordProcessor<Record, Entity> {

    fun isApplicable(record: Any): Boolean

    fun process(records: List<Record>, locale: BrnLocale = BrnLocale.RU): List<Entity>
}
