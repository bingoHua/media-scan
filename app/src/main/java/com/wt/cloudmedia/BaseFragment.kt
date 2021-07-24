package com.wt.cloudmedia

import android.app.Activity
import android.app.Application
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

open class BaseFragment : Fragment() {

    private var mFragmentProvider: ViewModelProvider? = null
    private var mApplicationProvider: ViewModelProvider? = null

    fun getBaseActivity(): BaseActivity {
        return activity as BaseActivity
    }

    fun getMediaApplication(): CloudMediaApplication {
        return getBaseActivity().getMediaApplication()
    }

    fun showToast(message: String) {
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
    }
    protected open fun <T : ViewModel?> getApplicationScopeViewModel(modelClass: Class<T>): T {
        if (mApplicationProvider == null) {
            mApplicationProvider = ViewModelProvider(
                getBaseActivity().getMediaApplication(), getApplicationFactory(getBaseActivity()))
        }
        return mApplicationProvider?.get(modelClass)?:throw Exception()
    }

    private fun getApplicationFactory(activity: Activity): ViewModelProvider.Factory {
        checkActivity(this)
        val application = checkApplication(activity)
        return ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }

    private fun checkActivity(fragment: Fragment) {
       fragment.activity ?: throw java.lang.IllegalStateException("Can't create ViewModelProvider for detached fragment")
    }

    private fun checkApplication(activity: Activity): Application {
        return activity.application
            ?: throw IllegalStateException("Your activity/fragment is not yet attached to "
                    + "Application. You can't request ViewModel before onCreate call.")
    }
    //TODO tip 1: DataBinding 严格模式（详见 DataBindingFragment - - - - - ）：
    // 将 DataBinding 实例限制于 base 页面中，默认不向子类暴露，
    // 通过这样的方式，来彻底解决 视图实例 null 安全的一致性问题，
    // 如此，视图实例 null 安全的安全性将和基于函数式编程思想的 Jetpack Compose 持平。
    // 如果这样说还不理解的话，详见 https://xiaozhuanlan.com/topic/9816742350 和 https://xiaozhuanlan.com/topic/2356748910
    //TODO tip 2: Jetpack 通过 "工厂模式" 来实现 ViewModel 的作用域可控，
    //目前我们在项目中提供了 Application、Activity、Fragment 三个级别的作用域，
    //值得注意的是，通过不同作用域的 Provider 获得的 ViewModel 实例不是同一个，
    //所以如果 ViewModel 对状态信息的保留不符合预期，可以从这个角度出发去排查 是否眼前的 ViewModel 实例不是目标实例所致。
    //如果这样说还不理解的话，详见 https://xiaozhuanlan.com/topic/6257931840
    protected open fun <T : ViewModel?> getFragmentScopeViewModel(modelClass: Class<T>): T {
        if (mFragmentProvider == null) {
            mFragmentProvider = ViewModelProvider(this)
        }
        mFragmentProvider?.let {
            return it.get(modelClass)
        } ?: throw Exception()
    }

}