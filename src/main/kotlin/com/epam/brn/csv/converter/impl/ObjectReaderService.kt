package com.epam.brn.csv.converter.impl

import com.epam.brn.csv.converter.ObjectReaderProvider
import com.epam.brn.csv.dto.ExerciseCsv
import com.epam.brn.csv.dto.GroupCsv
import com.epam.brn.csv.dto.SeriesCsv
import com.epam.brn.csv.dto.TaskCsv
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service

@Service
class ObjectReaderService {
    @Bean
    fun seriesOneReader() = object : ObjectReaderProvider<TaskCsv> {
        override fun <Type> objectReader(): ObjectReader {
            val csvMapper = CsvMapper()

            val csvSchema = csvMapper
                .schemaFor(TaskCsv::class.java)
                .withColumnSeparator(' ')
                .withLineSeparator(" ")
                .withColumnReordering(true)
                .withArrayElementSeparator(",")
                .withHeader()
            return csvMapper.readerWithTypedSchemaFor(TaskCsv::class.java)
                .with(csvSchema)
        }
    }

    @Bean
    fun groupReader() = object : ObjectReaderProvider<GroupCsv> {
        override fun <Type> objectReader(): ObjectReader {
            val csvMapper = CsvMapper().apply {
                enable(com.fasterxml.jackson.dataformat.csv.CsvParser.Feature.TRIM_SPACES)
            }

            val csvSchema = csvMapper
                .schemaFor(GroupCsv::class.java)
                .withColumnSeparator(',')
                .withLineSeparator(" ")
                .withColumnReordering(true)
                .withHeader()

            return csvMapper
                .readerWithTypedSchemaFor(GroupCsv::class.java)
                .with(csvSchema)
        }
    }

    @Bean
    fun seriesReader() = object : ObjectReaderProvider<SeriesCsv> {
        override fun <Type> objectReader(): ObjectReader {
            val csvMapper = CsvMapper().apply {
                enable(com.fasterxml.jackson.dataformat.csv.CsvParser.Feature.TRIM_SPACES)
            }

            val csvSchema = csvMapper
                .schemaFor(SeriesCsv::class.java)
                .withColumnSeparator(',')
                .withLineSeparator(" ")
                .withColumnReordering(true)
                .withHeader()

            return csvMapper
                .readerWithTypedSchemaFor(SeriesCsv::class.java)
                .with(csvSchema)
        }
    }

    @Bean
    fun exerciseReader() = object : ObjectReaderProvider<ExerciseCsv> {
        override fun <Type> objectReader(): ObjectReader {
            val csvMapper = CsvMapper().apply {
                enable(com.fasterxml.jackson.dataformat.csv.CsvParser.Feature.TRIM_SPACES)
            }

            val csvSchema = csvMapper
                .schemaFor(ExerciseCsv::class.java)
                .withColumnSeparator(',')
                .withLineSeparator(" ")
                .withColumnReordering(true)
                .withHeader()

            return csvMapper
                .readerWithTypedSchemaFor(ExerciseCsv::class.java)
                .with(csvSchema)
        }
    }

    @Bean
    fun seriesTwoReader() = object : ObjectReaderProvider<Map<String, Any>> {
        override fun <Type> objectReader(): ObjectReader {
            val csvMapper = CsvMapper()

            val csvSchema = CsvSchema
                .emptySchema()
                .withHeader()
                .withColumnSeparator(',')
                .withColumnReordering(true)
                .withLineSeparator(",")
                .withArrayElementSeparator(";")

            return csvMapper
                .readerFor(Map::class.java)
                .with(csvSchema)
        }
    }
}
