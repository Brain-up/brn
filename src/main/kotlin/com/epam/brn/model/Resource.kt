package com.epam.brn.model

import com.epam.brn.constant.WordTypeEnum
import com.epam.brn.dto.ResourceDto
import javax.persistence.Entity
import javax.persistence.Index
import javax.persistence.Table
import javax.persistence.UniqueConstraint
import javax.persistence.Id
import javax.persistence.GeneratedValue
import javax.persistence.Column
import javax.persistence.ManyToMany
import javax.persistence.GenerationType

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(columnNames = ["word", "audioFileUrl"])],
    indexes = [
        Index(name = "word_audio_file_idx", columnList = "word, audioFileUrl"),
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
    var word: String? = "",
    var wordType: String = "",
    var pictureFileUrl: String? = "",
    var soundsCount: Int? = 0,
    @ManyToMany(mappedBy = "answerOptions")
    var tasks: MutableSet<Task> = HashSet()
) {
    fun toDto() = ResourceDto(
        id = id,
        audioFileUrl = audioFileUrl,
        word = word,
        pictureFileUrl = pictureFileUrl,
        soundsCount = soundsCount,
        wordType = WordTypeEnum.valueOf(wordType)
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Resource

        if (id != other.id) return false
        if (audioFileUrl != other.audioFileUrl) return false
        if (word != other.word) return false
        if (pictureFileUrl != other.pictureFileUrl) return false
        if (soundsCount != other.soundsCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + audioFileUrl.hashCode()
        result = 31 * result + word.hashCode()
        result = 31 * result + pictureFileUrl.hashCode()
        result = 31 * result + soundsCount.hashCode()
        return result
    }

    override fun toString() = "Resource(id=$id, audioFileUrl='$audioFileUrl', word='$word', pictureFileUrl='$pictureFileUrl', soundsCount=$soundsCount)"
}