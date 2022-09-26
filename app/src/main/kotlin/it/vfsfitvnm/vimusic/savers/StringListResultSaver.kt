package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.autoSaver

val StringListResultSaver = ResultSaver.of(autoSaver<List<String>?>())
