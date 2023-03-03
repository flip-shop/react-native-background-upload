package com.vydia.RNUploader.helpers


import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType


/**
 * Function using to obtain field from [ReadableMap] with specific type
 * Returns [ReadableMapFieldState] depends on that field exists and type is as required
 */
fun obtainFieldState(
    map: ReadableMap,
    fieldNameKey: String,
    requiredFieldType: ReadableType
): ReadableMapFieldState {
    return if(map.hasKey(fieldNameKey)) {
        if(map.getType(fieldNameKey) != requiredFieldType) {
            ReadableMapFieldState.WrongType
        }else {
            ReadableMapFieldState.Correct
        }
    }else {
        ReadableMapFieldState.NotExists
    }
}
