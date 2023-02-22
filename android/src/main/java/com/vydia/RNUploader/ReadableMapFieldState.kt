package com.vydia.RNUploader

sealed class ReadableMapFieldState {
    object Correct: ReadableMapFieldState()
    object WrongType: ReadableMapFieldState()
    object NotExists: ReadableMapFieldState()
}
