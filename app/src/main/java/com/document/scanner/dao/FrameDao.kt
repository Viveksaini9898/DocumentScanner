package com.document.scanner.dao

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.document.scanner.data.Frame
import com.document.scanner.utils.MyDatabase

@Dao
interface FrameDao {


    companion object {
        private var INSTANCE: FrameDao? = null
        fun getInstance(context: Context): FrameDao {
            return INSTANCE ?: Room
                .databaseBuilder(context.applicationContext, MyDatabase::class.java, "document_scanner_db")
                .addMigrations(object : Migration(1, 2) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        database.execSQL("ALTER TABLE Frame ADD COLUMN name TEXT")
                    }
                }).allowMainThreadQueries()
                .build()
                .frameDao().apply {
                    INSTANCE = this
                }
        }
    }


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(frame: Frame): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(frame: Frame)

    @Delete
    fun delete(frame: Frame)

    @Query("SELECT * FROM Frame WHERE docId=:docId ORDER BY `index`")
    fun getFrames(docId: String): LiveData<MutableList<Frame>>

    @Query("SELECT * FROM Frame WHERE id=:id")
    fun getFrame(id: Long): LiveData<Frame>

    @Query("SELECT * FROM Frame WHERE docId=:docId ORDER BY `index`")
    fun getFramesSync(docId: String): MutableList<Frame>

    @Query("SELECT COUNT(id) FROM Frame WHERE docId=:docId")
    fun getFrameCount(docId: String): LiveData<Int>

    @Query("SELECT uri FROM Frame WHERE docId=:docId AND `index`=0")
    fun getFrameUri(docId: String): LiveData<String>
}