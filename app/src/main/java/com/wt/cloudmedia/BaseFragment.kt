package com.wt.cloudmedia

import android.widget.Toast
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {

    fun getBaseActivity(): BaseActivity {
        return activity as BaseActivity
    }

    fun getMediaApplication(): CloudMediaApplication {
        return getBaseActivity().getMediaApplication()
    }

    fun showToast(message: String) {
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
    }

}