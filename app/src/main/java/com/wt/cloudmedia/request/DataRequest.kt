package com.wt.cloudmedia.request

import androidx.lifecycle.LiveData
import com.funnywolf.livedatautils.EventMediatorLiveData
import com.microsoft.graph.requests.extensions.GraphServiceClient
import com.wt.cloudmedia.CloudMediaApplication
import com.wt.cloudmedia.api.LoginService
import com.wt.cloudmedia.db.movie.Movie

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

}