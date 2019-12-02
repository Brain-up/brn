package com.epam.brn.model

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ForeignKey
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
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
    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "USERNAME", foreignKey = ForeignKey(name = "fk_authority_user"))
    val userAccount: UserAccount,
    @Column(nullable = false)
    val authority: String
) {
    override fun toString(): String {
        return "Authority(id=$id, authority='$authority')"
    }
}