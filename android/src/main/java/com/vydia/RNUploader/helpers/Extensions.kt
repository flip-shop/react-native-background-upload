package com.vydia.RNUploader.helpers

import com.facebook.react.bridge.*

fun ReadableMap.toMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    val iterator = this.keySetIterator()
    while (iterator.hasNextKey()) {
        val key = iterator.nextKey()
        val value = this.getString(key)
        map[key] = value ?: ""
    }
    return map
}