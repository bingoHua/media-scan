package com.wt.cloudmedia.player

import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.Toast
import cn.jzvd.JZUtils
import cn.jzvd.JzvdStd
import com.wt.cloudmedia.R
import kotlin.math.abs

class JzvdStd2(context: Context, attrs: AttributeSet) : JzvdStd(context, attrs) {

    private val touchSlop = ViewConfiguration.get(jzvdContext).scaledTouchSlop

    init {
        gestureDetector = GestureDetector(getContext().applicationContext, object : SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                val totalTimeDuration = duration

                var seek = if (e.x > width / 2) {
                    currentPositionWhenPlaying + 10 * 1000
                } else {
                    currentPositionWhenPlaying - 10 * 1000
                }
                if (seek > totalTimeDuration) seek = totalTimeDuration
                if (seek < 0) seek = 0
                mediaInterface.seekTo(seek)
                return super.onDoubleTap(e)
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (!mChangePosition && !mChangeVolume) {
                    onClickUiToggle()
                }
                return super.onSingleTapConfirmed(e)
            }

            override fun onLongPress(e: MotionEvent) {
                super.onLongPress(e)
            }
        })
    }

    override fun gotoNormalScreen() { //goback本质上是goto
        gobakFullscreenTime = System.currentTimeMillis() //退出全屏
        val vg = JZUtils.scanForActivity(jzvdContext).window.decorView as ViewGroup
        vg.removeView(this)
        //        CONTAINER_LIST.getLast().removeAllViews();
        CONTAINER_LIST.last.addView(this, blockIndex, blockLayoutParams)
        CONTAINER_LIST.pop()
        setScreenNormal() //这块可以放到jzvd中
        JZUtils.showStatusBar(jzvdContext)
        JZUtils.setRequestedOrientation(jzvdContext, NORMAL_ORIENTATION)
        JZUtils.showSystemUI(jzvdContext)
    }

    override fun clickPoster() {
        if (jzDataSource == null || jzDataSource.urlsMap.isEmpty() || jzDataSource.currentUrl == null) {
            Toast.makeText(jzvdContext, resources.getString(R.string.no_url), Toast.LENGTH_SHORT).show()
            return
        }
        if (state == STATE_NORMAL) {
            if (!jzDataSource.currentUrl.toString().startsWith("file") &&
                !jzDataSource.currentUrl.toString().startsWith("/") &&
                !JZUtils.isWifiConnected(jzvdContext) && !WIFI_TIP_DIALOG_SHOWED
            ) {
                showWifiDialog()
                return
            }
            startVideo()
        } else if (state == STATE_AUTO_COMPLETE) {
            onClickUiToggle()
        }
    }

    override fun gotoFullscreen() {
        gotoFullscreenTime = System.currentTimeMillis()
        var vg = parent as ViewGroup
        jzvdContext = vg.context
        blockLayoutParams = layoutParams
        blockIndex = vg.indexOfChild(this)
        blockWidth = width
        blockHeight = height

        vg.removeView(this)
        cloneAJzvd(vg)
        CONTAINER_LIST.add(vg)
        vg = JZUtils.scanForActivity(jzvdContext).window.decorView as ViewGroup

        val fullLayout: ViewGroup.LayoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        vg.addView(this, fullLayout)

        setScreenFullscreen()
        JZUtils.hideStatusBar(jzvdContext)
        JZUtils.setRequestedOrientation(jzvdContext, FULLSCREEN_ORIENTATION)
        JZUtils.hideSystemUI(jzvdContext) //华为手机和有虚拟键的手机全屏时可隐藏虚拟键 issue:1326
    }

    override fun touchActionMove(x: Float, y: Float) {
        Log.i(TAG, "onTouch surfaceContainer actionMove [" + this.hashCode() + "] ")
        val deltaX = x - mDownX
        var deltaY = y - mDownY
        val absDeltaX = abs(deltaX)
        val absDeltaY = abs(deltaY)
        if (screen == SCREEN_FULLSCREEN) {
            //拖动的是NavigationBar和状态栏
            if (mDownX > JZUtils.getScreenWidth(context) || mDownY < JZUtils.getStatusBarHeight(context)) {
                return
            }
            if (!mChangePosition && !mChangeVolume && !mChangeBrightness) {
                if (absDeltaX > touchSlop || absDeltaY > touchSlop) {
                    cancelProgressTimer()
                    if (absDeltaX >= touchSlop) {
                        // 全屏模式下的CURRENT_STATE_ERROR状态下,不响应进度拖动事件.
                        // 否则会因为mediaplayer的状态非法导致App Crash
                        if (state != STATE_ERROR) {
                            mChangePosition = true
                            mGestureDownPosition = currentPositionWhenPlaying
                        }
                    } else {
                        //如果y轴滑动距离超过设置的处理范围，那么进行滑动事件处理
                        if (mDownX < mScreenHeight * 0.5f) { //左侧改变亮度
                            mChangeBrightness = true
                            val lp = JZUtils.getWindow(context).attributes
                            if (lp.screenBrightness < 0) {
                                try {
                                    mGestureDownBrightness = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS).toFloat()
                                    Log.i(TAG, "current system brightness: $mGestureDownBrightness")
                                } catch (e: SettingNotFoundException) {
                                    e.printStackTrace()
                                }
                            } else {
                                mGestureDownBrightness = lp.screenBrightness * 255
                                Log.i(TAG, "current activity brightness: $mGestureDownBrightness")
                            }
                        } else { //右侧改变声音
                            mChangeVolume = true
                            mGestureDownVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                        }
                    }
                }
            }
        }
        if (mChangePosition) {
            val totalTimeDuration = duration
            if (PROGRESS_DRAG_RATE <= 0) {
                Log.d(TAG, "error PROGRESS_DRAG_RATE value")
                PROGRESS_DRAG_RATE = 1f
            }
            mSeekTimePosition = (mGestureDownPosition + deltaX * totalTimeDuration / (mScreenWidth * PROGRESS_DRAG_RATE)).toLong()
            if (mSeekTimePosition > totalTimeDuration) mSeekTimePosition = totalTimeDuration
            val seekTime = JZUtils.stringForTime(mSeekTimePosition)
            val totalTime = JZUtils.stringForTime(totalTimeDuration)
            showProgressDialog(deltaX, seekTime, mSeekTimePosition, totalTime, totalTimeDuration)
        }
        if (mChangeVolume) {
            deltaY = -deltaY
            val max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val deltaV = (max * deltaY * 3 / mScreenHeight).toInt()
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mGestureDownVolume + deltaV, 0)
            //dialog中显示百分比
            val volumePercent = (mGestureDownVolume * 100 / max + deltaY * 3 * 100 / mScreenHeight).toInt()
            showVolumeDialog(-deltaY, volumePercent)
        }

        if (mChangeBrightness) {
            deltaY = -deltaY
            val deltaV = (255 * deltaY * 3 / mScreenHeight).toInt()
            val params = JZUtils.getWindow(context).attributes
            if ((mGestureDownBrightness + deltaV) / 255 >= 1) { //这和声音有区别，必须自己过滤一下负值
                params.screenBrightness = 1f
            } else if ((mGestureDownBrightness + deltaV) / 255 <= 0) {
                params.screenBrightness = 0.01f
            } else {
                params.screenBrightness = (mGestureDownBrightness + deltaV) / 255
            }
            JZUtils.getWindow(context).attributes = params
            //dialog中显示百分比
            val brightnessPercent = (mGestureDownBrightness * 100 / 255 + deltaY * 3 * 100 / mScreenHeight).toInt()
            showBrightnessDialog(brightnessPercent)
//                        mDownY = y;
        }
    }
}