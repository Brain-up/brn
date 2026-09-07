package com.epam.brn.service

import com.epam.brn.dto.request.contributor.ContactRequest
import com.epam.brn.dto.request.contributor.ContributorRequest
import com.epam.brn.enums.ContactType
import com.epam.brn.enums.ContributorType
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Contact
import com.epam.brn.model.Contributor
import com.epam.brn.model.GitHubUser
import com.epam.brn.repo.ContributorRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class ContributorServiceTest {
    @InjectMockKs
    private lateinit var contributorService: ContributorServiceImpl

    @MockK
    private lateinit var contributorRepository: ContributorRepository

    private val githubRepositoryName = "brn"

    @Test
    fun `should get all contributors`() {
        // GIVEN
        val contributor = createContributor(id = 1, name = "Contributor", contribution = 5)
        val contributorList = listOf(contributor)
        every { contributorRepository.findAllWithAssociations() } returns contributorList

        // WHEN
        val actualResult = contributorService.getAllContributors()

        // THEN
        actualResult.size shouldBe 1
        val actualContributor = actualResult[0]
        actualContributor.name shouldBe contributor.name
        actualContributor.contribution shouldBe contributor.contribution
        actualContributor.type shouldBe contributor.type
        verify(exactly = 1) { contributorRepository.findAllWithAssociations() }
    }

    @Test
    fun `should get all contributors with empty list`() {
        // GIVEN
        every { contributorRepository.findAllWithAssociations() } returns emptyList()

        // WHEN
        val actualResult = contributorService.getAllContributors()

        // THEN
        actualResult.size shouldBe 0
        verify(exactly = 1) { contributorRepository.findAllWithAssociations() }
    }

    @Test
    fun `should get contributors by type`() {
        // GIVEN
        val contributor = createContributor(id = 1, name = "Contributor", contribution = 5)
        val contributorList = listOf(contributor)
        every { contributorRepository.findAllByType(contributor.type) } returns contributorList

        // WHEN
        val actualResult = contributorService.getContributors("ru-ru", contributor.type)

        // THEN
        actualResult.size shouldBe 1
        val actualContributor = actualResult[0]
        actualContributor.name shouldBe contributor.name
        actualContributor.contribution shouldBe contributor.contribution
        actualContributor.type shouldBe contributor.type
    }

    @Test
    fun `should add contributor`() {
        // GIVEN
        val contributorRequest = mockk<ContributorRequest>()
        val contributor = createContributor(id = 1, name = "Contributor", contribution = 5)
        every { contributorRequest.toEntity() } returns contributor
        every { contributorRepository.save(contributor) } returns contributor

        // WHEN
        val actualResult = contributorService.createContributor(contributorRequest)

        // THEN
        actualResult.name shouldBe contributor.name
        actualResult.contribution shouldBe contributor.contribution
        actualResult.type shouldBe contributor.type
    }

    @Test
    fun `should update contributor`() {
        // GIVEN
        val contributorId = 1L
        val originalEmailContact = Contact(1L, ContactType.EMAIL, "user@test.com")
        val originalPhoneContact = Contact(2L, ContactType.PHONE, "1234567")
        val contributor =
            createContributor(
                contributorId,
                "Contributor",
                5L,
                mutableSetOf(originalEmailContact, originalPhoneContact),
            )

        val updatedContact = ContactRequest(ContactType.EMAIL, "new@test.com")
        val contributorRequest = createContributorRequest("Updated Contributor", 6L, setOf(updatedContact))
        every { contributorRepository.findById(contributorId) } returns Optional.of(contributor)
        every { contributorRepository.save(contributor) } returns contributor

        // WHEN
        val actualResult = contributorService.updateContributor(contributorId, contributorRequest)

        // THEN
        actualResult.name shouldBe contributorRequest.name
        actualResult.contribution shouldBe contributorRequest.contribution
        val updatedContacts = actualResult.contacts
        updatedContacts.size shouldBe 1
        updatedContacts.elementAt(0).value shouldBe updatedContact.value
    }

    @Test
    fun `should throw exception for invalid id`() {
        // GIVEN
        val contributorId = -1L
        val contributorRequest = mockk<ContributorRequest>()
        every { contributorRepository.findById(contributorId) } returns Optional.empty()

        // WHEN & THEN
        shouldThrow<EntityNotFoundException> {
            contributorService.updateContributor(contributorId, contributorRequest)
        }
    }

    @Test
    fun `should create contributor by github user with contact`() {
        // GIVEN
        val gitHubUserMockK = mockk<GitHubUser>()
        val contributorMockK = mockk<Contributor>()

        every { contributorRepository.findByGitHubUser(gitHubUserMockK) } returns null
        every { contributorRepository.save(any()) } returns contributorMockK
        every { contributorMockK.repositoryName } returns githubRepositoryName

        every { gitHubUserMockK.name } returns "name"
        every { gitHubUserMockK.company } returns "company"
        every { gitHubUserMockK.avatarUrl } returns "avatarUrl"
        every { gitHubUserMockK.bio } returns "bio"
        every { gitHubUserMockK.email } returns "mail"
        every { gitHubUserMockK.contributions } returns 1

        // WHEN
        val resultContributor = contributorService.createOrUpdateByGitHubUser(gitHubUserMockK, githubRepositoryName)
        // THEN
        resultContributor shouldBe contributorMockK
        resultContributor.repositoryName shouldBe githubRepositoryName
        verify(exactly = 1) { contributorRepository.findByGitHubUser(gitHubUserMockK) }
        verify(exactly = 1) { contributorRepository.save(any()) }
    }

    @Test
    fun `should update contributor by github user without contacts`() {
        // GIVEN
        val gitHubUserMockK = mockk<GitHubUser>()
        val existContributor = Contributor(name = "Contributor")
        val updatedContributor = mockk<Contributor>()
        every { updatedContributor.repositoryName } returns "brn"
        val capturedContributor = slot<Contributor>()
        every { contributorRepository.findByGitHubUser(gitHubUserMockK) } returns existContributor
        every { gitHubUserMockK.name } returns "name"
        every { gitHubUserMockK.company } returns "company"
        every { gitHubUserMockK.avatarUrl } returns "avatarUrl"
        every { gitHubUserMockK.bio } returns "bio"
        every { gitHubUserMockK.email } returns null
        every { contributorRepository.save(capture(capturedContributor)) } returns updatedContributor
        every { gitHubUserMockK.contributions } returns 1

        // WHEN
        val resultContributor = contributorService.createOrUpdateByGitHubUser(gitHubUserMockK, githubRepositoryName)
        // THEN
        resultContributor shouldBe updatedContributor
        resultContributor.repositoryName shouldBe githubRepositoryName
        capturedContributor.captured.contacts.size shouldBe 0
        verify(exactly = 1) { contributorRepository.findByGitHubUser(gitHubUserMockK) }
        verify(exactly = 1) { contributorRepository.save(existContributor) }
    }

    @Test
    fun `should update contributor by github user with empty repository name`() {
        // GIVEN
        val existContributor = Contributor(name = "Contributor", repositoryName = "")
        val gitHubUserMockK = mockk<GitHubUser>()
        every { contributorRepository.findByGitHubUser(gitHubUserMockK) } returns existContributor
        every { contributorRepository.save(existContributor) } returns existContributor

        every { gitHubUserMockK.name } returns "name"
        every { gitHubUserMockK.company } returns "company"
        every { gitHubUserMockK.avatarUrl } returns "avatarUrl"
        every { gitHubUserMockK.bio } returns "bio"
        every { gitHubUserMockK.email } returns "mail"
        every { gitHubUserMockK.contributions } returns 1
        // WHEN
        val resultContributor = contributorService.createOrUpdateByGitHubUser(gitHubUserMockK, githubRepositoryName)
        // THEN
        resultContributor.shouldNotBeNull()
        resultContributor shouldBe existContributor
        resultContributor.repositoryName shouldBe githubRepositoryName
        verify(exactly = 1) { contributorRepository.findByGitHubUser(gitHubUserMockK) }
        verify(exactly = 1) { contributorRepository.save(existContributor) }
    }

    @Test
    fun `should update contributor by github user on the same values`() {
        // GIVEN
        val gitHunUserMockK = mockk<GitHubUser>()
        val existContributor = Contributor(name = "Contributor")
        existContributor.contribution = 0
        existContributor.name = "name"
        existContributor.company = "company"
        existContributor.description = "description"
        existContributor.pictureUrl = "pictureUrl"
        existContributor.contacts = mutableSetOf(Contact(value = "email"))

        val capturedContributor = slot<Contributor>()
        val updatedContributor = mockk<Contributor>()
        every { updatedContributor.repositoryName } returns githubRepositoryName
        every { contributorRepository.findByGitHubUser(gitHunUserMockK) } returns existContributor
        every { gitHunUserMockK.name } returns "new name"
        every { gitHunUserMockK.company } returns "new company"
        every { gitHunUserMockK.avatarUrl } returns "new avatarUrl"
        every { gitHunUserMockK.bio } returns "new bio"
        every { gitHunUserMockK.email } returns "new mail"
        every { contributorRepository.save(capture(capturedContributor)) } returns updatedContributor
        every { gitHunUserMockK.contributions } returns 1
        // WHEN
        val resultContributor = contributorService.createOrUpdateByGitHubUser(gitHunUserMockK, githubRepositoryName)
        // THEN
        resultContributor shouldBe updatedContributor
        resultContributor.repositoryName shouldBe githubRepositoryName
        capturedContributor.captured shouldBe existContributor
        capturedContributor.captured.contacts.size shouldBe 1
        verify(exactly = 1) { contributorRepository.findByGitHubUser(gitHunUserMockK) }
        verify(exactly = 1) { contributorRepository.save(existContributor) }
    }

    private fun createContributorRequest(
        name: String,
        contribution: Long,
        contacts: Set<ContactRequest>,
    ): ContributorRequest = ContributorRequest(
        name = name,
        nameEn = name,
        type = ContributorType.SPECIALIST,
        contribution = contribution,
        contacts = contacts,
        company = null,
        companyEn = null,
        description = null,
        descriptionEn = null,
        pictureUrl = null,
        active = true,
    )

    private fun createContributor(
        id: Long?,
        name: String,
        contribution: Long,
        contacts: MutableSet<Contact> = mutableSetOf(),
    ): Contributor = Contributor(
        id = id,
        name = name,
        type = ContributorType.SPECIALIST,
        contribution = contribution,
    ).apply { this.contacts = contacts }
}
