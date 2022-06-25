package it.vfsfitvnm.vimusic.utils

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline


val Timeline.mediaItems: List<MediaItem>
    get() = (0 until windowCount).map { index ->
        getWindow(index, Timeline.Window()).mediaItem
    }


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

fun Player.addNext(mediaItem: MediaItem) {
    if (playbackState == Player.STATE_IDLE) {
        forcePlay(mediaItem)
    } else {
        addMediaItem(currentMediaItemIndex + 1, mediaItem)
    }
}

fun Player.enqueue(mediaItem: MediaItem) {
    if (playbackState == Player.STATE_IDLE) {
        forcePlay(mediaItem)
    } else {
        addMediaItem(mediaItemCount, mediaItem)
    }
}

fun Player.enqueue(mediaItems: List<MediaItem>) {
    if (playbackState == Player.STATE_IDLE) {
        forcePlayFromBeginning(mediaItems)
    } else {
        addMediaItems(mediaItemCount, mediaItems)
    }
}