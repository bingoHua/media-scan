package com.wt.cloudmedia.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.wt.cloudmedia.vo.Movie

@Dao
interface MovieDao {
    @Query("SELECT * FROM movie_table")
    fun getAll(): LiveData<List<Movie>>

    @Query("SELECT * FROM movie_table WHERE id IN (:movieIds)")
    fun loadAllByIds(movieIds: IntArray): LiveData<Movie>

    @Query("SELECT * FROM movie_table WHERE id LIKE :id LIMIT 1")
    fun findById(id:String): LiveData<Movie>

    @Insert
    fun insertMovies(vararg movies: Movie)

    @Insert
    fun insertMovie(movie: Movie)

    @Delete
    fun delete(movie: Movie)

    @Query("DELETE FROM movie_table")
    suspend fun deleteAll()

}