
package com.document.scanner.providers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject


interface DispatcherProvider {

    val main: CoroutineDispatcher

    val io: CoroutineDispatcher

    val computation: CoroutineDispatcher

}


internal class DispatcherProviderImpl @Inject constructor() : DispatcherProvider {

    override val main: CoroutineDispatcher = Dispatchers.Main

    override val io: CoroutineDispatcher = Dispatchers.IO

    override val computation: CoroutineDispatcher = Dispatchers.Default

}