package com.vydia.RNUploader

const val mockUrl = "https://example.com/upload"
const val mockFilePath = "/path/to/file"
val mockHeaders = mapOf(
    Pair("Content-Type", "multipart/form-data"),
    Pair("Authorization","token")
)
const val mockMethod = "POST"
const val mockMaxRetries = 3
const val mockUploadId = "1234"
const val mockRequestFieldName = "file"
const val mockRequestType = "multipart"
val mockedRequestParams = mapOf(
    Pair("uploadStartTimestamp","1677495316380"),
    Pair("mediaSize","4657931"),
    Pair("mediaDuration","10"),
    Pair("audioChannel","true"),
    Pair("networkType","{\"details\":{\"isConnectionExpensive\":false,\"subnet\":\"255.255.255.255\",\"ipAddress\":\"10.0.2.16\",\"frequency\":2447,\"strength\":99,\"bssid\":\"02:00:00:00:00:00\"},\"isConnected\":true,\"type\":\"wifi\",\"isInternetReachable\":true,\"isWifiEnabled\":true}"),
    Pair("video","{\"name\":\"63cfb32875689a001ac1e7e7\",\"uri\":\"file:///data/user/0/cache/56aaf5e6-a6ea-4c0b-9c52-32ce24add65c.mp4\",\"type\":\"undefined\"}"),
    Pair("items","[{\"item\":\"63cfb32875689a001ac1e7e7\"}]")
)