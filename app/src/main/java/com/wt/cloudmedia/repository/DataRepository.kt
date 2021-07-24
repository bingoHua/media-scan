package com.wt.cloudmedia.repository

import androidx.lifecycle.LiveData
import com.microsoft.graph.models.extensions.IGraphServiceClient
import com.wt.cloudmedia.CloudMediaApplication
import com.wt.cloudmedia.api.OneDriveService2
import com.wt.cloudmedia.db.movie.Movie

class DataRepository private constructor() {
    private val oneDriveService: OneDriveService2 = OneDriveService2(CloudMediaApplication.instance().appExecutors)

    companion object {
        private val S_REQUEST_MANAGER = DataRepository()

        fun getInstance(): DataRepository {
            return S_REQUEST_MANAGER
        }
    }

    fun getMoviesById(id: String, client: IGraphServiceClient): LiveData<Movie>? {
        return oneDriveService.getMoviesById(id, client)
    }

    fun getMovies(client: IGraphServiceClient): LiveData<Movie> {
        return oneDriveService.getMovies(client)
    }

}