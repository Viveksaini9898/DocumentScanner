
package com.document.scanner.imageloading.di

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import com.squareup.picasso.LruCache
import androidx.core.content.getSystemService
import com.google.android.datatransport.runtime.dagger.Module
import com.google.android.datatransport.runtime.dagger.Provides
import com.squareup.picasso.Picasso

import javax.inject.Singleton

@Module
internal object ImageLoadingModule {


    @Singleton
    @Provides
    fun providePicasso(context: Context): Picasso {
        val activityManager = context.getSystemService<ActivityManager>()
        // ~5% of the available heap
        val cacheSizeInBytes = (1024 * 1024 * activityManager?.memoryClass!! / 20)

        return Picasso.Builder(context)
            .defaultBitmapConfig(Bitmap.Config.ARGB_8888)
            .memoryCache(LruCache(cacheSizeInBytes))
            .build()
    }


}