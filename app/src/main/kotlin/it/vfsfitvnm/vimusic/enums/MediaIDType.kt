package it.vfsfitvnm.vimusic.enums

enum class MediaIDType {
    Playlist,
    RandomFavorites,
    RandomSongs,
    Song;

    val prefix: String
        get() = when (this) {
            Song -> "VIMUSIC_SONG_ID_"
            Playlist -> "VIMUSIC_PLAYLIST_ID_"
            RandomSongs -> "VIMUSIC_RANDOM_SONGS"
            RandomFavorites -> "VIMUSIC_RANDOM_FAVORITES"
        }
}