package com.document.scanner.task

import android.os.AsyncTask

abstract class BaseAsyncTask(private val listener: ProgressListener) : AsyncTask<Void, Void, Unit?>() {

    interface ProgressListener {
        // callback for start
        fun onStarted()

        // callback on success
        fun onCompleted()

        // callback on error
        fun onError(errorMessage: Unit?)

    }

    override fun onPreExecute() {
        listener.onStarted()

    }

    override fun onPostExecute(errorMessage: Unit?) {
        super.onPostExecute(errorMessage)
        if (null != errorMessage) {
            listener.onError(errorMessage)
        } else {
            listener.onCompleted()
        }
    }
}