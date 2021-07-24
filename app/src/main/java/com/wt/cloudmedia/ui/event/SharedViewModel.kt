package com.wt.cloudmedia.ui.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.microsoft.graph.models.extensions.IGraphServiceClient
import com.wt.cloudmedia.request.UserRequest

class SharedViewModel : ViewModel() {
    val userRequest = UserRequest()

}