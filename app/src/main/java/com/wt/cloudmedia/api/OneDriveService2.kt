package com.wt.cloudmedia.api

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.funnywolf.livedatautils.EventMutableLiveData
import com.microsoft.graph.models.extensions.DriveItem
import com.microsoft.graph.models.extensions.IGraphServiceClient
import com.wt.cloudmedia.AppExecutors
import com.wt.cloudmedia.db.movie.Movie

class OneDriveService2 constructor(private val appExecutors: AppExecutors) {

    private var graphServiceClient: IGraphServiceClient? = null

    fun getMovies(): LiveData<Movie> {
        //val client = GraphServiceClient.builder().authenticationProvider { request -> request.addHeader("Authorization", "Bearer $accessToken") }.buildClient()
        //val liveData = MutableLiveData<ApiResponse<List<Movie>>>()
        val liveData = EventMutableLiveData<Movie>()
        appExecutors.networkIO().execute {
            processItems(liveData)
        }
        return liveData
    }

    fun getMoviesOneTime(): List<Movie>? {
        return processItems()
    }

    fun setClient(client: IGraphServiceClient) {
        this.graphServiceClient = client
    }

    private fun processItems(): List<Movie>? {
        val list = ArrayList<Movie>()
        val client = this.graphServiceClient ?: return null
        val children = client.me().drive().root().itemWithPath("aria").children().buildRequest().get()
        with(children.currentPage) {
            this.forEach {
                if (it.folder != null) {
                    list.addAll(processFolder(client, it))
                } else if (it.file != null && it.video != null) {
                    val item = client.me().drive().items(it.id).buildRequest().get()
                    println("lzh.${item.name} ${item.id} ${item.webUrl}")
                    val thumbnail = client.me().drive().items(it.id).thumbnails().buildRequest().get()
                    item.additionalDataManager()["@microsoft.graph.downloadUrl"]?.asString?.also { url ->
                        list.add(Movie(item.id, url, thumbnail.currentPage[0].large.url, item.name, item.createdDateTime.timeInMillis, 0, null))
                    }
                }
            }
        }
        return list;
    }

    private fun processItems(liveData: EventMutableLiveData<Movie>) {
        val client = this.graphServiceClient ?: return
        val children = client.me().drive().root().itemWithPath("aria").children().buildRequest().get()
        with(children.currentPage) {
            this.forEach {
                if (it.folder != null) {
                    processFolder(client, it).let { movies ->
                        movies.forEach { movie ->
                            liveData.postValue(movie)
                        }
                    }
                } else if (it.file != null && it.video != null) {
                    val item = client.me().drive().items(it.id).buildRequest().get()
                    println("lzh.${item.name} ${item.id} ${item.webUrl}")
                    val thumbnail = client.me().drive().items(it.id).thumbnails().buildRequest().get()
                    item.additionalDataManager()["@microsoft.graph.downloadUrl"]?.asString?.also { url ->
                        liveData.postValue(Movie(item.id, url, thumbnail.currentPage[0].large.url, item.name, item.createdDateTime.timeInMillis, 0, null))
                    }
                }
            }
        }
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
                    movieList.add(Movie(item.id, url, thumbnail.currentPage[0].large.url, item.name, item.createdDateTime.timeInMillis, 0, null))
                }
            }
        }
        return movieList
    }

}