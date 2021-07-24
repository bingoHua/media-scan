package com.wt.cloudmedia.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wt.cloudmedia.db.movie.Movie
import com.wt.cloudmedia.db.movie.MovieDao
import com.wt.cloudmedia.db.recentMovie.RecentMovie
import com.wt.cloudmedia.db.recentMovie.RecentMovieDao
import com.wt.cloudmedia.vo.DateConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Movie::class, RecentMovie::class], version = 8)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun recentMovieDao(): RecentMovieDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @JvmStatic
        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "word_database")
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // Migration is not part of this codelab.
                    .fallbackToDestructiveMigration()
                    .addCallback(MovieDatabaseCallback(scope))
                    .addMigrations(object : Migration(7,8) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL("ALTER TABLE movie_table ADD COLUMN add_time INTEGER NOT NULL DEFAULT 0")
                        }
                    })
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }

        private class MovieDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            /**
             * Override the onCreate method to populate the database.
             */
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // If you want to keep the data through app restarts,
                // comment out the following line.
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.movieDao())
                    }
                }
            }
        }

        /**
         * Populate the database in a new coroutine.
         * If you want to start with more words, just add them.
         */
        suspend fun populateDatabase(movieDao: MovieDao) {
            // Start the app with a clean database every time.
            // Not needed if you only populate on creation.
            movieDao.deleteAll()
        }
    }

}