package com.wt.cloudmedia.request

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.microsoft.graph.http.BaseRequest
import com.microsoft.identity.client.IAuthenticationResult
import com.wt.cloudmedia.LoginService

class UserRequest : DefaultLifecycleObserver {
    private val userLiveData = MutableLiveData<DataResult<IAuthenticationResult>>()

    val loginResult = userLiveData as LiveData<DataResult<IAuthenticationResult>>
    fun requestLogin(activity: Activity) {
        LoginService.login(activity, userLiveData::postValue)
    }
}