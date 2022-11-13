package com.epam.brn.utils

import com.epam.brn.model.UserAccount

fun UserAccount.copy() = UserAccount(
    id = this.id,
    fullName = this.fullName,
    email = this.email,
    gender = this.gender,
    bornYear = this.bornYear,
    changed = this.changed,
    avatar = this.avatar,
    photo = this.photo,
    description = this.description
)