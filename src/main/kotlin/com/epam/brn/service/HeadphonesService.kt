package com.epam.brn.service

import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.model.Headphones

interface HeadphonesService {
    fun save(headphones: Headphones): HeadphonesDto
    fun getAllHeadphonesForUser(userId: Long): Set<HeadphonesDto>
}
