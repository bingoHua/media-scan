package com.wt.cloudmedia.request

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.LiveData
import com.funnywolf.livedatautils.EventMediatorLiveData
import com.microsoft.graph.requests.extensions.GraphServiceClient
import com.wt.cloudmedia.CloudMediaApplication
import com.wt.cloudmedia.api.LoginService
import com.wt.cloudmedia.db.movie.Movie
import com.wt.cloudmedia.service.VideoFetchService

class DataRequest {

    fun requestMovie(): LiveData<DataResult<List<Movie>>> {
        val liveData = EventMediatorLiveData<DataResult<List<Movie>>>()
        val token = LoginService.getAuthenticationResult()
        if (token == null) {
            liveData.value = DataResult(null, ResponseStatus("pls login first", false))
        } else {
            liveData.addSource(CloudMediaApplication.instance().dataRepository.getMoviesFromDB()) {
                liveData.value = DataResult(it, ResponseStatus(null, true))
            }
            val result = GraphServiceClient.builder().authenticationProvider { request ->
                request.addHeader("Authorization", "Bearer ${token.accessToken}")
            }
            CloudMediaApplication.instance().dataRepository.requestUpdateMovies(result.buildClient())
        }
        return liveData
    }

    fun launchService(context:Context) {
        Intent(context, VideoFetchService::class.java).let {
            it.action = "start"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(it)
            } else {
                context.startService(it)
            }
        }
    }

}