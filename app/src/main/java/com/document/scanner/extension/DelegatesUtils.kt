package com.document.scanner.extension

import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty

fun <T> observeChanges(
    initialValue: T,
    onChange: (oldValue: T, newValue: T) -> Unit
): ReadWriteProperty<Any, T> {
    return Delegates.observable(initialValue) { _, oldValue, newValue ->
        onChange(oldValue, newValue)
    }
}