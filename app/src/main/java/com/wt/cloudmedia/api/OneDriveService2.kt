package com.wt.cloudmedia.api

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import com.funnywolf.livedatautils.EventMutableLiveData
import com.microsoft.graph.models.extensions.DriveItem
import com.microsoft.graph.models.extensions.IGraphServiceClient
import com.wt.cloudmedia.AppExecutors
import com.wt.cloudmedia.db.movie.Movie
import com.wt.cloudmedia.request.DataResult
import com.wt.cloudmedia.request.ResponseStatus

class OneDriveService2 constructor(private val appExecutors: AppExecutors) {

    @MainThread
    fun getMoviesById(id: String, client: IGraphServiceClient): LiveData<Movie>? {
        val liveData = EventMutableLiveData<Movie>()
        appExecutors.networkIO().execute {
            val item = client.me().drive().items(id).buildRequest().get()
            println("lzh.${item.name} ${item.id} ${item.webUrl}")
            val thumbnail = client.me().drive().items(id).thumbnails().buildRequest().get()
            item.additionalDataManager()["@microsoft.graph.downloadUrl"]?.asString?.also { url ->
                liveData.postValue(Movie(item.id, url, thumbnail.currentPage[0].large.url, item.name, item.createdDateTime.timeInMillis, 0, null, System.currentTimeMillis()))
            }
        }
        return liveData
    }

    fun requestMovies(client: IGraphServiceClient, result: DataResult.Result<Movie>) {
        //val client = GraphServiceClient.builder().authenticationProvider { request -> request.addHeader("Authorization", "Bearer $accessToken") }.buildClient()
        //val liveData = MutableLiveData<ApiResponse<List<Movie>>>()
        appExecutors.networkIO().execute {
            processItems(result, client)
        }
    }

    private fun processItems(result: DataResult.Result<Movie>, client: IGraphServiceClient) {
        val children = client.me().drive().root().itemWithPath("video").children().buildRequest().get()
        with(children.currentPage) {
            this.forEach {
                if (it.folder != null) {
                    processFolder(result, client, it)
                } else if (it.file != null && it.video != null) {
                    val item = client.me().drive().items(it.id).buildRequest().get()
                    println("lzh.${item.name} ${item.id} ${item.webUrl}")
                    val thumbnail = client.me().drive().items(it.id).thumbnails().buildRequest().get()
                    item.additionalDataManager()["@microsoft.graph.downloadUrl"]?.asString?.also { url ->
                        result.onResult(DataResult(Movie(item.id, url, thumbnail.currentPage[0].large.url, item.name, item.createdDateTime.timeInMillis, 0, null, System.currentTimeMillis()),
                            ResponseStatus(null, true)))
                    }
                }
            }
        }
    }

    private fun processFolder(resultCallback: DataResult.Result<Movie>, client: IGraphServiceClient, folderItem: DriveItem) {
        val result = client.me().drive().items(folderItem.id).children().buildRequest().get().currentPage
        result.forEach {
            if (it.folder != null) {
                processFolder(resultCallback, client , it)
            } else if (it.file != null && it.video != null) {
                val item = client.me().drive().items(it.id).buildRequest().get()
                println("lzh.${item.name} ${item.id} ${item.webUrl}")
                val thumbnail = client.me().drive().items(it.id).thumbnails().buildRequest().get()
                item.additionalDataManager()["@microsoft.graph.downloadUrl"]?.asString?.also { url ->
                    resultCallback.onResult(DataResult(Movie(item.id, url, thumbnail.currentPage[0].large.url, item.name, item.createdDateTime.timeInMillis, 0, null, System.currentTimeMillis()),
                    ResponseStatus(null, true)))
                }
            }
        }
    }

}