package com.document.scanner.repository

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import com.document.scanner.dao.DocumentDao
import com.document.scanner.dao.FrameDao
import com.document.scanner.data.Document

class DocumentRepository(application: Context?){

    private val documentDao = DocumentDao.getInstance(application!!)

    fun insert(document: Document){
        documentDao.insert(document)
    }

    fun getDocument(docId: String?): LiveData<Document> {
        return documentDao.getDocument(docId!!)
    }

    fun getAllDocuments(): LiveData<MutableList<Document>> {
       return documentDao.getAllDocuments()
    }

    fun delete(docId: String?) {
        documentDao.delete(docId!!)
    }

    suspend fun getDocumentSync(docId: String?): Document {
        return documentDao.getDocumentSync(docId!!)
    }

    fun update(document: Document) {
        documentDao.update(document)
    }

    suspend fun getDocumentName(docId: String?): String {
        return documentDao.getDocumentName(docId!!)
    }
}