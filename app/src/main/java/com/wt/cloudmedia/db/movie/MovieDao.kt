package com.wt.cloudmedia.db.movie

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MovieDao {
    @Query("SELECT * FROM movie_table ORDER BY create_time DESC")
    fun getAll(): LiveData<List<Movie>>

    @Query("SELECT * FROM movie_table WHERE id LIKE :id LIMIT 1")
    fun findById(id: String): Movie?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMovies(vararg movies: Movie)

    @Delete
    fun delete(movie: Movie)

    @Update
    fun updateMovie(movie: Movie)

    @Query("DELETE FROM movie_table")
    suspend fun deleteAll()

}