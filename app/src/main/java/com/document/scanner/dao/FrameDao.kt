package com.document.scanner.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.wonderscan.android.data.Frame

@Dao
interface FrameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(frame: Frame): Long

    @Update
    fun update(frame: Frame)

    @Delete
    fun delete(frame: Frame)

    @Query("SELECT * FROM Frame WHERE docId=:docId ORDER BY `index`")
    fun getFrames(docId: String): LiveData<MutableList<Frame>>

    @Query("SELECT * FROM Frame WHERE id=:id")
    fun getFrame(id: Long): LiveData<Frame>

    @Query("SELECT * FROM Frame WHERE docId=:docId ORDER BY `index`")
    suspend fun getFramesSync(docId: String): MutableList<Frame>

    @Query("SELECT COUNT(id) FROM Frame WHERE docId=:docId")
    fun getFrameCount(docId: String): LiveData<Int>

    @Query("SELECT uri FROM Frame WHERE docId=:docId AND `index`=0")
    fun getFrameUri(docId: String): LiveData<String>
}