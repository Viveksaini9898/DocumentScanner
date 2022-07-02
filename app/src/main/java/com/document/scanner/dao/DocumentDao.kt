package com.document.scanner.dao


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.document.scanner.data.Document
import com.document.scanner.utils.MyDatabase

@Dao
interface DocumentDao {

    companion object {
        private var INSTANCE: DocumentDao? = null
        fun getInstance(context: Context): DocumentDao {
            return INSTANCE ?: Room
                .databaseBuilder(context.applicationContext, MyDatabase::class.java, "document_scanner_db")
                .addMigrations(object : Migration(1, 2) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        database.execSQL("ALTER TABLE Document ADD COLUMN name TEXT")
                    }
                }).allowMainThreadQueries()
                .build()
                .documentDao().apply {
                    INSTANCE = this
                }
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(document: Document)

    @Update
    fun update(document: Document)

    @Delete
    fun delete(document: Document)

    @Query("DELETE FROM Document where id=:docId")
    fun delete(docId: String)

    @Query("SELECT * FROM Document WHERE id=:docId")
    fun getDocument(docId: String): LiveData<Document>

    @Query("SELECT * FROM Document WHERE id=:docId")
    fun getDocumentSync(docId: String): Document

    @Query("SELECT name FROM Document WHERE id=:docId")
    fun getDocumentName(docId: String): String

    @Query("SELECT * FROM Document ORDER BY dateTime DESC")
    fun getAllDocuments(): LiveData<MutableList<Document>>

    @Query("SELECT * FROM Document WHERE name LIKE :query ORDER BY dateTime DESC")
    fun search(query: String): MutableList<Document>
}