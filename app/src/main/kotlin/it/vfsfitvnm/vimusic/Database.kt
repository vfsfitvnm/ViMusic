package it.vfsfitvnm.vimusic

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.os.Parcel
import androidx.media3.common.MediaItem
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import it.vfsfitvnm.vimusic.models.*
import kotlinx.coroutines.flow.Flow


@Dao
interface Database {
    companion object : Database by DatabaseInitializer.Instance.database

    @Query("SELECT * FROM SearchQuery WHERE query LIKE :query ORDER BY id DESC")
    fun getRecentQueries(query: String): Flow<List<SearchQuery>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(searchQuery: SearchQuery)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(info: Artist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(info: Album)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(playlist: Playlist): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(info: SongInPlaylist): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(info: List<Artist>): List<Long>

    @Query("SELECT * FROM Song WHERE id = :id")
    fun songFlow(id: String): Flow<Song?>

    @Query("SELECT * FROM Song WHERE id = :id")
    fun song(id: String): Song?

    @Query("SELECT * FROM Playlist WHERE id = :id")
    fun playlist(id: Long): Playlist?

    @Query("SELECT * FROM Song")
    fun songs(): Flow<List<Song>>

    @Query("SELECT * FROM Artist WHERE id = :id")
    fun artist(id: String): Flow<Artist?>

    @Transaction
    @Query("SELECT * FROM Song WHERE id = :id")
    fun songWithInfo(id: String): DetailedSong?

    @Transaction
    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0 ORDER BY ROWID DESC")
    fun history(): Flow<List<DetailedSong>>

    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL ORDER BY likedAt DESC")
    fun favorites(): Flow<List<DetailedSong>>

    @Transaction
    @Query("SELECT * FROM Song WHERE totalPlayTimeMs >= 60000 ORDER BY totalPlayTimeMs DESC LIMIT 20")
    fun mostPlayed(): Flow<List<DetailedSong>>

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
    fun insert(songWithAuthors: SongArtistMap): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(song: Song): Long

    @Update
    fun update(song: Song)

    @Update
    fun update(artist: Artist)

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

    @Transaction
    @Query("SELECT * FROM Song JOIN SongArtistMap ON Song.id = SongArtistMap.songId WHERE SongArtistMap.artistId = :artistId ORDER BY Song.ROWID DESC")
    @RewriteQueriesToDropUnusedColumns
    fun artistSongs(artistId: String): Flow<List<DetailedSong>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertQueue(queuedMediaItems: List<QueuedMediaItem>)

    @Query("SELECT * FROM QueuedMediaItem")
    fun queue(): List<QueuedMediaItem>

    @Query("DELETE FROM QueuedMediaItem")
    fun clearQueue()
}

@androidx.room.Database(
    entities = [
        Song::class,
        SongInPlaylist::class,
        Playlist::class,
        Artist::class,
        SongArtistMap::class,
        Album::class,
        SearchQuery::class,
        QueuedMediaItem::class,
    ],
    views = [
        SortedSongInPlaylist::class
    ],
    version = 11,
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
        AutoMigration(from = 10, to = 11),
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
                    .addMigrations(From8To9Migration())
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
