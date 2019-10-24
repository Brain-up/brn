package com.epam.brn.repo

import com.epam.brn.model.StudyHistory
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface StudyHistoryRepository : CrudRepository<StudyHistory, Long> {

    fun findByUserAccount_Id(id: Long?): List<StudyHistory>
    fun findByUserAccount_IdAndExercise_Id(userId: Long?, exerciseId: Long?): Optional<StudyHistory>
}