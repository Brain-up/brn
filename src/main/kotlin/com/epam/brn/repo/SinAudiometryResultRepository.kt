package com.epam.brn.repo

import com.epam.brn.model.SinAudiometryResult
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SinAudiometryResultRepository : JpaRepository<SinAudiometryResult, Long>
