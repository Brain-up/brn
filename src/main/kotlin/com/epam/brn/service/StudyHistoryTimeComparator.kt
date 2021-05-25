package com.epam.brn.service

import com.epam.brn.model.StudyHistory
import org.springframework.stereotype.Component

@Component
class StudyHistoryTimeComparator : Comparator<StudyHistory> {
    override fun compare(first: StudyHistory?, second: StudyHistory?): Int {
        return first!!.startTime.compareTo(second!!.startTime)
    }
}
