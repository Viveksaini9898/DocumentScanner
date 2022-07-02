package com.document.scanner.data

import android.net.Uri

data class ImageModel (val id:Long, val title:String?, val fileName:String?, val albumName:String?, val mimeType:String?,
                       val date:Long, val size:Long, var uri:Uri)