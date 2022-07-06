package it.vfsfitvnm.vimusic

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.os.Parcel
import androidx.media3.common.MediaItem
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteDatabase
import it.vfsfitvnm.vimusic.enums.SongSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.models.*
import it.vfsfitvnm.vimusic.utils.getFloatOrNull
import it.vfsfitvnm.vimusic.utils.getLongOrNull
import kotlinx.coroutines.flow.Flow


@Dao
interface Database {
    companion object : Database by DatabaseInitializer.Instance.database

    @Transaction
    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0 ORDER BY ROWID ASC")
    fun songsByRowIdAsc(): Flow<List<DetailedSong>>

    @Transaction
    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0 ORDER BY ROWID DESC")
    fun songsByRowIdDesc(): Flow<List<DetailedSong>>

    @Transaction
    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0 ORDER BY totalPlayTimeMs ASC")
    fun songsByPlayTimeAsc(): Flow<List<DetailedSong>>

    @Transaction
    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0 ORDER BY totalPlayTimeMs DESC")
    fun songsByPlayTimeDesc(): Flow<List<DetailedSong>>

    fun songs(sortBy: SongSortBy, sortOrder: SortOrder): Flow<List<DetailedSong>> {
        return when (sortBy) {
            SongSortBy.PlayTime -> when (sortOrder) {
                SortOrder.Ascending -> songsByPlayTimeAsc()
                SortOrder.Descending -> songsByPlayTimeDesc()
            }
            SongSortBy.DateAdded -> when (sortOrder) {
                SortOrder.Ascending -> songsByRowIdAsc()
                SortOrder.Descending -> songsByRowIdDesc()
            }
        }
    }

    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL ORDER BY likedAt DESC")
    fun favorites(): Flow<List<DetailedSong>>

    @Query("SELECT * FROM QueuedMediaItem")
    fun queue(): List<QueuedMediaItem>

    @Query("DELETE FROM QueuedMediaItem")
    fun clearQueue()

    @Query("SELECT * FROM SearchQuery WHERE query LIKE :query ORDER BY id DESC")
    fun queries(query: String): Flow<List<SearchQuery>>

    @Query("SELECT * FROM Song WHERE id = :id")
    fun song(id: String): Flow<Song?>

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
    @Query("SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist")
    fun playlistPreviews(): Flow<List<PlaylistPreview>>

    @Query("SELECT thumbnailUrl FROM Song JOIN SongPlaylistMap ON id = songId WHERE playlistId = :id ORDER BY position LIMIT 4")
    fun playlistThumbnailUrls(id: Long): Flow<List<String?>>

    @Transaction
    @Query("SELECT * FROM Song JOIN SongArtistMap ON Song.id = SongArtistMap.songId WHERE SongArtistMap.artistId = :artistId ORDER BY Song.ROWID DESC")
    @RewriteQueriesToDropUnusedColumns
    fun artistSongs(artistId: String): Flow<List<DetailedSong>>

    @Transaction
    @Query("SELECT * FROM Song JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId WHERE SongAlbumMap.albumId = :albumId AND position IS NOT NULL ORDER BY position")
    @RewriteQueriesToDropUnusedColumns
    fun albumSongs(albumId: String): Flow<List<DetailedSong>>

    @Query("UPDATE SongPlaylistMap SET position = position - 1 WHERE playlistId = :playlistId AND position >= :fromPosition")
    fun decrementSongPositions(playlistId: Long, fromPosition: Int)

    @Query("UPDATE SongPlaylistMap SET position = position - 1 WHERE playlistId = :playlistId AND position >= :fromPosition AND position <= :toPosition")
    fun decrementSongPositions(playlistId: Long, fromPosition: Int, toPosition: Int)

    @Query("UPDATE SongPlaylistMap SET position = position + 1 WHERE playlistId = :playlistId AND position >= :fromPosition AND position <= :toPosition")
    fun incrementSongPositions(playlistId: Long, fromPosition: Int, toPosition: Int)

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
            thumbnailUrl = mediaItem.mediaMetadata.artworkUri?.toString(),
            loudnessDb = mediaItem.mediaMetadata.extras?.getFloatOrNull("loudnessDb"),
            contentLength = mediaItem.mediaMetadata.extras?.getLongOrNull("contentLength"),
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
                        info = null
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

    @Delete
    fun delete(searchQuery: SearchQuery)

    @Delete
    fun delete(playlist: Playlist)

    @Delete
    fun delete(songPlaylistMap: SongPlaylistMap)

    fun upsert(songAlbumMap: SongAlbumMap) {
        if (insert(songAlbumMap) == -1L) {
            update(songAlbumMap)
        }
    }

    fun upsert(artist: Artist) {
        if (insert(artist) == -1L) {
            update(artist)
        }
    }

    fun upsert(album: Album) {
        if (insert(album) == -1L) {
            update(album)
        }
    }
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
    ],
    views = [
        SortedSongPlaylistMap::class
    ],
    version = 12,
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
                    .addMigrations(From8To9Migration(), From10To11Migration())
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
            it.query(SimpleSQLiteQuery("SELECT DISTINCT browseId, text, Info.id FROM Info JOIN Song ON Info.id = Song.albumId;")).use { cursor ->
                val albumValues = ContentValues(2)
                while (cursor.moveToNext()) {
                    albumValues.put("id", cursor.getString(0))
                    albumValues.put("title", cursor.getString(1))
                    it.insert("Album", CONFLICT_IGNORE, albumValues)

                    it.execSQL("UPDATE Song SET albumId = '${cursor.getString(0)}' WHERE albumId = ${cursor.getLong(2)}")
                }
            }

            it.query(SimpleSQLiteQuery("SELECT GROUP_CONCAT(text, ''), SongWithAuthors.songId FROM Info JOIN SongWithAuthors ON Info.id = SongWithAuthors.authorInfoId GROUP BY songId;")).use { cursor ->
                val songValues = ContentValues(1)
                while (cursor.moveToNext()) {
                    songValues.put("artistsText", cursor.getString(0))
                    it.update("Song", CONFLICT_IGNORE, songValues, "id = ?", arrayOf(cursor.getString(1)))
                }
            }

            it.query(SimpleSQLiteQuery("SELECT browseId, text, Info.id FROM Info JOIN SongWithAuthors ON Info.id = SongWithAuthors.authorInfoId WHERE browseId NOT NULL;")).use { cursor ->
                val artistValues = ContentValues(2)
                while (cursor.moveToNext()) {
                    artistValues.put("id", cursor.getString(0))
                    artistValues.put("name", cursor.getString(1))
                    it.insert("Artist", CONFLICT_IGNORE, artistValues)

                    it.execSQL("UPDATE SongWithAuthors SET authorInfoId = '${cursor.getString(0)}' WHERE authorInfoId = ${cursor.getLong(2)}")
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

fun query(block: () -> Unit) = DatabaseInitializer.Instance.getQueryExecutor().execute(block)

fun transaction(block: () -> Unit) = with(DatabaseInitializer.Instance) {
    getTransactionExecutor().execute {
        runInTransaction(block)
    }
}

val RoomDatabase.path: String
    get() = getOpenHelper().writableDatabase.path

fun RoomDatabase.checkpoint() {
    getOpenHelper().writableDatabase.run {
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
