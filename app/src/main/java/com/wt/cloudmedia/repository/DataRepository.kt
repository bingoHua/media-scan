package com.wt.cloudmedia.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.microsoft.graph.models.extensions.IGraphServiceClient
import com.wt.cloudmedia.api.OneDriveService2
import com.wt.cloudmedia.db.AppDatabase
import com.wt.cloudmedia.db.movie.Movie
import com.wt.cloudmedia.db.movie.MovieDao
import com.wt.cloudmedia.request.DataResult

class DataRepository private constructor(
    private val oneDriveService: OneDriveService2,
    private val movieDao: MovieDao
) {
    companion object {
       @Volatile
       private var instance : DataRepository? = null

        fun getInstance(oneDriveService: OneDriveService2, movieDao: MovieDao) =
            instance?: synchronized(this) {
                instance?:DataRepository(oneDriveService, movieDao).also { instance = it }
            }
    }

    fun getMoviesById(id: String, client: IGraphServiceClient): LiveData<Movie>? {
        return oneDriveService.getMoviesById(id, client)
    }

    fun getMoviesFromDB() = movieDao.getAll()

    fun requestUpdateMovies(client: IGraphServiceClient) {
        oneDriveService.requestMovies(client, object : DataResult.Result<Movie> {
            override fun onResult(dataResult: DataResult<Movie>) {
                if (dataResult.responseStatus.isSuccess) {
                    val match = movieDao.findById(dataResult.result.id)
                    match?.also {
                        if (match.url != dataResult.result.url) {
                            movieDao.updateMovie(dataResult.result)
                            Log.d("DataRepository", "${it.name} update.")
                        } else {
                            Log.d("DataRepository", "${it.name} is added.")
                        }
                    }?: run {
                        Log.d("DataRepository", "${dataResult.result.name} is not added.")
                        movieDao.insertMovies(dataResult.result)
                    }
                }
            }
        })
    }

}