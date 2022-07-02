package com.document.scanner.repository

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.document.scanner.dao.FrameDao
import com.document.scanner.data.Frame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FrameRepository(application: Context?) {
    private val frameDao = FrameDao.getInstance(application!!)

    fun insert(frame: Frame) {
        frameDao.insert(frame)
    }

    fun getFrameCount(docId: String): LiveData<Int> {
        return frameDao.getFrameCount(docId)
    }

    fun deleteFrame(frame: Frame) {
        frameDao.delete(frame)
    }

    fun update(frame: Frame) {
        frameDao.update(frame)
    }

    fun delete(frame: Frame) {
        frameDao.delete(frame)
    }

    fun getFrames(docId: String): LiveData<MutableList<Frame>> {
        return frameDao.getFrames(docId)
    }


    fun getFrame(id: Long?): LiveData<Frame>? {
        return frameDao.getFrame(id!!)
    }

    fun getFrameUri(docId: String): LiveData<String>? {
       return frameDao.getFrameUri(docId)
    }

     fun getFramesSync(docId: String): MutableList<Frame> {
        return frameDao.getFramesSync(docId)
    }

}