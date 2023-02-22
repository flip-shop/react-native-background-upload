package com.vydia.RNUploader


import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType



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
