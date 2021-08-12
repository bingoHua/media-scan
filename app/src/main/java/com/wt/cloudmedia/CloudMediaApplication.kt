package com.wt.cloudmedia

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.wt.cloudmedia.api.OneDriveService2
import com.wt.cloudmedia.db.AppDatabase
import com.wt.cloudmedia.repository.DataRepository
import com.wt.cloudmedia.repository.MovieRepository2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.properties.Delegates

class CloudMediaApplication : Application(), ViewModelStoreOwner {
    // No need to cancel this scope as it'll be torn down with the process
    private val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    private val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val dataRepository by lazy { DataRepository.getInstance(oneDriveService, database.movieDao()) }
    private val appExecutors by lazy { AppExecutors() }
    private val oneDriveService by lazy { OneDriveService2(appExecutors) }
    val repository by lazy {
        MovieRepository2(
            appExecutors,
            database.movieDao(),
            database.recentMovieDao(),
            oneDriveService
        )
    }
    private lateinit var appViewModelStore: ViewModelStore

    override fun onCreate() {
        super.onCreate()
        instance = this
        appViewModelStore = ViewModelStore()
    }

    companion object {
        private var instance: CloudMediaApplication by Delegates.notNull()
        fun instance() = instance
    }

    override fun getViewModelStore(): ViewModelStore {
        return appViewModelStore
    }

}