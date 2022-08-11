<div align="center">
    <img src="./app/src/main/ic_launcher-playstore.png" width="128" height="128" style="display: block; margin: 0 auto"/>
    <h1>ViMusic</h1>
    <p>A Jetpack Compose Android application for streaming music from YouTube Music</p>

[![Downloads](https://img.shields.io/badge/-Jetpack%20Compose-3a83f9?style=for-the-badge&logo=jetpackcompose&logoColor=white&labelColor=2ec781)](https://developer.android.com/jetpack/compose)
</div>

---

<p align="center">
  <img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/1.jpg" width="30%" />
  <img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/2.jpg" width="30%" />
  <img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/3.jpg" width="30%" />

  <img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/4.jpg" width="30%" />
  <img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/5.jpg" width="30%" />
  <img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/6.jpg" width="30%" />
</p>

## Features
- Play (almost) any song or video from YouTube Music
- Background playback
- Cache audio chunks for offline playback
- Search for songs, albums, artists videos and playlists
- Fetch, display and edit songs lyrics or synchronized lyrics
- Local playlist management
- Reorder songs in playlist or queue
- Light/Dark/Dynamic theme
- Skip silence
- Sleep timer
- Audio normalization
- Persistent queue
- Open YouTube/YouTube Music links (`watch`, `playlist`)
- ...

## Installation

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/it.vfsfitvnm.vimusic/)
[<img src="https://github.com/machiav3lli/oandbackupx/blob/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png"
      alt="Get it on GitHub"
      height="80">](https://github.com/vfsfitvnm/ViMusic/releases/latest)

After installing, I recommend executing the following ADB command to neutralize some animation lags you may experience in cold starts:
```
adb shell cmd package compile -r bg-dexopt it.vfsfitvnm.vimusic
```

## Similar projects, inspirations and acknowledgments
- [**Beatbump**](https://github.com/snuffyDev/Beatbump): Alternative YouTube Music frontend built with Svelte/SvelteKit.
- [**music**](https://github.com/z-huang/music): A material design music player with music from YouTube/YouTube Music.
- [**YouTube-Internal-Clients**](https://github.com/zerodytrash/YouTube-Internal-Clients): A python script that discovers hidden YouTube API clients. Just a research project.
- [**ionicons**](https://github.com/ionic-team/ionicons): Premium hand-crafted icons built by Ionic, for Ionic apps and web apps everywhere.
