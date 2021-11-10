package com.document.scanner.view


import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager
import android.content.*
import android.util.AttributeSet
import android.util.Log
import java.lang.IllegalArgumentException

class CustomViewPager : ViewPager {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        try {
            return super.onTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, Log.getStackTraceString(e))
        }
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        try {
            return super.onInterceptTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, Log.getStackTraceString(e))
        }
        return false
    }

    companion object {
        private val TAG = CustomViewPager::class.java.simpleName
    }
}