package com.vydia.RNUploader.helpers

sealed class ReadableMapFieldState {
    object Correct: ReadableMapFieldState()
    object WrongType: ReadableMapFieldState()
    object NotExists: ReadableMapFieldState()
}
