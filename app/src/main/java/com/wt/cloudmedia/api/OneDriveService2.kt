package com.wt.cloudmedia.api

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import com.funnywolf.livedatautils.EventMutableLiveData
import com.microsoft.graph.models.extensions.DriveItem
import com.microsoft.graph.models.extensions.IBaseGraphServiceClient
import com.microsoft.graph.models.extensions.IGraphServiceClient
import com.wt.cloudmedia.AppExecutors
import com.wt.cloudmedia.db.movie.Movie

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

    fun getMovies(client: IGraphServiceClient): LiveData<Movie> {
        //val client = GraphServiceClient.builder().authenticationProvider { request -> request.addHeader("Authorization", "Bearer $accessToken") }.buildClient()
        //val liveData = MutableLiveData<ApiResponse<List<Movie>>>()
        val liveData = EventMutableLiveData<Movie>()
        appExecutors.networkIO().execute {
            processItems(liveData, client)
        }
        return liveData
    }

    fun getMoviesOneTime(client: IGraphServiceClient): List<Movie>? {
        return processItems(client)
    }

    private fun processItems(client: IGraphServiceClient): List<Movie>? {
        val list = ArrayList<Movie>()
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
                        list.add(Movie(item.id, url, thumbnail.currentPage[0].large.url, item.name, item.createdDateTime.timeInMillis, 0, null, System.currentTimeMillis()))
                    }
                }
            }
        }
        return list;
    }

    private fun processItems(liveData: EventMutableLiveData<Movie>, client: IGraphServiceClient) {
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
                        liveData.postValue(Movie(item.id, url, thumbnail.currentPage[0].large.url, item.name, item.createdDateTime.timeInMillis, 0, null, System.currentTimeMillis()))
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
                    movieList.add(Movie(item.id, url, thumbnail.currentPage[0].large.url, item.name, item.createdDateTime.timeInMillis, 0, null, System.currentTimeMillis()))
                }
            }
        }
        return movieList
    }

}