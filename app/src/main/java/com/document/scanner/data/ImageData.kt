package com.document.scanner.data

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import java.util.ArrayList


class ImageData (private val context: Context?) {

    fun getAll(): MutableList<ImageModel> {

        val list = mutableListOf<ImageModel>()

        var cursor: Cursor?=null
        try {
            cursor = context?.contentResolver?.query(Media.EXTERNAL_CONTENT_URI, null, null, null, null)

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val idIndex = cursor.getColumnIndex(Media._ID)
                    val titleIndex = cursor.getColumnIndex(Media.TITLE)
                    val displayIndex = cursor.getColumnIndex(Media.DISPLAY_NAME)
                    val albumIndex = cursor.getColumnIndex(Media.BUCKET_DISPLAY_NAME)
                    val dateAddedIndex = cursor.getColumnIndex(Media.DATE_ADDED)
                    val sizeIndex = cursor.getColumnIndex(Media.SIZE)
                    val typeIndex = cursor.getColumnIndex(Media.MIME_TYPE)
                    do {
                        val holder = ImageModel(
                            cursor.getLong(idIndex),
                            cursor.getString(titleIndex),
                            cursor.getString(displayIndex),
                            cursor.getString(albumIndex),
                            cursor.getString(typeIndex),
                            cursor.getLong(dateAddedIndex) * 1000,
                            cursor.getLong(sizeIndex),
                            Uri.parse(Media.EXTERNAL_CONTENT_URI.toString() + "/" + cursor.getInt(
                                idIndex)))
                        list.add(holder)
                    } while (cursor.moveToNext())
                }
                cursor?.close()
            }
        }catch (e:Exception){

        }finally {
            cursor?.close()
        }
        return list
    }

    fun getAllImages(): MutableList<String> {
        val uris: MutableList<String> = ArrayList()
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        context?.contentResolver?.query(
            Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            Media.DATE_ADDED + " DESC"
        )?.let { cursor ->
            while (cursor.moveToNext()) {
                val absolutePathOfImage =
                    cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
                uris.add(absolutePathOfImage)
            }
            cursor.close()
        }
        return uris
    }
}
