package com.epam.brn.repo

import com.epam.brn.model.Resource
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ResourceRepository : CrudRepository<Resource, Long> {

    fun findByWord(word: String): List<Resource>

    fun findByWordLike(word: String): List<Resource>

    fun findFirstByWordLike(word: String): Optional<Resource>

    fun findFirstByWordAndWordType(word: String, wordType: String): Optional<Resource>

    fun findFirstByWordAndAudioFileUrlLike(word: String, audioFileUrl: String): Optional<Resource>

    fun findFirstByWordAndWordTypeAndAudioFileUrlLike(word: String, wordType: String, audioFileUrl: String): Optional<Resource>

    fun findFirstByWordAndLocaleAndWordType(word: String, locale: String, wordType: String): Optional<Resource>
}
