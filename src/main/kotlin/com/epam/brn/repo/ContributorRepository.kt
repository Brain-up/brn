package com.epam.brn.repo

import com.epam.brn.enums.ContributorType
import com.epam.brn.model.Contributor
import com.epam.brn.model.GitHubUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ContributorRepository : JpaRepository<Contributor, Long> {
    fun findByGitHubUser(gitHubUser: GitHubUser): Contributor?

    @Query(
        "SELECT c FROM Contributor c LEFT JOIN FETCH c.gitHubUser LEFT JOIN FETCH c.contacts " +
            "WHERE c.type = ?1 AND c.active = true ORDER BY c.contribution DESC",
    )
    fun findAllByType(type: ContributorType): List<Contributor>
}
