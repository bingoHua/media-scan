package com.wt.cloudmedia.request

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.microsoft.identity.client.IAuthenticationResult
import com.wt.cloudmedia.api.LoginService

class UserRequest  {
    private val _userLiveData = MutableLiveData<DataResult<IAuthenticationResult>>()
    private val _logOutData = MutableLiveData<DataResult<Any>>()
    val userLiveData = _userLiveData as LiveData<DataResult<IAuthenticationResult>>
    val logOutData = _logOutData as LiveData<DataResult<Any>>
    fun requestLogin(activity: Activity) {
        LoginService.login(activity, _userLiveData::postValue)
    }
    fun requestLoginOut() {
        LoginService.loginOut(_logOutData::postValue)
    }
    
}