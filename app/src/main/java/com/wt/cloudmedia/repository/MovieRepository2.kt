package com.wt.cloudmedia.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.switchMap
import com.funnywolf.livedatautils.EventMediatorLiveData
import com.funnywolf.livedatautils.EventMutableLiveData
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

    private val dbResult = MediatorLiveData<List<Movie>>()
    val moveLivedata: LiveData<List<Movie>> = dbResult.switchMap {
        processUpdate(it)
    }

    private fun processUpdate(movieList: List<Movie>): LiveData<List<Movie>> {
        val mediator = EventMediatorLiveData<List<Movie>>()
        val liveData = EventMutableLiveData<Movie>()
        mediator.value = arrayListOf()
        mediator.addSource(liveData) {
            (mediator.value as MutableList).add(it)
        }
        appExecutors.networkIO().execute {
            movieList.forEach {
                if ( System.currentTimeMillis() - it.time  < 3600 * 1000) {
                    liveData.postValue(it)
                } else {
                    appExecutors.mainThread().execute {
                        /*val movieLiveData = oneDriveService.getMoviesById(it.id)
                        movieLiveData?.let { moLiveData ->
                            mediator.addSource(moLiveData) { movie ->
                                mediator.removeSource(moLiveData)
                                val list = mediator.value as MutableList
                                list.add(movie)
                                mediator.value = list
                                appExecutors.diskIO().execute {
                                    movieDao.updateMovie(movie)
                                }
                            }
                        }*/
                    }
                }
            }
        }
        return mediator
    }

    init {
        val dbData = movieDao.getAll()
        dbResult.addSource(dbData) {
            dbResult.removeSource(dbData)
            if (!it.isNullOrEmpty()) {
                dbResult.value = it
            }
        }
    }

    private fun fetchFromRemote() {
        /*dbResult.addSource(oneDriveService.getMovies()) {
            appExecutors.networkIO().execute {
                val list = dbResult.value
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
                        Log.d("MovieRepository2", "${it.name} update.")
                    } else {
                        Log.d("MovieRepository2", "${it.name} is added.")
                    }
                }
            }
        }*/
    }

    fun getMovies(): LiveData<List<Movie>> {
        fetchFromRemote()
        return dbResult
    }

    fun getRecentMovies(): LiveData<List<Movie>> {
        return recentMovieDao.getRecentMovies()
    }

    fun saveRecentMovie(movie: Movie) {
        recentMovieDao.insertMovie(RecentMovie(movie.id, Date(System.currentTimeMillis()), movie.timeStamp))
    }


}