package it.vfsfitvnm.vimusic

import android.content.Context
import android.database.Cursor
import android.os.Parcel
import androidx.media3.common.MediaItem
import androidx.room.*
import it.vfsfitvnm.vimusic.models.*
import kotlinx.coroutines.flow.Flow


@Dao
interface Database {
    companion object : Database by DatabaseInitializer.Instance.database

    @Query("SELECT * FROM SearchQuery WHERE query LIKE :query ORDER BY id DESC")
    fun getRecentQueries(query: String): Flow<List<SearchQuery>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(searchQuery: SearchQuery)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(info: Info): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(playlist: Playlist): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(info: SongInPlaylist): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(info: List<Info>): List<Long>

    @Query("SELECT * FROM Song WHERE id = :id")
    fun songFlow(id: String): Flow<Song?>

    @Query("SELECT * FROM Song WHERE id = :id")
    fun song(id: String): Song?

    @Query("SELECT * FROM Playlist WHERE id = :id")
    fun playlist(id: Long): Playlist?

    @Query("SELECT * FROM Song")
    fun songs(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song WHERE id = :id")
    fun songWithInfo(id: String): SongWithInfo?

    @Transaction
    @Query("SELECT * FROM Song WHERE totalPlayTimeMs >= 15000 ORDER BY ROWID DESC")
    fun history(): Flow<List<SongWithInfo>>

    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL ORDER BY likedAt DESC")
    fun favorites(): Flow<List<SongWithInfo>>

    @Transaction
    @Query("SELECT * FROM Song WHERE totalPlayTimeMs >= 60000 ORDER BY totalPlayTimeMs DESC LIMIT 20")
    fun mostPlayed(): Flow<List<SongWithInfo>>

    @Query("UPDATE Song SET totalPlayTimeMs = totalPlayTimeMs + :addition WHERE id = :id")
    fun incrementTotalPlayTimeMs(id: String, addition: Long)

    @Transaction
    @Query("SELECT * FROM Playlist WHERE id = :id")
    fun playlistWithSongs(id: Long): Flow<PlaylistWithSongs?>

    @Query("SELECT COUNT(*) FROM SongInPlaylist WHERE playlistId = :id")
    fun playlistSongCount(id: Long): Int

    @Query("UPDATE SongInPlaylist SET position = position - 1 WHERE playlistId = :playlistId AND position >= :fromPosition")
    fun decrementSongPositions(playlistId: Long, fromPosition: Int)

    @Query("UPDATE SongInPlaylist SET position = position - 1 WHERE playlistId = :playlistId AND position >= :fromPosition AND position <= :toPosition")
    fun decrementSongPositions(playlistId: Long, fromPosition: Int, toPosition: Int)

    @Query("UPDATE SongInPlaylist SET position = position + 1 WHERE playlistId = :playlistId AND position >= :fromPosition AND position <= :toPosition")
    fun incrementSongPositions(playlistId: Long, fromPosition: Int, toPosition: Int)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(songWithAuthors: SongWithAuthors): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(song: Song): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertQueuedMediaItems(queuedMediaItems: List<QueuedMediaItem>)

    @Query("SELECT * FROM QueuedMediaItem")
    fun queuedMediaItems(): List<QueuedMediaItem>

    @Query("DELETE FROM QueuedMediaItem")
    fun clearQueuedMediaItems()

    @Update
    fun update(song: Song)

    @Update
    fun update(songInPlaylist: SongInPlaylist)

    @Update
    fun update(playlist: Playlist)

    @Delete
    fun delete(searchQuery: SearchQuery)

    @Delete
    fun delete(playlist: Playlist)

    @Delete
    fun delete(song: Song)

    @Delete
    fun delete(songInPlaylist: SongInPlaylist)

    @Transaction
    @Query("SELECT id, name, (SELECT COUNT(*) FROM SongInPlaylist WHERE playlistId = id) as songCount FROM Playlist")
    fun playlistPreviews(): Flow<List<PlaylistPreview>>

    @Query("SELECT thumbnailUrl FROM Song JOIN SongInPlaylist ON id = songId WHERE playlistId = :id ORDER BY position LIMIT 4")
    fun playlistThumbnailUrls(id: Long): Flow<List<String?>>
}

@androidx.room.Database(
    entities = [
        Song::class,
        SongInPlaylist::class,
        Playlist::class,
        Info::class,
        SongWithAuthors::class,
        SearchQuery::class,
        QueuedMediaItem::class
    ],
    views = [
        SortedSongInPlaylist::class
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ]
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
                    .build()
            }
        }
    }
}

@TypeConverters
object Converters {
    // TODO: temporary
    @TypeConverter
    fun mediaItemFromByteArray(value: ByteArray?): MediaItem? {
        return value?.let { byteArray ->
            val parcel = Parcel.obtain()
            parcel.unmarshall(byteArray, 0, byteArray.size)
            parcel.setDataPosition(0)

            val pb = parcel.readBundle(MediaItem::class.java.classLoader)
            parcel.recycle()
            pb?.let {
                MediaItem.CREATOR.fromBundle(pb)
            }
        }
    }

    // TODO: temporary
    @TypeConverter
    fun mediaItemToByteArray(mediaItem: MediaItem?): ByteArray? {
        return mediaItem?.toBundle()?.let { persistableBundle ->
            val parcel = Parcel.obtain()
            parcel.writeBundle(persistableBundle)
            parcel.marshall().also {
                parcel.recycle()
            }
        }
    }
}

val Database.internal: RoomDatabase
    get() = DatabaseInitializer.Instance

fun Database.checkpoint() {
    internal.openHelper.writableDatabase.run {
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
