package com.epam.brn.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator

@Entity
data class Authority(
    @Id
    @GeneratedValue(generator = "authority_id_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(
        name = "authority_id_seq",
        sequenceName = "authority_id_seq",
        allocationSize = 50
    )
    var id: Long? = null,
    @Column(unique = true, nullable = false, name = "AUTHORITY_NAME")
    val authorityName: String

) {
    override fun toString(): String {
        return "Authority(id=$id, authority='$authorityName')"
    }
}
