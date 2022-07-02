package com.epam.brn.model.projection

import java.time.LocalDateTime

interface FirstLastStudyView {
    val firstStudy: LocalDateTime?
    val lastStudy: LocalDateTime?
}
