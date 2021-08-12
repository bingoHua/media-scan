package com.wt.cloudmedia.repository

import androidx.lifecycle.LiveData
import com.microsoft.graph.models.extensions.IGraphServiceClient
import com.wt.cloudmedia.api.OneDriveService2
import com.wt.cloudmedia.db.AppDatabase
import com.wt.cloudmedia.db.movie.Movie
import kotlinx.coroutines.flow.Flow

class DataRepository private constructor(
    private val oneDriveService: OneDriveService2,
    private val movieDao: AppDatabase
) {
    companion object {
       @Volatile
       private var instance : DataRepository? = null

        fun getInstance(oneDriveService: OneDriveService2, movieDao: AppDatabase) =
            instance?: synchronized(this) {
                instance?:DataRepository(oneDriveService, movieDao).also { instance = it }
            }
    }

    fun getMoviesById(id: String, client: IGraphServiceClient): LiveData<Movie>? {
        return oneDriveService.getMoviesById(id, client)
    }

    fun getMoviesFromDB() = movieDao.movieDao().getAll()

    fun requestUpdateMovies(client: IGraphServiceClient) {
        oneDriveService.getMovies(client)
    }

}