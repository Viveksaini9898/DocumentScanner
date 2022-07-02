package com.document.scanner.task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

fun <T: Any>backGroundThread(work: suspend (() -> T?)){

    CoroutineScope(Dispatchers.IO).launch {
        work()
    }
}
fun uiThread(callback: () -> Unit){
    CoroutineScope(Dispatchers.Main).launch {
        MainScope()
        callback()
    }
}

inline fun <reified V : ViewDataBinding> ViewGroup.toBinding(): V {
    return V::class.java.getMethod(
        "inflate",
        LayoutInflater::class.java,
        ViewGroup::class.java,
        Boolean::class.java
    ).invoke(null, LayoutInflater.from(context), this, false) as V
}