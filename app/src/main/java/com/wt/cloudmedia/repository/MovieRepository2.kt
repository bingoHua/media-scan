package com.wt.cloudmedia.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.wt.cloudmedia.AppExecutors
import com.wt.cloudmedia.api.OneDriveService2
import com.wt.cloudmedia.db.movie.Movie
import com.wt.cloudmedia.db.movie.MovieDao
import com.wt.cloudmedia.db.recentMovie.RecentMovie
import com.wt.cloudmedia.db.recentMovie.RecentMovieDao
import java.sql.Date

class MovieRepository2 constructor(private val appExecutors: AppExecutors,
                                   private val movieDao: MovieDao,
                                   private val recentMovieDao: RecentMovieDao,
                                   private val oneDriveService: OneDriveService2) {

    private val movieResult = MediatorLiveData<List<Movie>>()

    init {
        val dbData = movieDao.getAll()
        movieResult.addSource(dbData) {
            if (!it.isNullOrEmpty()) {
                movieResult.value = it
            }
        }
    }

    private fun fetchFromRemote() {
        movieResult.addSource(oneDriveService.getMovies()) {
            appExecutors.networkIO().execute {
                val list = movieResult.value
                val match = list?.find { movie ->
                    it.id == movie.id
                }
                if (match == null) {
                    Log.d("MovieRepository2", "${it.name} is not added.")
                    movieDao.insertMovies(it)
                    recentMovieDao.insertMovie(RecentMovie(it.id, it.playDate, it.timeStamp))
                } else {
                    if (match.url != it.url) {
                        movieDao.updateMovie(it)
                    }
                    Log.d("MovieRepository2", "${it.name} is added.")
                }
            }
        }
    }

    fun getMovies(): LiveData<List<Movie>> {
        fetchFromRemote()
        return movieResult
    }

    fun getRecentMovies(): LiveData<List<Movie>> {
        return recentMovieDao.getRecentMovies()
    }

    fun saveRecentMovie(movie: Movie) {
        recentMovieDao.insertMovie(RecentMovie(movie.id, Date(System.currentTimeMillis()), movie.timeStamp))
    }

    fun getMoviesOneTime(): List<Movie>? {
        return oneDriveService.getMoviesOneTime()
    }

}