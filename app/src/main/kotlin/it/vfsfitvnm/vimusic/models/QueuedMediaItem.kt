package it.vfsfitvnm.vimusic.models

import androidx.compose.runtime.Immutable
import androidx.media3.common.MediaItem
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity
class QueuedMediaItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val mediaItem: MediaItem,
    var position: Long?
)
