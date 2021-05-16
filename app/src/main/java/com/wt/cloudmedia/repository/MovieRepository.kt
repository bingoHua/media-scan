/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wt.cloudmedia.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.wt.cloudmedia.AppExecutors
import com.wt.cloudmedia.api.ApiResponse
import com.wt.cloudmedia.api.OneDriveService
import com.wt.cloudmedia.db.MovieDao
import com.wt.cloudmedia.vo.Movie
import com.wt.cloudmedia.vo.Resource

/**
 * Abstracted Repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
class MovieRepository constructor(private val appExecutors: AppExecutors, private val movieDao: MovieDao, private val oneDriveService: OneDriveService) {

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(movie: Movie) {
        movieDao.insertMovie(movie)
    }

    fun updateMove(id: String): LiveData<Resource<Movie>> {
        return object : NetworkBoundResource<Movie, Movie>(appExecutors) {

            override fun loadFromDb(): LiveData<Movie> {
                return movieDao.findById(id)
            }

            override fun createCall(): LiveData<ApiResponse<Movie>> {
                return oneDriveService.getMovie(id)
            }

            override fun saveCallResult(item: Movie) {
                movieDao.insertMovies(item)
            }

            override fun shouldFetch(data: Movie?) = true

        }.asLiveData()
    }

    @WorkerThread
    fun loadMovies(): LiveData<Resource<List<Movie>>> {
        return object : NetworkBoundResource<List<Movie>, List<Movie>>(appExecutors) {

            override fun loadFromDb(): LiveData<List<Movie>> {
                return movieDao.getAll()
            }


            override fun createCall(): LiveData<ApiResponse<List<Movie>>> {
                return oneDriveService.getMovies()
            }

            override fun saveCallResult(item: List<Movie>) {
                movieDao.insertMovies(*item.toTypedArray())
            }

            override fun shouldFetch(data: List<Movie>?) =
                data == null || data.isEmpty() || (System.currentTimeMillis() - data[0].time) > 360 * 1000

        }.asLiveData()
    }

}
