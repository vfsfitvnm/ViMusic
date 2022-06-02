package it.vfsfitvnm.vimusic.utils

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline


fun Player.forcePlay(mediaItem: MediaItem) {
    setMediaItem(mediaItem, true)
    playWhenReady = true
    prepare()
}

fun Player.forcePlayAtIndex(mediaItems: List<MediaItem>, mediaItemIndex: Int) {
    if (mediaItems.isEmpty()) return

    setMediaItems(mediaItems, true)
    playWhenReady = true
    seekToDefaultPosition(mediaItemIndex)
    prepare()
}

fun Player.forcePlayFromBeginning(mediaItems: List<MediaItem>) =
    forcePlayAtIndex(mediaItems, 0)

val Player.lastMediaItem: MediaItem?
    get() = mediaItemCount.takeIf { it > 0 }?.let { it - 1 }?.let(::getMediaItemAt)

val Timeline.mediaItems: List<MediaItem>
    get() = (0 until windowCount).map { index ->
        getWindow(index, Timeline.Window()).mediaItem
    }

fun Player.addNext(mediaItem: MediaItem) {
    addMediaItem(currentMediaItemIndex + 1, mediaItem)
}

fun Player.enqueue(mediaItem: MediaItem) {
    addMediaItem(mediaItemCount, mediaItem)
}

fun Player.enqueue(mediaItems: List<MediaItem>) {
    addMediaItems(mediaItemCount, mediaItems)
}