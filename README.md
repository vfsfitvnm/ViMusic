<div align="center">
    <img src="./app/src/main/ic_launcher-playstore.png" width="128" height="128" style="display: block; margin: 0 auto"/>
    <h1>ViMusic</h1>
    <p>A Jetpack Compose Android application for streaming music from YouTube Music</p>

[![Downloads](https://img.shields.io/badge/-Jetpack%20Compose-3a83f9?style=for-the-badge&logo=jetpackcompose&logoColor=white&labelColor=2ec781)](https://developer.android.com/jetpack/compose)
[![Downloads](https://img.shields.io/github/downloads/vfsfitvnm/ViMusic/total?style=for-the-badge&labelColor=002b41)](https://github.com/vfsfitvnm/ViMusic/releases)
</div>

---

<p>
  <img src="https://user-images.githubusercontent.com/46219656/174445416-4fdc05de-1280-41be-a4e0-d40724606f4a.png" width="200" />
  <img src="https://user-images.githubusercontent.com/46219656/174445418-357c84b1-2db1-4add-9709-cd4a5ecb2215.png" width="200" />
  <img src="https://user-images.githubusercontent.com/46219656/174445421-f5697ec8-adee-4c4d-9aa8-b4336b9d7c86.png" width="200" />
  <img src="https://user-images.githubusercontent.com/46219656/174445419-59a48e05-d1da-4a58-b1cf-b0a3331c1f15.png" width="200" />
  <img src="https://user-images.githubusercontent.com/46219656/174445415-a1f16f1b-362e-4fa3-bc8e-a58d53b26c88.png" width="200" />
  <img src="https://user-images.githubusercontent.com/46219656/174445412-f8b1d15c-908e-490d-8702-0e59cf64772e.png" width="200" />
</p>

## Features
- Play any non-age-restricted song/video from YouTube Music
- Background playback
- Cache audio chunks for offline playback
- Search for songs, albums, artists videos and playlists
- Display and edit songs lyrics
- Local playlist management
- Reorder songs in playlist or queue
- Light/Dark theme
- Skip silence
- Sleep timer
- Audio normalization
- Open YouTube/YouTube Music links (`watch`, `playlist`)
- ...

## Installation
[<img src="https://github.com/machiav3lli/oandbackupx/blob/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png" alt="Get it on GitHub" height="80">](https://github.com/vfsfitvnm/ViMusic/releases/latest)

After installing, I recommend executing the following ADB command to neutralize some animation lags you may experience in cold starts:
```
adb shell cmd package compile -r bg-dexopt it.vfsfitvnm.vimusic
```

## Similar projects, inspirations and acknowledgments
- [**Beatbump**](https://github.com/snuffyDev/Beatbump): Alternative YouTube Music frontend built with Svelte/SvelteKit.
- [**music**](https://github.com/z-huang/music): A material design music player with music from YouTube/YouTube Music.
- [**YouTube-Internal-Clients**](https://github.com/zerodytrash/YouTube-Internal-Clients): A python script that discovers hidden YouTube API clients. Just a research project.
- [**ionicons**](https://github.com/ionic-team/ionicons): Premium hand-crafted icons built by Ionic, for Ionic apps and web apps everywhere.
