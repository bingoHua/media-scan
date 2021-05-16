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

package com.wt.cloudmedia

import androidx.lifecycle.*
import com.wt.cloudmedia.repository.MovieRepository
import com.wt.cloudmedia.vo.Movie
import kotlinx.coroutines.launch

/**
 * View Model to keep a reference to the word repository and
 * an up-to-date list of all words.
 */

class MovieViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _allMovies = MutableLiveData<List<Movie>>()

    val allMovie = MediatorLiveData<List<Movie>>()

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(move: Movie) = viewModelScope.launch {
        repository.insert(move)
    }

    fun loadMoves() {
        val result = repository.loadMovies()
        allMovie.addSource(result) {
            if (it.data != null) {
                allMovie.value = it.data
            }
        }
    }

}

class MovieViewModelFactory(private val repository: MovieRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovieViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
