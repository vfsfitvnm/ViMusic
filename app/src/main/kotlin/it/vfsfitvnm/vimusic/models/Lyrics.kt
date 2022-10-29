package it.vfsfitvnm.vimusic.models

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Immutable
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
class Lyrics(
    @PrimaryKey val songId: String,
    val fixed: String?,
    val synced: String?,
)
