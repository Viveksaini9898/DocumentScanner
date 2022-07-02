package com.document.scanner.viewmodel

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.*
import com.document.scanner.data.Document
import com.document.scanner.data.Frame
import com.document.scanner.repository.DocumentRepository
import com.document.scanner.repository.FrameRepository
import com.document.scanner.task.backGroundThread
import com.document.scanner.task.uiThread
import com.document.scanner.utils.ExportPdf
import com.document.scanner.utils.Filter
import com.document.scanner.utils.Utils
import com.document.scanner.utils.Utils.cropAndFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.imgcodecs.Imgcodecs
import java.io.IOException

class ViewPageActivityViewModel:BaseViewModel() {

    private val frameRepository = FrameRepository(context)
    private val documentRepository = DocumentRepository(context)


    var currentIndex = 0

    fun updateFrame(frame: Frame) {
        backGroundThread {
            frameRepository.update(frame)
        }
    }

    fun deleteFrame(frame: Frame) {
        backGroundThread {
            frameRepository.delete(frame)
        }
    }

    fun getDocument(docId: String?): LiveData<Document> {
        return documentRepository.getDocument(docId)
    }

    fun frames(docId: String?): LiveData<MutableList<Frame>> {
        return frameRepository.getFrames(docId!!)
    }

    var count: LiveData<Int> = MutableLiveData(0)

    fun processUnprocessedFrames(docId: String, context: Context?) {
        backGroundThread {
            frameRepository.getFramesSync(docId).let { frames ->
                for (i in frames.indices) {
                    if (frames[i].editedUri == null) {
                        val frame = frames[i]
                        viewModelScope.launch(Dispatchers.Default) {
                            if (frame.croppedUri == null)
                                cropAndFormat(frame, context, frameRepository)
                            else processFrame(context!!, frame)
                        }
                    }
                }
            }
        }
    }

    private fun processFrame(context: Context, frame: Frame) {
        val file = Utils.createPhotoFile(context)
        val mat = Imgcodecs.imread(frame.croppedUri)
        val editedMat = Filter.magicColor(mat)
        Imgcodecs.imwrite(file.absolutePath, editedMat)
        frame.editedUri = file.absolutePath
        frameRepository.update(frame)
        mat.release()
        editedMat.release()
    }

    fun update(frames: List<Frame>) {
        backGroundThread {
            for (i in frames.indices) {
                frameRepository.update(frames[i])
            }
        }
    }

    fun getFrame(frameId: Long): LiveData<Frame>? {
        return frameRepository.getFrame(frameId)
    }

    fun delete(docId: String?) {
        backGroundThread {
            documentRepository.delete(docId)
        }
    }

    suspend fun getDocumentSync(docId: String?): Document {
        return documentRepository.getDocumentSync(docId)
    }

    fun updateDocument(document: Document) {
        backGroundThread {
            documentRepository.update(document)
        }
    }

    fun sendCreateFileIntent(type: String?, resultLauncher: ActivityResultLauncher<Intent>,docId: String?) {
        backGroundThread {
            val name = documentRepository.getDocumentName(docId)
            Intent(Intent.ACTION_CREATE_DOCUMENT).let {
                it.addCategory(Intent.CATEGORY_OPENABLE)
                it.type = type
                it.putExtra(Intent.EXTRA_TITLE, name)
                resultLauncher.launch(it)
            }
        }
    }

    fun exportPdf(uri: Uri,docId: String?) {
        backGroundThread {
            val pdf = ExportPdf.exportPdf(frameRepository.getFramesSync(docId!!))
            try {
                pdf.writeTo(context?.contentResolver?.openOutputStream(uri))
                uiThread {
                    Toast.makeText(
                        context,
                        "PDF document saved in " + uri.path,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


}
