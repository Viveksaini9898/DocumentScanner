
package com.document.scanner.providers

import android.content.Context

import javax.inject.Inject


interface StringProvider {

    fun getString(id: Int, vararg args: Any): String

    fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any): String

}


internal class StringProviderImpl @Inject constructor(
     private val applicationContext: Context
) : StringProvider {


    override fun getString(id: Int, vararg args: Any): String {
        return applicationContext.getString(id, *args)
    }


    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any): String {
        return applicationContext.resources.getQuantityString(id, quantity, *formatArgs)
    }


}