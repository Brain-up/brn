package com.epam.brn.service.impl

import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.model.Headphones
import com.epam.brn.repo.HeadphonesRepository
import com.epam.brn.service.HeadphonesService
import org.springframework.stereotype.Service

@Service
class HeadphonesServiceImpl(private var headphonesRepository: HeadphonesRepository) : HeadphonesService {
    override fun save(headphones: Headphones): HeadphonesDto {
        return headphonesRepository.save(headphones).toDto()
    }

    override fun getAllHeadphonesForUser(userId: Long): Set<HeadphonesDto> {
        return headphonesRepository.getHeadphonesForUser(userId).map { it.toDto() }.toSet()
    }
}
