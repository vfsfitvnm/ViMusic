package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.autoSaver

val StringResultSaver = resultSaver(autoSaver<String?>())
