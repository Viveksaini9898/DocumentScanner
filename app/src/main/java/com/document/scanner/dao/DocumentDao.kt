package com.document.scanner.dao


import androidx.lifecycle.LiveData
import androidx.room.*
import com.document.scanner.data.Document

@Dao
interface DocumentDao {
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
    suspend fun getDocumentSync(docId: String): Document

    @Query("SELECT name FROM Document WHERE id=:docId")
    suspend fun getDocumentName(docId: String): String

    @Query("SELECT * FROM Document ORDER BY dateTime DESC")
    fun getAllDocuments(): LiveData<MutableList<Document>>

    @Query("SELECT * FROM Document WHERE name LIKE :query ORDER BY dateTime DESC")
    suspend fun search(query: String): MutableList<Document>
}