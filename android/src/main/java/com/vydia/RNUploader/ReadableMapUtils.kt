package com.vydia.RNUploader

import com.facebook.react.bridge.ReadableMap

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