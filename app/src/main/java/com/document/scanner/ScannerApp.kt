package com.document.scanner

import android.app.Application
import com.document.scanner.utils.MyDatabase

class ScannerApp : Application() {
        val database by lazy { MyDatabase.geDatabase(this) }
}