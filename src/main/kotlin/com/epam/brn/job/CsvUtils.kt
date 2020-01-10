package com.epam.brn.job

import com.epam.brn.constant.BrnCsvTypes

class CsvUtils {

    companion object {

        private val csvContentTypes = listOf(
            BrnCsvTypes.TEXT_CSV_TYPE,
            BrnCsvTypes.MS_EXCEL_TYPE,
            BrnCsvTypes.PLAIN_TYPE,
            BrnCsvTypes.TSV_TYPE,
            BrnCsvTypes.OCTET_STREAM
        )

        fun isFileContentTypeCsv(contentType: String): Boolean {
            return csvContentTypes.contains(contentType)
        }
    }
}
