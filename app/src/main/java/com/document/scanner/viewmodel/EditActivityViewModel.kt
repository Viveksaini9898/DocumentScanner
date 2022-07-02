
package com.document.scanner.viewmodel

import androidx.lifecycle.LiveData
import com.document.scanner.data.Frame
import com.document.scanner.repository.DocumentRepository
import com.document.scanner.repository.FrameRepository


class EditActivityViewModel: BaseViewModel() {

    private val frameRepository = FrameRepository(context)
    private val documentRepository = DocumentRepository(context)


    fun getFrame(frameId: Long): LiveData<Frame>? {
        return frameRepository.getFrame(frameId)
    }

}