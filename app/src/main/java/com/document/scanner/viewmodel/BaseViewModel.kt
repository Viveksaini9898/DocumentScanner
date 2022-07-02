package com.document.scanner.viewmodel

import androidx.lifecycle.ViewModel
import com.document.scanner.extension.GlobalContextWrapper
import kotlinx.coroutines.GlobalScope

abstract class BaseViewModel: ViewModel() {

    val context get() = GlobalContextWrapper.context
}