<div align="center">
    <img src="./app/src/main/ic_launcher-playstore.png" width="128" height="128" style="display: block; margin: 0 auto"/>
    <h1>ViMusic</h1>
    <p>A Jetpack Compose Android application for streaming music from YouTube Music</p>

[![Downloads](https://img.shields.io/badge/-Jetpack%20Compose-3a83f9?style=for-the-badge&logo=jetpackcompose&logoColor=white&labelColor=2ec781)](https://developer.android.com/jetpack/compose)
[![Downloads](https://img.shields.io/github/downloads/vfsfitvnm/ViMusic/total?style=for-the-badge&labelColor=002b41)](https://github.com/vfsfitvnm/ViMusic/releases)
</div>

---

<p>
  <img src="https://user-images.githubusercontent.com/46219656/171632003-33a017d7-cdc8-4588-a1fc-45be294969a8.png" width="200" />
  <img src="https://user-images.githubusercontent.com/46219656/171632005-f51cce3f-20d4-44e6-83dd-c1d27e9c63e0.png" width="200" />
  <img src="https://user-images.githubusercontent.com/46219656/171632000-feadb479-41cf-43ba-8c8a-11aed303ad69.png" width="200" />
  <img src="https://user-images.githubusercontent.com/46219656/171632008-e66392c7-9462-4383-b7cf-8ec5f5701f9c.png" width="200" />
</p>

## Features
- Play any non-age-restricted song/video from YouTube Music
- Background playback
- Cache audio chunks for offline playback
- Search for songs, albums, artists and videos
- Display songs lyrics
- Local playlist management
- Reorder songs in playlist or queue
- Light/Dark theme
- ...

## TODO
- **Improve UI/UX** (help needed)
- Settings page
- Support YouTube playlists (and other stuff to improve features parity)
- Download songs (not sure about this)
- Play local songs (not sure about this, too)
- Translation

## Installation
[<img src="https://github.com/machiav3lli/oandbackupx/blob/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png" alt="Get it on GitHub" height="80">](https://github.com/vfsfitvnm/ViMusic/releases/latest)

After installing, I recommend executing the following ADB command to neutralize some animation lags you may experience in cold starts:
```
adb shell cmd package compile -r bg-dexopt it.vfsfitvnm.vimusic
```

## Known problems
The application is using `androidx.media3`, which is unstable. Expect random crashes or buggy notification behaviours. Hopefully, they will be fixed within the next alpha release.

## Contributions
There's a huge room for improvements! Please open an issue to report bugs, discuss ideas and so on.

## Similar projects, inspirations and acknowledgments
- [**Beatbump**](https://github.com/snuffyDev/Beatbump): Alternative YouTube Music frontend built with Svelte/SvelteKit.
- [**music**](https://github.com/z-huang/music): A material design music player with music from YouTube/YouTube Music.
- [**YouTube-Internal-Clients**](https://github.com/zerodytrash/YouTube-Internal-Clients): A python script that discovers hidden YouTube API clients. Just a research project.
- [**ionicons**](https://github.com/ionic-team/ionicons): Premium hand-crafted icons built by Ionic, for Ionic apps and web apps everywhere.
