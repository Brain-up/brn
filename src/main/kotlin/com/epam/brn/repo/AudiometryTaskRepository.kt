package com.epam.brn.repo

import com.epam.brn.model.AudiometryTask
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AudiometryTaskRepository : JpaRepository<AudiometryTask, Long>
