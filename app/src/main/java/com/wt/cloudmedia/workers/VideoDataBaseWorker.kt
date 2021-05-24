package com.wt.cloudmedia.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wt.cloudmedia.db.AppDatabase
import com.wt.cloudmedia.db.recentMovie.RecentMovie
import com.wt.cloudmedia.repository.MovieRepository2
import kotlinx.coroutines.coroutineScope
import java.sql.Date

class VideoDataBaseWorker(context: Context, workerParameters: WorkerParameters, private val repository2: MovieRepository2) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result = coroutineScope {
        val movieDao = AppDatabase.getDatabase(applicationContext, this).movieDao()
        val recentMovie = AppDatabase.getDatabase(applicationContext, this).recentMovieDao()
        repository2.getMoviesOneTime()?.also {
            it.forEach { movie ->
                val result = movieDao.findById(movie.id)
                if (result == null) {
                    movieDao.insertMovies(movie)
                    recentMovie.insertMovie(RecentMovie(movie.id, Date(System.currentTimeMillis()), 0))
                } else if (result.url != movie.url) {
                    movieDao.updateMovie(movie)
                }
            }
        } ?: Result.failure()
        Result.success()
    }
}