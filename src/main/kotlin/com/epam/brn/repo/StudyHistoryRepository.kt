package com.epam.brn.repo

import com.epam.brn.model.StudyHistory
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface StudyHistoryRepository : CrudRepository<StudyHistory, Long> {

    fun findByIdLike(id: String): List<StudyHistory>
}