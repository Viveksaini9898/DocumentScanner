package com.document.scanner.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    foreignKeys = [ForeignKey(
        entity = Document::class,
        childColumns = ["docId"],
        parentColumns = ["id"],
        onDelete = CASCADE
    )]
)
class Frame(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(index = true)
    var docId: String,
    var index: Int,
    var timeInMillis: Long,
    var angle: Int,
    var name: String? = null,
    var note: String? = null,
    var ocr: String? = null,
    var uri: String,
    var editedUri: String? = null,
    var croppedUri: String? = null
) : Serializable