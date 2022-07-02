/*
 * Copyright (C) 2021 Dev Sebastian
 * This file is part of WonderScan <https://github.com/devsebastian/WonderScan>.
 *
 * WonderScan is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WonderScan is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WonderScan.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.document.scanner.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.*
import com.document.scanner.R
import com.document.scanner.dao.FrameDao
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
import java.text.SimpleDateFormat
import java.util.*


class ListFrameActivityViewModel : BaseViewModel() {
    private val frameRepository = FrameRepository(context)
    private val documentRepository = DocumentRepository(context)
    var document: Document = Document()
    private val frameDao = FrameDao.getInstance(context!!)
    var frames: LiveData<MutableList<Frame>> = frameRepository.getFrames(document.id)


    var count: LiveData<Int> = MutableLiveData(0)
    fun document(docId : String): LiveData<Document> = documentRepository.getDocument(docId)
    fun frames(docId : String): LiveData<MutableList<Frame>> = frameRepository.getFrames(docId)

    fun processUnprocessedFrames(docId: String) {
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

    fun update(frames: MutableList<Frame>) {
        backGroundThread {
            frames.forEach {
                frameRepository.update(it)
            }
        }
    }

    fun delete(docId: String) {
        backGroundThread {
            documentRepository.delete(docId)
        }
    }

    suspend fun getDocument(docId: String): Document {
        return documentRepository.getDocumentSync(docId)
    }

    fun updateDocument(document: Document) {
        backGroundThread {
            documentRepository.update(document)
        }
    }

    fun sendCreateFileIntent(docId: String,type: String?, resultLauncher: ActivityResultLauncher<Intent>) {
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

    fun exportPdf(docId: String,uri: Uri) {
        backGroundThread {
            val pdf = ExportPdf.exportPdf(frameRepository.getFramesSync(docId))
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

    private fun getName(): String {
        val simpleDateFormat =
            SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.getDefault())
        return context?.getString(R.string.app_name) + " " + simpleDateFormat.format(Date())
    }

    fun setup(paths: MutableList<String>) {
        val docName: String = getName()
        document.name = docName
        document.dateTime = System.currentTimeMillis()
        backGroundThread {
            documentRepository.insert(document)
            getFramesFromImagePaths(paths).let { frames ->
                for (frame in frames) {
                    viewModelScope.launch(Dispatchers.Default) {
                        cropAndFormat(frame, context, frameRepository)
                    }
                }
            }
        }
    }

    private fun getFramesFromImagePaths(
        paths: MutableList<String>,
    ): MutableList<Frame> {
        val frames = ArrayList<Frame>()
        for (i in paths.indices) {
            val sourcePath = paths[i]
            val frame = Frame(
                timeInMillis = System.currentTimeMillis(),
                index = i,
                docId = document.id,
                uri = sourcePath,
                angle = 0
            )
            frame.id = frameDao.insert(frame)
            frames.add(frame)
        }
        return frames
    }
}

