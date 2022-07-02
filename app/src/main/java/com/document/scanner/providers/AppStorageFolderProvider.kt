
package com.document.scanner.providers

import android.os.Environment
import java.io.File
import javax.inject.Inject


interface AppStorageFolderProvider {

    fun getAppStorageFolder(): File

}


internal class AppStorageFolderProviderImpl @Inject constructor() : AppStorageFolderProvider {


    private companion object {

        private const val APP_STORAGE_FOLDER_NAME = "DocSkanner"

    }


    override fun getAppStorageFolder(): File {
        val docsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val docsFolderPath = docsFolder.absolutePath
        val appStorageFolderPath = (docsFolderPath + File.separator + APP_STORAGE_FOLDER_NAME)
        val appStorageFolder = File(appStorageFolderPath)

        return appStorageFolder
    }


}