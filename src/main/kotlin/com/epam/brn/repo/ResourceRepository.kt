package com.epam.brn.repo

import com.epam.brn.model.Resource
import java.util.Optional
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ResourceRepository : CrudRepository<Resource, Long> {

    fun findByWordLike(word: String): List<Resource>

    fun findFirstByWordLike(word: String): Optional<Resource>

    fun findFirstByWordAndAudioFileUrlLike(word: String, audioFileUrl: String): Optional<Resource>
}
