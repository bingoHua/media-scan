package com.wt.cloudmedia.request

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.microsoft.identity.client.IAuthenticationResult
import com.wt.cloudmedia.api.LoginService

class UserRequest  {
    private val _userLiveData = MutableLiveData<DataResult<IAuthenticationResult>>()

    val userLiveData = _userLiveData as LiveData<DataResult<IAuthenticationResult>>
    fun requestLogin(activity: Activity) {
        LoginService.login(activity, _userLiveData::postValue)
    }
}