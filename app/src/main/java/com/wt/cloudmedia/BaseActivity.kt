package com.wt.cloudmedia

import androidx.appcompat.app.AppCompatActivity
import com.wt.cloudmedia.CloudMediaApplication

open class BaseActivity: AppCompatActivity() {

    fun getMediaApplication():CloudMediaApplication {
        return application as CloudMediaApplication
    }

}