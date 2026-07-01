package com.wallpaperhub.app.util

import android.view.WindowManager

/**
 * 截屏防护 - R18 页面专用
 * 设置 FLAG_SECURE 阻止截屏和录屏
 */
object ScreenshotBlocker {

    /**
     * 在 Activity/Window 上设置 FLAG_SECURE
     * 效果: 截屏显示黑屏,录屏显示黑屏, 最近任务列表模糊
     */
    fun enable(window: android.view.Window) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    /**
     * 移除 FLAG_SECURE
     * 仅在离开 R18 页面时调用
     */
    fun disable(window: android.view.Window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}
