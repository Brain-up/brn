package com.epam.brn.model

import com.epam.brn.dto.ResourceDto
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.ManyToMany
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    var audioFileUrl: String? = "",
    @Column(nullable = false)
    var word: String = "",
    var wordType: String = "",
    var locale: String = "",
    var pictureFileUrl: String? = "",
    var soundsCount: Int? = 0,
    @ManyToMany(mappedBy = "answerOptions", cascade = [CascadeType.MERGE])
    var tasks: MutableSet<Task> = HashSet(),
    var description: String? = ""
) {
    fun toDto() = ResourceDto(
        id = id,
        audioFileUrl = audioFileUrl,
        word = word,
        pictureFileUrl = pictureFileUrl,
        soundsCount = soundsCount,
        wordType = WordType.valueOf(wordType),
        description = description
    )

    override fun toString() = "Resource(id=$id, audioFileUrl='$audioFileUrl', word='$word'," +
            " pictureFileUrl='$pictureFileUrl', soundsCount=$soundsCount), description='$description'"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Resource

        if (id != other.id) return false
        if (audioFileUrl != other.audioFileUrl) return false
        if (word != other.word) return false
        if (wordType != other.wordType) return false
        if (pictureFileUrl != other.pictureFileUrl) return false
        if (soundsCount != other.soundsCount) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (audioFileUrl?.hashCode() ?: 0)
        result = 31 * result + (word.hashCode())
        result = 31 * result + wordType.hashCode()
        result = 31 * result + (pictureFileUrl?.hashCode() ?: 0)
        result = 31 * result + (soundsCount ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }
}
