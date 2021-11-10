package com.document.scanner.utils


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.document.scanner.dao.DocumentDao
import com.document.scanner.dao.FrameDao
import com.document.scanner.data.Document
import com.wonderscan.android.data.Frame


@Database(entities = [Document::class, Frame::class], version = 5, exportSchema = false)
abstract class MyDatabase : RoomDatabase() {
    abstract fun frameDao(): FrameDao
    abstract fun documentDao(): DocumentDao

    companion object {
        private var INSTANCE: MyDatabase? = null

        fun geDatabase(context: Context): MyDatabase? {
            synchronized(Database::class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        MyDatabase::class.java,
                        "document_scanner_db"
                    ).fallbackToDestructiveMigration().build()
                }
            }
            return INSTANCE
        }
    }
}