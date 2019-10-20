package com.epam.brn.repo

import com.epam.brn.model.Resource
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ResourceRepository : CrudRepository<Resource, Long> {

    fun findByIdLike(id: String): List<Resource>

    fun findByWordLike(word: String): List<Resource>
}