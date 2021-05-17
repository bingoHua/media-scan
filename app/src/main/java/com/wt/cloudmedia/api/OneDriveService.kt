package com.wt.cloudmedia.api

import androidx.annotation.WorkerThread
import androidx.lifecycle.*
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

    fun getMovies(): LiveData<ApiResponse<List<Movie>>> {
        //val client = GraphServiceClient.builder().authenticationProvider { request -> request.addHeader("Authorization", "Bearer $accessToken") }.buildClient()
        //val liveData = MutableLiveData<ApiResponse<List<Movie>>>()
        val liveData = MutableLiveData<MutableList<Movie>>()
        liveData.value = ArrayList()
        appExecutors.networkIO().execute {
            processItems(liveData)
        }
        return Transformations.map(liveData){
            ApiResponse.create(it)
        }
    }

    fun setClient(client: IGraphServiceClient) {
        this.graphServiceClient = client
    }

    private fun getItem(id: String): Movie? {
        val client = this.graphServiceClient ?: return null
        val result = client.me().drives(id).buildRequest().get()
        println("lzh.${result.name} ${result.id} ${result.webUrl}")
        val thumbnail = client.me().drive().items(result.id).thumbnails().buildRequest().get()
        result.additionalDataManager()["@microsoft.graph.downloadUrl"]?.asString?.also { url ->
            return Movie(result.id, url, thumbnail.currentPage[0].large.url, result.name, System.currentTimeMillis())
        }
        return null
    }

    private fun processItems(liveData: MutableLiveData<MutableList<Movie>>) {
        val client = this.graphServiceClient ?: return
        val children = client.me().drive().root().itemWithPath("aria").children().buildRequest().get()
        with(children.currentPage) {
            this.forEach {
                if (it.folder != null) {
                    val list = liveData.value
                    list?.addAll(processFolder(client, it))
                    liveData.postValue(list)
                } else if (it.file != null && it.video != null) {
                    val item = client.me().drive().items(it.id).buildRequest().get()
                    println("lzh.${item.name} ${item.id} ${item.webUrl}")
                    val thumbnail = client.me().drive().items(it.id).thumbnails().buildRequest().get()
                    item.additionalDataManager()["@microsoft.graph.downloadUrl"]?.asString?.also { url ->
                        val list = liveData.value
                        list?.add(Movie(item.id, url, thumbnail.currentPage[0].large.url, item.name, System.currentTimeMillis()))
                        liveData.postValue(list)
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
                    movieList.add(Movie(item.id, url, thumbnail.currentPage[0].large.url, item.name, System.currentTimeMillis()))
                }
            }
        }
        return movieList
    }

}