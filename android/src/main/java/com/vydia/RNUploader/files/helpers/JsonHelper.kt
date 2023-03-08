package com.vydia.RNUploader.files.helpers

import com.google.gson.Gson

fun serializeToJson(objectToSerialize: Any): String {
    val gson = Gson()
    return gson.toJson(objectToSerialize)
}

inline fun <reified Type> deserializeFromJson(jsonString: String): Type   {
    val gson = Gson()

    return gson.fromJson(jsonString, Type::class.java)
}
