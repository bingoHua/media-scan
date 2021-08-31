package com.wt.cloudmedia.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.microsoft.graph.requests.extensions.GraphServiceClient
import com.wt.cloudmedia.CloudMediaApplication
import com.wt.cloudmedia.R
import com.wt.cloudmedia.api.LoginService
import com.wt.cloudmedia.constant.NOTIFICATION_ID_VIDEO_FETCHING
import com.wt.cloudmedia.constant.CHANNEL_VIDEO_FETCH

class VideoFetchService : Service() {
    private var notificationContent = CloudMediaApplication.instance().applicationContext.getString(R.string.video_fetching)
    private val notificationBuilder by lazy {
        val builder = NotificationCompat.Builder(this, CHANNEL_VIDEO_FETCH)
            //.setSmallIcon(R.drawable.ic_download)
            .setOngoing(true)
        .setContentTitle(notificationContent)
        /*  builder.addAction(
              R.drawable.ic_stop_black_24dp,
              getString(R.string.cancel),
              IntentHelp.servicePendingIntent<CacheAudioService2>(this, IntentAction.stop)
          )*/
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    override fun onCreate() {
        super.onCreate()
        upNotification()
        requestMovie()
    }

    private fun requestMovie() {
        val token = LoginService.getAuthenticationResult()
        if (token == null) {
        } else {
            val result = GraphServiceClient.builder().authenticationProvider { request ->
                request.addHeader("Authorization", "Bearer ${token.accessToken}")
            }
            CloudMediaApplication.instance().dataRepository.requestUpdateMovies(result.buildClient())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
     /*   intent?.action?.let {
            when (it) {
                "start" -> {
                    requestMovie()
                }
            }
        }*/
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("ServiceCast")
    private fun upNotification() {
        notificationBuilder.setContentText(notificationContent)
        val manager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val channel = NotificationChannel(CHANNEL_VIDEO_FETCH, notificationContent, NotificationManager.IMPORTANCE_LOW)
        manager.createNotificationChannel(channel)
        val notification = notificationBuilder.build()
        startForeground(NOTIFICATION_ID_VIDEO_FETCHING, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}