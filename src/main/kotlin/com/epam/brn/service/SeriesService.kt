package com.epam.brn.service

import com.epam.brn.dto.SeriesDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Series
import com.epam.brn.repo.SeriesRepository
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.LineNumberReader
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class SeriesService(private val seriesRepository: SeriesRepository) {

    val dataFormatNumLines = 5

    private val log = logger()

    fun findSeriesForGroup(groupId: Long): List<SeriesDto> {
        log.debug("try to find series for groupId=$groupId")
        val series = seriesRepository.findByExerciseGroupLike(groupId)
        return series.map { seriesEntry -> seriesEntry.toDto() }
    }

    fun findSeriesDtoForId(seriesId: Long): SeriesDto {
        log.debug("try to find series for seriesId=$seriesId")
        val series = seriesRepository.findById(seriesId)
            .orElseThrow { EntityNotFoundException("no series was found for id=$seriesId") }
        return series.toDto()
    }

    fun findSeriesForId(seriesId: Long): Series {
        log.debug("try to find series for seriesId=$seriesId")
        return seriesRepository.findById(seriesId)
            .orElseThrow { EntityNotFoundException("no series was found for id=$seriesId") }
    }

    fun findSeriesWithExercisesForId(seriesId: Long): Series {
        log.debug("try to find series for seriesId=$seriesId")
        return seriesRepository.findSeriesWithExercisesById(seriesId)
            .orElseThrow { EntityNotFoundException("no series was found for id=$seriesId") }
    }

    fun save(series: Series): Series {
        return seriesRepository.save(series)
    }

    fun getSeriesUploadFileFormat(seriesId: Long): String {
        val seriesFileName = InitialDataLoader.fileNameForSeries(seriesId = seriesId)
        return try {
            Thread.currentThread().contextClassLoader.getResourceAsStream("initFiles/$seriesFileName").use { readFirstNLines(it, dataFormatNumLines) }
        } catch (exception: Exception) {
            throw IOException("First $dataFormatNumLines lines from file $seriesFileName for series $seriesId could not be read", exception)
        }
    }

    private fun readFirstNLines(inputStream: InputStream, numLines: Int): String {
        val lineNumberReader = LineNumberReader(InputStreamReader(inputStream))
        val lines = StringBuilder()
        var line = lineNumberReader.readLine()
        while (line != null && lineNumberReader.lineNumber <= numLines) {
            if (lineNumberReader.lineNumber > 1) { lines.append("\r\n") }
            lines.append(line)
            line = lineNumberReader.readLine()
        }
        return lines.toString()
    }
}
