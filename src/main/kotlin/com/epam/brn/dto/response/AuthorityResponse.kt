package com.epam.brn.dto.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class AuthorityResponse(
    val authorityName: String
)
