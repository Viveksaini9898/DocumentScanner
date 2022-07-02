package com.document.scanner.extension

import android.content.Context
import android.util.Log

class GlobalContextWrapper {
    companion object {

        private var appContext: Context? = null

        @Synchronized
        fun bindContext(context: Context?) {
            if (context != null) {
                val var1 = context.applicationContext
                appContext = var1 ?: context
            }
        }

        @Synchronized
        fun unbindContext(context: Context?) {
            if (context != null) {
                val var1 = context.applicationContext
                if (appContext === var1) {
                    appContext = null
                }
            }
        }

        @get:Synchronized
        val context: Context?
            get() {
                if (appContext == null) {
                    Log.d("@Arun", ": null")
                }
                return appContext
            }
    }
}
