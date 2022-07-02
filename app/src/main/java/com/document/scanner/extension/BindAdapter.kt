package com.document.scanner.extension

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@BindingAdapter("changeVisibility")
fun View?.visibility(value:Boolean){
    this?.isVisible=value
}


@BindingAdapter("loadImage")
fun ImageView.loadImage(path: String?){
    Glide.with(context).load(path).into(this)
}

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T
): Lazy<T> {
    return lazy(LazyThreadSafetyMode.NONE) {
        bindingInflater(layoutInflater)
    }
}

fun <T : ViewBinding> Fragment.viewBinding(
    viewBindingFactory: (LayoutInflater) -> T
): FragmentViewBindingDelegate<T> {
    return FragmentViewBindingDelegate(this, viewBindingFactory)
}

class FragmentViewBindingDelegate<T : ViewBinding>(
    private val fragment: Fragment,
    private val viewBindingFactory: (LayoutInflater) -> T
) : ReadOnlyProperty<Fragment, T> {


    private var binding: T? = null


    init {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onDestroy(owner: LifecycleOwner) {
                binding = null
            }

        })
    }
    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        if(binding != null) {
            return checkNotNull(binding)
        }

        val lifecycle = fragment.viewLifecycleOwner.lifecycle

        if(!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException("Could not retrieve a view binding when the fragment is not initialized.")
        }

        return viewBindingFactory(thisRef.layoutInflater)
            .also { binding = it }
    }
}