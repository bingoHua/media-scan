package com.wt.cloudmedia.ui.rencent

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wt.cloudmedia.db.movie.Movie
import com.wt.cloudmedia.repository.MovieRepository2

class RecentViewModel(private val repository: MovieRepository2) : ViewModel() {

    fun getRecentMovies(): LiveData<List<Movie>> {
        return repository.getRecentMovies()
    }

}

class RecentMovieViewModelFactory(private val repository: MovieRepository2) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}