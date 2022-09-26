package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.autoSaver

val StringResultSaver = ResultSaver.of(autoSaver<String?>())
