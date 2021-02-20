package com.epam.brn.repo

import com.epam.brn.model.AudiometryHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AudiometryHistoryRepository : JpaRepository<AudiometryHistory, Long>
