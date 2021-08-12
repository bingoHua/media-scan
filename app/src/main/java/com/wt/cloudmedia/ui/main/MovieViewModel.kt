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

package com.wt.cloudmedia.ui.main

import androidx.lifecycle.*
import com.microsoft.graph.models.extensions.IGraphServiceClient
import com.wt.cloudmedia.db.movie.Movie
import com.wt.cloudmedia.repository.DataRepository
import com.wt.cloudmedia.request.DataRequest

/**
 * View Model to keep a reference to the word repository and
 * an up-to-date list of all words.
 */

class MovieViewModel : ViewModel() {
    val dataRequest = DataRequest()
}

class MovieViewModelFactory(private val repository: DataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovieViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
