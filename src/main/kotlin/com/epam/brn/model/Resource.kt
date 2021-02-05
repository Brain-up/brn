package com.epam.brn.model

import com.epam.brn.dto.ResourceDto
import com.epam.brn.enums.WordType
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.ManyToMany
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(columnNames = ["word", "audioFileUrl", "wordType"])],
    indexes = [
        Index(name = "word_audio_file_idx", columnList = "word, audioFileUrl, wordType"),
        Index(name = "audio_file_idx", columnList = "audioFileUrl")
    ]
)
data class Resource(
    @Id
    @GeneratedValue(generator = "resource_id_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(
        name = "resource_id_seq",
        sequenceName = "resource_id_seq",
        allocationSize = 1
    )
    var id: Long? = null,
    @Column(nullable = false)
    var audioFileUrl: String? = "",
    @Column(nullable = false)
    var word: String = "",
    var wordType: String = "",
    var pictureFileUrl: String? = "",
    var soundsCount: Int? = 0,
) {
    @ManyToMany(mappedBy = "answerOptions", cascade = [CascadeType.MERGE])
    var tasks: MutableSet<Task> = HashSet()

    fun toDto() = ResourceDto(
        id = id,
        audioFileUrl = audioFileUrl,
        word = word,
        pictureFileUrl = pictureFileUrl,
        soundsCount = soundsCount,
        wordType = WordType.valueOf(wordType)
    )
}
