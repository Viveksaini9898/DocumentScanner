package com.document.scanner.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.document.scanner.R
import com.document.scanner.databinding.LayoutBottomButtonBinding

class BottomButton : FrameLayout {
    private var binding: LayoutBottomButtonBinding? = null

    var text: String
        get() = binding?.textView?.text.toString()
        set(value) {
            binding?.textView?.text = value
        }


    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, -1)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
         binding = LayoutBottomButtonBinding.inflate(LayoutInflater.from(context),this,true)
        context.obtainStyledAttributes(attrs, R.styleable.BottomButton).apply {
            showIcon(this)
            showText(this)
            recycle()
        }
    }

    private fun showIcon(attributes: TypedArray) {
        val iconResId = attributes.getResourceId(R.styleable.BottomButton_icon, -1)
        val icon = AppCompatResources.getDrawable(context, iconResId)
        binding?.imageViewSchema?.setImageDrawable(icon)
    }


    private fun showText(attributes: TypedArray) {
        binding?.textView?.text = attributes.getString(R.styleable.BottomButton_text).orEmpty()
    }
}