package com.wt.cloudmedia

import android.app.Activity
import android.app.Application
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wt.cloudmedia.common.ui.LoadingDialog

open class BaseActivity: AppCompatActivity() {

    private var mApplicationProvider: ViewModelProvider? = null
    private var loadingDialog:LoadingDialog? = null

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
    @UiThread
    protected fun showLoading(cancelable: Boolean) {
        if (supportFragmentManager.isDestroyed) {
            return
        }
        loadingDialog?:run {
            loadingDialog = LoadingDialog()
            loadingDialog
        }?.also {
            if (!it.isAdded) {
                it.isCancelable = cancelable
                it.show(supportFragmentManager, ACTIVITY_LOADING_DIALOG)
            }
        }
    }

    @UiThread
    protected fun hideLoading() {
        loadingDialog?.let {
            if (it.isAdded) {
                it.dismiss()
            }
        }
    }

    companion object {
        private const val ACTIVITY_LOADING_DIALOG = "activity_loading_dialog"
    }

}