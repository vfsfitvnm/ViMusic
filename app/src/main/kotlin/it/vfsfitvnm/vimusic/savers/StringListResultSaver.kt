package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.autoSaver

val StringListResultSaver = resultSaver(autoSaver<List<String>?>())
