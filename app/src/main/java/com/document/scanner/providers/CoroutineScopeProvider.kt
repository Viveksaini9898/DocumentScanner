

package com.document.scanner.providers

import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


interface CoroutineScopeProvider {

    fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job

    fun <T> async(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> T
    ): Deferred<T>

    fun cancelChildren()

}


internal class CoroutineScopeProviderImpl @Inject constructor() : CoroutineScopeProvider {


    private val scope = CoroutineScope(SupervisorJob())


    override fun launch(
        context: CoroutineContext,
        start: CoroutineStart,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return scope.launch(context, start, block)
    }


    override fun <T> async(
        context: CoroutineContext,
        start: CoroutineStart,
        block: suspend CoroutineScope.() -> T
    ): Deferred<T> {
        return scope.async(context, start, block)
    }


    override fun cancelChildren() {
        scope.coroutineContext.cancelChildren()
    }


}