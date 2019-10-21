package com.epam.brn.repo

import com.epam.brn.model.StudyHistory
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface StudyHistoryRepository : CrudRepository<StudyHistory, Long> {

    fun findByUserAccount_IdAndExercise_Id(userId: Long?, exerciseId: Long?): Optional<StudyHistory>
}