package com.wt.cloudmedia.api

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.microsoft.graph.models.extensions.DriveItem
import com.microsoft.graph.models.extensions.IGraphServiceClient
import com.wt.cloudmedia.AppExecutors
import com.wt.cloudmedia.vo.Movie

class OneDriveService constructor(private val appExecutors: AppExecutors) {

    private var graphServiceClient: IGraphServiceClient? = null

    fun getMovie(id: String): LiveData<ApiResponse<Movie>> {
        val liveData = MutableLiveData<ApiResponse<Movie>>()
        appExecutors.networkIO().execute {
            getItem(id)?.let {
                liveData.postValue(ApiResponse.create(it))
            }
        }
        return liveData
    }

    @WorkerThread
    fun getMovies(): LiveData<ApiResponse<List<Movie>>> {
        //val client = GraphServiceClient.builder().authenticationProvider { request -> request.addHeader("Authorization", "Bearer $accessToken") }.buildClient()
        val liveData = MutableLiveData<ApiResponse<List<Movie>>>()
        appExecutors.networkIO().execute {
            processItems()?.let {
                liveData.postValue(ApiResponse.create(it))
            }
        }
        return liveData
    }

    fun setClient(client: IGraphServiceClient) {
        this.graphServiceClient = client
    }

    private fun getItem(id: String): Movie? {
        val client = this.graphServiceClient ?: return null
        val result = client.me().drives(id).buildRequest().get()
        val thumbnail = client.me().drive().items(result.id).thumbnails().buildRequest().get()
        result.additionalDataManager()["@microsoft.graph.downloadUrl"]?.asString?.also { url ->
            return Movie(result.id, url, thumbnail.currentPage[0].large.url, result.name, System.currentTimeMillis())
        }
        return null
    }

    private fun processItems(): List<Movie>? {
        val client = this.graphServiceClient ?: return null
        val children = client.me().drive().root().itemWithPath("aria").children().buildRequest().get()
        val items = ArrayList<Movie>()
        with(children.currentPage) {
            this.forEach {
                if (it.folder != null) {
                    items.addAll(processFolder(client, it))
                } else if (it.file != null && it.video != null) {
                    val item = client.me().drive().items(it.id).buildRequest().get()
                    println("lzh.${item.name} ${item.id} ${item.webUrl}")
                    val thumbnail = client.me().drive().items(it.id).thumbnails().buildRequest().get()
                    item.additionalDataManager()["@microsoft.graph.downloadUrl"]?.asString?.also { url ->
                        items.add(Movie(item.id, url, thumbnail.currentPage[0].large.url, item.name, System.currentTimeMillis()))
                    }
                }
            }
        }
        return items
    }

    private fun processFolder(client: IGraphServiceClient, folderItem: DriveItem): List<Movie> {
        val movieList = ArrayList<Movie>()
        val result = client.me().drive().items(folderItem.id).children().buildRequest().get().currentPage
        result.forEach {
            if (it.folder != null) {
                movieList.addAll(processFolder(client, it))
            } else if (it.file != null && it.video != null) {
                val item = client.me().drive().items(it.id).buildRequest().get()
                println("lzh.${item.name} ${item.id} ${item.webUrl}")
                val thumbnail = client.me().drive().items(it.id).thumbnails().buildRequest().get()
                item.additionalDataManager()["@microsoft.graph.downloadUrl"]?.asString?.also { url ->
                    movieList.add(Movie(item.id, url, thumbnail.currentPage[0].large.url, item.name, System.currentTimeMillis()))
                }
            }
        }
        return movieList
    }

}