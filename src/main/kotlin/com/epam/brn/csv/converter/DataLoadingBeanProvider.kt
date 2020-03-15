package com.epam.brn.csv.converter

import org.springframework.data.repository.CrudRepository

interface DataLoadingBeanProvider<Csv, Entity> {
    fun shouldProcess(fileName: String): Boolean
    fun repository(): CrudRepository<Entity, Long>
    fun objectReaderProvider(): ObjectReaderProvider<Csv>
    fun converter(): CsvToEntityConverter<Csv, Entity>
}
