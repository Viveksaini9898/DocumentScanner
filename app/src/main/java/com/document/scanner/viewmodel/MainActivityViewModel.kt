package com.document.scanner.viewmodel


import android.app.Application
import androidx.lifecycle.*
import com.document.scanner.data.Document
import com.document.scanner.repository.DocumentRepository
import com.document.scanner.repository.FrameRepository
import com.document.scanner.task.backGroundThread

import kotlinx.coroutines.launch

class MainActivityViewModel: BaseViewModel() {

    private val documentRepository = DocumentRepository(context)
    private val frameRepository = FrameRepository(context)


    private var documents: LiveData<MutableList<Document>>? = null

    fun getAllDocuments(): LiveData<MutableList<Document>>? {
        if (documents == null) {
            viewModelScope.launch {
                documents = documentRepository?.getAllDocuments()
            }
        }
        return documents
    }

    fun getPageCount(docId: String): LiveData<Int> {
        lateinit var count: LiveData<Int>
        viewModelScope.launch {
            count = frameRepository.getFrameCount(docId) ?: MutableLiveData(0)
        }
        return count
    }

    fun getFirstFrameImagePath(docId: String): LiveData<String>? {
        return frameRepository.getFrameUri(docId)
    }

    fun deleteDocument(docId: String){
        backGroundThread {
            documentRepository.delete(docId)
        }
    }
}
