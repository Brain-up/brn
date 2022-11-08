package com.epam.brn.repo

import com.epam.brn.model.GitHubUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GitHubUserRepository : JpaRepository<GitHubUser, Long>
