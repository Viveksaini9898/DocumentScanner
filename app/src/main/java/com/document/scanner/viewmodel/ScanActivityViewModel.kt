package com.document.scanner.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.util.Pair
import androidx.lifecycle.*
import com.document.scanner.activity.ListFramesActivity
import com.document.scanner.activity.ViewPageActivity
import com.document.scanner.constants.INTENT_DOCUMENT_ID
import com.document.scanner.constants.INTENT_FRAME_POSITION
import com.document.scanner.data.Document

import com.document.scanner.data.Frame
import com.document.scanner.repository.DocumentRepository
import com.document.scanner.repository.FrameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScanActivityViewModel:BaseViewModel() {

    private val frameRepository = FrameRepository(context)
    private val documentRepository = DocumentRepository(context)
    private var newDocument = true
    var docId: String? = null
    var count: LiveData<Int> = MutableLiveData(0)

    private val paths: MutableList<Pair<String, String>> = ArrayList()

    fun addPath(sourceUri: String, croppedUri: String) {
        paths.add(Pair.create(sourceUri, croppedUri))
    }

    fun pathsCount(): Int {
        return paths.size
    }

    fun getPageCount(docId: String): LiveData<Int> {
        viewModelScope.launch {
            count = frameRepository?.getFrameCount(docId) ?: MutableLiveData(0)
        }
        newDocument = false
        this.docId = docId
        return count
    }

    fun capture(name: String, angle: Int, count: Int, activity: Activity) {
        viewModelScope.launch(Dispatchers.Default) {
            if (newDocument) {
                val doc = Document()
                docId = doc.id
                doc.name = name
                doc.dateTime = System.currentTimeMillis()
                documentRepository?.insert(doc)
            }
            for (i in paths.indices) {
                val path = paths[i]
                val frame = Frame(
                    timeInMillis = System.currentTimeMillis(),
                    index = count + i,
                    angle = angle,
                    docId = docId!!,
                    uri = path.first,
                    croppedUri = path.second
                )
                frameRepository?.insert(frame)
            }
            if (!activity.isDestroyed) {
                val i = Intent(activity, ListFramesActivity::class.java)
                i.putExtra(INTENT_DOCUMENT_ID, docId)
                i.putExtra(INTENT_FRAME_POSITION,-1)
                activity.startActivity(i)
                activity.finish()
            }
        }
    }
}
