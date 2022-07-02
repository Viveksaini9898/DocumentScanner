package com.document.scanner.viewmodel

import androidx.lifecycle.*
import com.document.scanner.R
import com.document.scanner.dao.FrameDao
import com.document.scanner.data.Document
import com.document.scanner.data.Frame
import com.document.scanner.data.ImageData
import com.document.scanner.repository.DocumentRepository
import com.document.scanner.repository.FrameRepository
import com.document.scanner.utils.Utils.cropAndFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ImageViewModel : BaseViewModel() {

    private val frameRepository = FrameRepository(context)
    private val documentRepository = DocumentRepository(context)


    fun getAllImage(reverse:Boolean=false)  = liveData(Dispatchers.IO){
        val list= ImageData(context).getAll()
        if (reverse){
            list.reverse()
        }
        emit(list)
    }


    val getAllUris = liveData(Dispatchers.IO) {
        val list = ImageData(context).getAllImages()
        emit(list)
    }

}