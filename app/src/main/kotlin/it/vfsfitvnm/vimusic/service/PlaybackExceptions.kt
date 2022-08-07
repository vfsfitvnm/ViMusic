package it.vfsfitvnm.vimusic.service

import androidx.media3.common.PlaybackException

class PlayableFormatNotFoundException : PlaybackException(null, null, ERROR_CODE_REMOTE_ERROR)

class UnplayableException : PlaybackException(null, null, ERROR_CODE_REMOTE_ERROR)

class LoginRequiredException : PlaybackException(null, null, ERROR_CODE_REMOTE_ERROR)
