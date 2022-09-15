package it.vfsfitvnm.vimusic

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.os.Parcel
import androidx.core.database.getFloatOrNull
import androidx.media3.common.MediaItem
import androidx.room.AutoMigration
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.DeleteTable
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RenameColumn
import androidx.room.RenameTable
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import androidx.room.Upsert
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteDatabase
import it.vfsfitvnm.vimusic.enums.PlaylistSortBy
import it.vfsfitvnm.vimusic.enums.SongSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.models.Album
import it.vfsfitvnm.vimusic.models.Artist
import it.vfsfitvnm.vimusic.models.DetailedSong
import it.vfsfitvnm.vimusic.models.DetailedSongWithContentLength
import it.vfsfitvnm.vimusic.models.Format
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.models.PlaylistPreview
import it.vfsfitvnm.vimusic.models.PlaylistWithSongs
import it.vfsfitvnm.vimusic.models.QueuedMediaItem
import it.vfsfitvnm.vimusic.models.SearchQuery
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.models.SongAlbumMap
import it.vfsfitvnm.vimusic.models.SongArtistMap
import it.vfsfitvnm.vimusic.models.SongPlaylistMap
import it.vfsfitvnm.vimusic.models.SortedSongPlaylistMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface Database {
    companion object : Database by DatabaseInitializer.Instance.database

    @Transaction
    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0 ORDER BY ROWID ASC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByRowIdAsc(): Flow<List<DetailedSong>>

    @Transaction
    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0 ORDER BY ROWID DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByRowIdDesc(): Flow<List<DetailedSong>>

    @Transaction
    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0 ORDER BY title ASC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByTitleAsc(): Flow<List<DetailedSong>>

    @Transaction
    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0 ORDER BY title DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByTitleDesc(): Flow<List<DetailedSong>>

    @Transaction
    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0 ORDER BY totalPlayTimeMs ASC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByPlayTimeAsc(): Flow<List<DetailedSong>>

    @Transaction
    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0 ORDER BY totalPlayTimeMs DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByPlayTimeDesc(): Flow<List<DetailedSong>>

    fun songs(sortBy: SongSortBy, sortOrder: SortOrder): Flow<List<DetailedSong>> {
        return when (sortBy) {
            SongSortBy.PlayTime -> when (sortOrder) {
                SortOrder.Ascending -> songsByPlayTimeAsc()
                SortOrder.Descending -> songsByPlayTimeDesc()
            }
            SongSortBy.Title -> when (sortOrder) {
                SortOrder.Ascending -> songsByTitleAsc()
                SortOrder.Descending -> songsByTitleDesc()
            }
            SongSortBy.DateAdded -> when (sortOrder) {
                SortOrder.Ascending -> songsByRowIdAsc()
                SortOrder.Descending -> songsByRowIdDesc()
            }
        }
    }

    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL ORDER BY likedAt DESC")
    @RewriteQueriesToDropUnusedColumns
    fun favorites(): Flow<List<DetailedSong>>

    @Query("SELECT * FROM QueuedMediaItem")
    fun queue(): List<QueuedMediaItem>

    @Query("DELETE FROM QueuedMediaItem")
    fun clearQueue()

    @Query("SELECT * FROM SearchQuery WHERE query LIKE :query ORDER BY id DESC")
    fun queries(query: String): Flow<List<SearchQuery>>

    @Query("SELECT * FROM Song WHERE id = :id")
    fun song(id: String): Flow<Song?>

    @Query("SELECT likedAt FROM Song WHERE id = :songId")
    fun likedAt(songId: String): Flow<Long?>

    @Query("UPDATE Song SET likedAt = :likedAt WHERE id = :songId")
    fun like(songId: String, likedAt: Long?): Int

    @Query("SELECT lyrics FROM Song WHERE id = :songId")
    fun lyrics(songId: String): Flow<String?>

    @Query("SELECT synchronizedLyrics FROM Song WHERE id = :songId")
    fun synchronizedLyrics(songId: String): Flow<String?>

    @Query("UPDATE Song SET lyrics = :lyrics WHERE id = :songId")
    fun updateLyrics(songId: String, lyrics: String?): Int

    @Query("UPDATE Song SET synchronizedLyrics = :lyrics WHERE id = :songId")
    fun updateSynchronizedLyrics(songId: String, lyrics: String?): Int

    @Query("SELECT * FROM Artist WHERE id = :id")
    fun artist(id: String): Flow<Artist?>

    @Query("SELECT * FROM Album WHERE id = :id")
    fun album(id: String): Flow<Album?>

    @Query("UPDATE Song SET totalPlayTimeMs = totalPlayTimeMs + :addition WHERE id = :id")
    fun incrementTotalPlayTimeMs(id: String, addition: Long)

    @Transaction
    @Query("SELECT * FROM Playlist WHERE id = :id")
    fun playlistWithSongs(id: Long): Flow<PlaylistWithSongs?>

    @Transaction
    @Query("SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY name ASC")
    fun playlistPreviewsByName(): Flow<List<PlaylistPreview>>

    @Transaction
    @Query("SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY ROWID ASC")
    fun playlistPreviewsByDateAdded(): Flow<List<PlaylistPreview>>

    @Transaction
    @Query("SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY songCount ASC")
    fun playlistPreviewsByDateSongCount(): Flow<List<PlaylistPreview>>

    fun playlistPreviews(
        sortBy: PlaylistSortBy,
        sortOrder: SortOrder
    ): Flow<List<PlaylistPreview>> {
        return when (sortBy) {
            PlaylistSortBy.Name -> playlistPreviewsByName()
            PlaylistSortBy.DateAdded -> playlistPreviewsByDateAdded()
            PlaylistSortBy.SongCount -> playlistPreviewsByDateSongCount()
        }.map {
            when (sortOrder) {
                SortOrder.Ascending -> it
                SortOrder.Descending -> it.reversed()
            }
        }
    }

    @Query("SELECT thumbnailUrl FROM Song JOIN SongPlaylistMap ON id = songId WHERE playlistId = :id ORDER BY position LIMIT 4")
    fun playlistThumbnailUrls(id: Long): Flow<List<String?>>

    @Transaction
    @Query("SELECT * FROM Song JOIN SongArtistMap ON Song.id = SongArtistMap.songId WHERE SongArtistMap.artistId = :artistId AND totalPlayTimeMs > 0 ORDER BY Song.ROWID DESC")
    @RewriteQueriesToDropUnusedColumns
    fun artistSongs(artistId: String): Flow<List<DetailedSong>>

    @Transaction
    @Query("SELECT * FROM Song JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId WHERE SongAlbumMap.albumId = :albumId AND position IS NOT NULL ORDER BY position")
    @RewriteQueriesToDropUnusedColumns
    fun albumSongs(albumId: String): Flow<List<DetailedSong>>

    @Query("SELECT * FROM Format WHERE songId = :songId")
    fun format(songId: String): Flow<Format>

    @Transaction
    @Query("SELECT * FROM Song JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0 ORDER BY Song.ROWID DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsWithContentLength(): Flow<List<DetailedSongWithContentLength>>

    @Query("""
        UPDATE SongPlaylistMap SET position = 
          CASE 
            WHEN position < :fromPosition THEN position + 1
            WHEN position > :fromPosition THEN position - 1
            ELSE :toPosition
          END 
        WHERE playlistId = :playlistId AND position BETWEEN MIN(:fromPosition,:toPosition) and MAX(:fromPosition,:toPosition)
    """)
    fun move(playlistId: Long, fromPosition: Int, toPosition: Int)

    @Query("DELETE FROM SongPlaylistMap WHERE playlistId = :id")
    fun clearPlaylist(id: Long)

    @Query("SELECT loudnessDb FROM Format WHERE songId = :songId")
    fun loudnessDb(songId: String): Flow<Float?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(format: Format)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(searchQuery: SearchQuery)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(info: Artist): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(info: Album): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(playlist: Playlist): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(songPlaylistMap: SongPlaylistMap): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(songAlbumMap: SongAlbumMap): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(songArtistMap: SongArtistMap): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(song: Song): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(queuedMediaItems: List<QueuedMediaItem>)

    @Transaction
    fun insert(mediaItem: MediaItem, block: (Song) -> Song = { it }) {
        val song = Song(
            id = mediaItem.mediaId,
            title = mediaItem.mediaMetadata.title!!.toString(),
            artistsText = mediaItem.mediaMetadata.artist?.toString(),
            durationText = mediaItem.mediaMetadata.extras?.getString("durationText")!!,
            thumbnailUrl = mediaItem.mediaMetadata.artworkUri?.toString()
        ).let(block).also { song ->
            if (insert(song) == -1L) return
        }

        mediaItem.mediaMetadata.extras?.getString("albumId")?.let { albumId ->
            Album(
                id = albumId,
                title = mediaItem.mediaMetadata.albumTitle?.toString(),
                year = null,
                authorsText = null,
                thumbnailUrl = null,
                shareUrl = null,
                timestamp = null,
            ).also(::insert)

            upsert(
                SongAlbumMap(
                    songId = song.id,
                    albumId = albumId,
                    position = null
                )
            )
        }

        mediaItem.mediaMetadata.extras?.getStringArrayList("artistNames")?.let { artistNames ->
            mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds")?.let { artistIds ->
                artistNames.mapIndexed { index, artistName ->
                    Artist(
                        id = artistIds[index],
                        name = artistName,
                        thumbnailUrl = null,
                        info = null,
                        timestamp = null,
                    ).also(::insert)
                }
            }
        }?.forEach { artist ->
            insert(
                SongArtistMap(
                    songId = song.id,
                    artistId = artist.id
                )
            )
        }
    }

    @Update
    fun update(song: Song)

    @Update
    fun update(artist: Artist)

    @Update
    fun update(album: Album)

    @Update
    fun update(songAlbumMap: SongAlbumMap)

    @Update
    fun update(songPlaylistMap: SongPlaylistMap)

    @Update
    fun update(playlist: Playlist)

    @Upsert
    fun upsert(album: Album, songAlbumMaps: List<SongAlbumMap>)

    @Upsert
    fun upsert(songAlbumMap: SongAlbumMap)

    @Upsert
    fun upsert(artist: Artist)

    @Delete
    fun delete(searchQuery: SearchQuery)

    @Delete
    fun delete(playlist: Playlist)

    @Delete
    fun delete(playlist: Album)

    @Delete
    fun delete(songPlaylistMap: SongPlaylistMap)
}

@androidx.room.Database(
    entities = [
        Song::class,
        SongPlaylistMap::class,
        Playlist::class,
        Artist::class,
        SongArtistMap::class,
        Album::class,
        SongAlbumMap::class,
        SearchQuery::class,
        QueuedMediaItem::class,
        Format::class,
    ],
    views = [
        SortedSongPlaylistMap::class
    ],
    version = 17,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4, spec = DatabaseInitializer.From3To4Migration::class),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8, spec = DatabaseInitializer.From7To8Migration::class),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 11, to = 12, spec = DatabaseInitializer.From11To12Migration::class),
        AutoMigration(from = 12, to = 13),
        AutoMigration(from = 13, to = 14),
        AutoMigration(from = 15, to = 16),
        AutoMigration(from = 16, to = 17),
    ],
)
@TypeConverters(Converters::class)
abstract class DatabaseInitializer protected constructor() : RoomDatabase() {
    abstract val database: Database

    companion object {
        lateinit var Instance: DatabaseInitializer

        context(Context)
                operator fun invoke() {
            if (!::Instance.isInitialized) {
                Instance = Room
                    .databaseBuilder(this@Context, DatabaseInitializer::class.java, "data.db")
                    .addMigrations(
                        From8To9Migration(),
                        From10To11Migration(),
                        From14To15Migration()
                    )
                    .build()
            }
        }
    }

    @DeleteTable.Entries(DeleteTable(tableName = "QueuedMediaItem"))
    class From3To4Migration : AutoMigrationSpec

    @RenameColumn.Entries(RenameColumn("Song", "albumInfoId", "albumId"))
    class From7To8Migration : AutoMigrationSpec

    class From8To9Migration : Migration(8, 9) {
        override fun migrate(it: SupportSQLiteDatabase) {
            it.query(SimpleSQLiteQuery("SELECT DISTINCT browseId, text, Info.id FROM Info JOIN Song ON Info.id = Song.albumId;"))
                .use { cursor ->
                    val albumValues = ContentValues(2)
                    while (cursor.moveToNext()) {
                        albumValues.put("id", cursor.getString(0))
                        albumValues.put("title", cursor.getString(1))
                        it.insert("Album", CONFLICT_IGNORE, albumValues)

                        it.execSQL(
                            "UPDATE Song SET albumId = '${cursor.getString(0)}' WHERE albumId = ${
                                cursor.getLong(
                                    2
                                )
                            }"
                        )
                    }
                }

            it.query(SimpleSQLiteQuery("SELECT GROUP_CONCAT(text, ''), SongWithAuthors.songId FROM Info JOIN SongWithAuthors ON Info.id = SongWithAuthors.authorInfoId GROUP BY songId;"))
                .use { cursor ->
                    val songValues = ContentValues(1)
                    while (cursor.moveToNext()) {
                        songValues.put("artistsText", cursor.getString(0))
                        it.update(
                            "Song",
                            CONFLICT_IGNORE,
                            songValues,
                            "id = ?",
                            arrayOf(cursor.getString(1))
                        )
                    }
                }

            it.query(SimpleSQLiteQuery("SELECT browseId, text, Info.id FROM Info JOIN SongWithAuthors ON Info.id = SongWithAuthors.authorInfoId WHERE browseId NOT NULL;"))
                .use { cursor ->
                    val artistValues = ContentValues(2)
                    while (cursor.moveToNext()) {
                        artistValues.put("id", cursor.getString(0))
                        artistValues.put("name", cursor.getString(1))
                        it.insert("Artist", CONFLICT_IGNORE, artistValues)

                        it.execSQL(
                            "UPDATE SongWithAuthors SET authorInfoId = '${cursor.getString(0)}' WHERE authorInfoId = ${
                                cursor.getLong(
                                    2
                                )
                            }"
                        )
                    }
                }

            it.execSQL("INSERT INTO SongArtistMap(songId, artistId) SELECT songId, authorInfoId FROM SongWithAuthors")

            it.execSQL("DROP TABLE Info;")
            it.execSQL("DROP TABLE SongWithAuthors;")
        }
    }

    class From10To11Migration : Migration(10, 11) {
        override fun migrate(it: SupportSQLiteDatabase) {
            it.query(SimpleSQLiteQuery("SELECT id, albumId FROM Song;")).use { cursor ->
                val songAlbumMapValues = ContentValues(2)
                while (cursor.moveToNext()) {
                    songAlbumMapValues.put("songId", cursor.getString(0))
                    songAlbumMapValues.put("albumId", cursor.getString(1))
                    it.insert("SongAlbumMap", CONFLICT_IGNORE, songAlbumMapValues)
                }
            }

            it.execSQL("CREATE TABLE IF NOT EXISTS `Song_new` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `artistsText` TEXT, `durationText` TEXT NOT NULL, `thumbnailUrl` TEXT, `lyrics` TEXT, `likedAt` INTEGER, `totalPlayTimeMs` INTEGER NOT NULL, `loudnessDb` REAL, `contentLength` INTEGER, PRIMARY KEY(`id`))")

            it.execSQL("INSERT INTO Song_new(id, title, artistsText, durationText, thumbnailUrl, lyrics, likedAt, totalPlayTimeMs, loudnessDb, contentLength) SELECT id, title, artistsText, durationText, thumbnailUrl, lyrics, likedAt, totalPlayTimeMs, loudnessDb, contentLength FROM Song;")
            it.execSQL("DROP TABLE Song;")
            it.execSQL("ALTER TABLE Song_new RENAME TO Song;")
        }
    }

    @RenameTable("SongInPlaylist", "SongPlaylistMap")
    @RenameTable("SortedSongInPlaylist", "SortedSongPlaylistMap")
    class From11To12Migration : AutoMigrationSpec

    class From14To15Migration : Migration(14, 15) {
        override fun migrate(it: SupportSQLiteDatabase) {
            it.query(SimpleSQLiteQuery("SELECT id, loudnessDb, contentLength FROM Song;"))
                .use { cursor ->
                    val formatValues = ContentValues(3)
                    while (cursor.moveToNext()) {
                        formatValues.put("songId", cursor.getString(0))
                        formatValues.put("loudnessDb", cursor.getFloatOrNull(1))
                        formatValues.put("contentLength", cursor.getFloatOrNull(2))
                        it.insert("Format", CONFLICT_IGNORE, formatValues)
                    }
                }

            it.execSQL("CREATE TABLE IF NOT EXISTS `Song_new` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `artistsText` TEXT, `durationText` TEXT NOT NULL, `thumbnailUrl` TEXT, `lyrics` TEXT, `likedAt` INTEGER, `totalPlayTimeMs` INTEGER NOT NULL, PRIMARY KEY(`id`))")

            it.execSQL("INSERT INTO Song_new(id, title, artistsText, durationText, thumbnailUrl, lyrics, likedAt, totalPlayTimeMs) SELECT id, title, artistsText, durationText, thumbnailUrl, lyrics, likedAt, totalPlayTimeMs FROM Song;")
            it.execSQL("DROP TABLE Song;")
            it.execSQL("ALTER TABLE Song_new RENAME TO Song;")
        }
    }
}

@TypeConverters
object Converters {
    @TypeConverter
    fun mediaItemFromByteArray(value: ByteArray?): MediaItem? {
        return value?.let { byteArray ->
            runCatching {
                val parcel = Parcel.obtain()
                parcel.unmarshall(byteArray, 0, byteArray.size)
                parcel.setDataPosition(0)
                val bundle = parcel.readBundle(MediaItem::class.java.classLoader)
                parcel.recycle()

                bundle?.let(MediaItem.CREATOR::fromBundle)
            }.getOrNull()
        }
    }

    @TypeConverter
    fun mediaItemToByteArray(mediaItem: MediaItem?): ByteArray? {
        return mediaItem?.toBundle()?.let { persistableBundle ->
            val parcel = Parcel.obtain()
            parcel.writeBundle(persistableBundle)
            val bytes = parcel.marshall()
            parcel.recycle()

            bytes
        }
    }
}

val Database.internal: RoomDatabase
    get() = DatabaseInitializer.Instance

fun query(block: () -> Unit) = DatabaseInitializer.Instance.queryExecutor.execute(block)

fun transaction(block: () -> Unit) = with(DatabaseInitializer.Instance) {
    transactionExecutor.execute {
        runInTransaction(block)
    }
}

val RoomDatabase.path: String?
    get() = openHelper.writableDatabase.path

fun RoomDatabase.checkpoint() {
    openHelper.writableDatabase.run {
        query("PRAGMA journal_mode").use { cursor ->
            if (cursor.moveToFirst()) {
                when (cursor.getString(0).lowercase()) {
                    "wal" -> {
                        query("PRAGMA wal_checkpoint").use(Cursor::moveToFirst)
                        query("PRAGMA wal_checkpoint(TRUNCATE)").use(Cursor::moveToFirst)
                        query("PRAGMA wal_checkpoint").use(Cursor::moveToFirst)
                    }
                }
            }
        }
    }
}
