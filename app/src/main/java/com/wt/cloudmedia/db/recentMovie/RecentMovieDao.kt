package com.wt.cloudmedia.db.recentMovie

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wt.cloudmedia.db.movie.Movie

@Dao
interface RecentMovieDao {
    @Query("SELECT * FROM movie_table INNER JOIN recent_movie_table ON recent_movie_table.id == movie_table.id")
    fun getRecentMovies(): LiveData<List<Movie>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMovie(vararg recentMovie: RecentMovie)

    @Query("DELETE FROM recent_movie_table")
    fun deleteAll()
}