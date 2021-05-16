package com.wt.cloudmedia

import android.app.Application
import com.microsoft.graph.models.extensions.IGraphServiceClient
import com.wt.cloudmedia.api.OneDriveService
import com.wt.cloudmedia.db.MovieDatabase
import com.wt.cloudmedia.repository.MovieRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class CloudMediaApplication : Application() {
    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { MovieDatabase.getDatabase(this, applicationScope) }
    val appExecutors by lazy { AppExecutors() }
    val oneDriveService by lazy { OneDriveService(appExecutors) }
    val repository by lazy { MovieRepository(appExecutors, database.movieDao(), oneDriveService) }
}