package com.document.scanner.data


import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
class Document {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    var name: String? = null
    var dateTime: Long = 0
}