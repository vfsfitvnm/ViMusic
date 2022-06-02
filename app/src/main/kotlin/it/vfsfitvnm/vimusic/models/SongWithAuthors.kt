package it.vfsfitvnm.vimusic.models

import androidx.compose.runtime.Immutable
import androidx.room.*


@Immutable
@Entity(
    primaryKeys = ["songId", "authorInfoId"],
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Info::class,
            parentColumns = ["id"],
            childColumns = ["authorInfoId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SongWithAuthors(
    val songId: String,
    @ColumnInfo(index = true) val authorInfoId: Long
)
