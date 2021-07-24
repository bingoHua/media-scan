package com.wt.cloudmedia

import android.app.Activity
import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

open class BaseActivity: AppCompatActivity() {

    private var mApplicationProvider: ViewModelProvider? = null

    fun getMediaApplication():CloudMediaApplication {
        return application as CloudMediaApplication
    }
    protected open fun <T : ViewModel?> getApplicationScopeViewModel(modelClass: Class<T>): T {
        if (mApplicationProvider == null) {
            mApplicationProvider = ViewModelProvider(this,
                getAppFactory(this))
        }
        return mApplicationProvider?.get(modelClass)?:throw Exception()
    }

    private fun getAppFactory(activity: Activity): ViewModelProvider.Factory {
        val application: Application = checkApplication(activity)
        return ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }

    private fun checkApplication(activity: Activity): Application {
        return activity.application
            ?: throw IllegalStateException("Your activity/fragment is not yet attached to "
                    + "Application. You can't request ViewModel before onCreate call.")
    }

}