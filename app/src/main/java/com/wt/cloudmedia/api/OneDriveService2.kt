package com.wt.cloudmedia.api

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.microsoft.graph.models.extensions.DriveItem
import com.microsoft.graph.models.extensions.IGraphServiceClient
import com.wt.cloudmedia.AppExecutors
import com.wt.cloudmedia.db.movie.Movie

class OneDriveService2 constructor(private val appExecutors: AppExecutors) {

    private var graphServiceClient: IGraphServiceClient? = null

    @WorkerThread
    fun getMovie(id: String): Movie? {
        return getItem(id)
    }

    fun getMovies(): LiveData<Movie> {
        //val client = GraphServiceClient.builder().authenticationProvider { request -> request.addHeader("Authorization", "Bearer $accessToken") }.buildClient()
        //val liveData = MutableLiveData<ApiResponse<List<Movie>>>()
        val liveData = MutableLiveData<Movie>()
        appExecutors.networkIO().execute {
            processItems(liveData)
        }
        return liveData
    }

    fun setClient(client: IGraphServiceClient) {
        this.graphServiceClient = client
    }

    private fun getItem(id: String): Movie? {
        val client = this.graphServiceClient ?: return null
        val result = client.me().drive().items(id).buildRequest().get()
        println("lzh.${result.name} ${result.id} ${result.webUrl}")
        val thumbnail = client.me().drive().items(result.id).thumbnails().buildRequest().get()
        result.additionalDataManager()["@microsoft.graph.downloadUrl"]?.asString?.also { url ->
            return Movie(result.id, url, thumbnail.currentPage[0].large.url, result.name, System.currentTimeMillis(), 0, null)
        }
        return null
    }

    private fun processItems(liveData: MutableLiveData<Movie>) {
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
                        liveData.postValue(Movie(item.id, url, thumbnail.currentPage[0].large.url, item.name, System.currentTimeMillis(), 0, null))
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
                    movieList.add(Movie(item.id, url, thumbnail.currentPage[0].large.url, item.name, System.currentTimeMillis(), 0, null))
                }
            }
        }
        return movieList
    }

}