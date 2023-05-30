//
//  FileUploaderService.swift
//  VydiaRNFileUploader
//
//  Created by Michael Czerniakowski on 19/05/2023.
//  Copyright © 2023 Marc Shilling. All rights reserved.
//

import Foundation
import Photos
import MobileCoreServices
//WIP: move to other place or external file.

enum UploadError: Error {
    case fileDeletionFailed
    case directoryCreationFailed
    case dataSavingFailed
}

enum NetworkError: Error {
    case invalidURL
    case multipartDataSaveFailure // move it to other case? it is not strictly networkerror... maybe connect both enums to one?
}

@available(iOS 12, *)
@objc(FileUploaderService)
@objcMembers
public class FileUploaderService: RCTEventEmitter, URLSessionDelegate {
    
    @objc public static var emitter: RCTEventEmitter?
    
    var filesMap: [String: URL] = [:]
    var _responsesData: [Int: NSMutableData] = [:]
    var urlSession: URLSession? = nil
    var fileManager: FileManager
    static var uploadId: Int = 0
    static let BACKGROUND_SESSION_ID: String = "ReactNativeBackgroundUpload"
    private static let synchronizationQueue = DispatchQueue(label: "com.example.synchronization")
    
    private static func getNextUploadId() -> Int {
        defer {
            uploadId += 1
        }
        return uploadId
    }
    
    public override init() { //WIP!!!
        self.fileManager = FileManager()
        super.init()
        FileUploaderService.emitter = self
    }
    
    override public static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    public override func sendEvent(withName eventName: String, body: Any?) {
        if let emitter = FileUploaderService.emitter {
            emitter.sendEvent(withName: eventName, body: body)
        }
    }
    
    @objc open override func supportedEvents() -> [String] {
        return [
            "RNFileUploader-progress",
            "RNFileUploader-error",
            "RNFileUploader-cancelled",
            "RNFileUploader-completed"
        ]
    }
    
    /*
     Gets file information for the path specified.  Example valid path is: file:///var/mobile/Containers/Data/Application/3C8A0EFB-A316-45C0-A30A-761BF8CCF2F8/tmp/trim.A5F76017-14E9-4890-907E-36A045AF9436.MOV
     Returns an object such as: {mimeType: "video/quicktime", size: 2569900, exists: true, name: "trim.AF9A9225-FC37-416B-A25B-4EDB8275A625.MOV", extension: "MOV"}
     */
    
    //MARK: - React Native Bridge - getFileInfo
    
    public func getFileInfo(_ path: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        if let escapedPath = path.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
           let fileUrl = URL(string: escapedPath) {
            
            let pathWithoutProtocol = fileUrl.path
            let name = fileUrl.lastPathComponent
            let fileExtension = fileUrl.pathExtension
            
            let exists = FileManager.default.fileExists(atPath: pathWithoutProtocol)
            
            var params: [String: Any] = ["name": name, "extension": fileExtension, "exists": exists]
            
            if exists {
                params["mimeType"] = name.guessMimeType()
                
                if let attributes = try? FileManager.default.attributesOfItem(atPath: pathWithoutProtocol),
                   let fileSize = attributes[.size] as? UInt64 {
                    params["size"] = fileSize
                }
            }
            
            resolve(params)
        } else {
            reject("RN Uploader", "Invalid file path", nil)
        }
    }
    
    /*
     Utility method to copy a PHAsset file into a local temp file, which can then be uploaded.
     */
    func copyAssetToFile(assetUrl: String, completionHandler: @escaping (String?, Error?) -> Void) {
        guard let url = URL(string: assetUrl) else {
            let details = [NSLocalizedDescriptionKey: "Invalid asset URL"]
            let error = NSError(domain: "RNUploader", code: 0, userInfo: details)
            completionHandler(nil, error)
            return
        }
        
        let fetchResult = PHAsset.fetchAssets(withALAssetURLs: [url], options: nil)
        
        guard let asset = fetchResult.lastObject else {
            let details = [NSLocalizedDescriptionKey: "Asset could not be fetched. Are you missing permissions?"]
            let error = NSError(domain: "RNUploader", code: 5, userInfo: details)
            completionHandler(nil, error)
            return
        }
        
        guard let assetResource = PHAssetResource.assetResources(for: asset).first else {
            let details = [NSLocalizedDescriptionKey: "Failed to retrieve asset resource"]
            let error = NSError(domain: "RNUploader", code: 0, userInfo: details)
            completionHandler(nil, error)
            return
        }
        
        let pathToWrite = NSTemporaryDirectory().appending(UUID().uuidString)
        let pathURL = URL(fileURLWithPath: pathToWrite)
        let fileURI = pathURL.absoluteString
        
        let options = PHAssetResourceRequestOptions()
        options.isNetworkAccessAllowed = true /// check availability for iOS 12 to 15
        
        PHAssetResourceManager.default().writeData(for: assetResource,
                                                   toFile: pathURL,
                                                   options: options) { error in
            if let error = error {
                completionHandler(nil, error)
            } else {
                completionHandler(fileURI, nil)
            }
        }
    }
    
    func saveMultipartUploadDataToDisk(uploadId: String, data: Data) throws -> URL? {
        let paths = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask)
        guard let cacheDirectory = paths.first else {
            throw UploadError.directoryCreationFailed
        }
        
        let path = "\(uploadId).multipart"
        let uploaderDirectory = cacheDirectory.appendingPathComponent("uploader", isDirectory: true)
        let filePath = uploaderDirectory.appendingPathComponent(path)
        
        print("Path to save: \(filePath.path)")
        
        // Remove file if needed
        if fileManager.fileExists(atPath: filePath.path) {
            do {
                try fileManager.removeItem(at: filePath)
            } catch {
                print("Cannot delete file at path: \(filePath.path). Error: \(error.localizedDescription)")
                throw UploadError.fileDeletionFailed
            }
        }
        
        // Create directory if needed
        if !fileManager.fileExists(atPath: uploaderDirectory.path) {
            do {
                try fileManager.createDirectory(at: uploaderDirectory, withIntermediateDirectories: false, attributes: nil)
            } catch {
                print("Cannot save data at path \(filePath.path). Error: \(error.localizedDescription)")
                throw UploadError.directoryCreationFailed
            }
        }
        
        // Save Data to file
        do {
            try data.write(to: filePath, options: .atomic)
        } catch {
            print("Cannot save data at path \(filePath.path). Error: \(error.localizedDescription)")
            throw UploadError.dataSavingFailed
        }
        
        return filePath
    }
    
    func removeFilesForUpload(_ uploadId: String) {
        
        if let fileURL = filesMap[uploadId] {
            do {
                try FileManager.default.removeItem(at: fileURL)
            } catch {
                print("Cannot delete file at path \(fileURL.absoluteString). Error: \(error.localizedDescription)")
            }
            filesMap.removeValue(forKey: uploadId)
        }
    }
    
    func urlSession(groupId: String) -> URLSession {
        if urlSession == nil {
            let sessionConfiguration = URLSessionConfiguration.background(withIdentifier: FileUploaderService.BACKGROUND_SESSION_ID) //check if this is ok.
            if !groupId.isEmpty {
                sessionConfiguration.sharedContainerIdentifier = groupId
            }
            
            urlSession = URLSession(configuration: sessionConfiguration, delegate: self, delegateQueue: nil)
        }
        
        return urlSession! //remove force unwrap?
    }
    
    /*
     * Starts a file upload.
     * Options are passed in as the first argument as a js hash:
     * {
     *   url: string.  url to post to.
     *   path: string.  path to the file on the device
     *   headers: hash of name/value header pairs
     * }
     *
     * Returns a promise with the string ID of the upload.
     */
    
    //MARK: - React Native Bridge - startUpload
    
    public func startUpload(_ options: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        
        let thisUploadId: Int = FileUploaderService.getNextUploadId()
        
        do {
            let jsonData = try JSONSerialization.data(withJSONObject: options, options: [])
            let uploadOptions = try JSONDecoder().decode(UploadOptions.self, from: jsonData)
            
            var fileURI = uploadOptions.path
            let method = uploadOptions.method ?? "POST"
            let uploadType = uploadOptions.type ?? "raw"
            let fieldName = uploadOptions.field
            let customUploadId = uploadOptions.customUploadId
            let appGroup = uploadOptions.appGroup
            let headers = uploadOptions.headers
            let parameters = uploadOptions.parameters
            
            guard let uploadUrl = URL(string: uploadOptions.url) else {
                return reject("RN Uploader", "URL not compliant with RFC 2396", nil)
            }
            
            var request = URLRequest(url: uploadUrl)
            request.httpMethod = method
            
            if let headers = headers {
                headers.forEach { (key, value) in
                    request.setValue(value, forHTTPHeaderField: key)
                }
            }
            
            if let newFileURI = fileURI {
                if newFileURI.hasPrefix("assets-library") {
                    let group = DispatchGroup()
                    group.enter()
                    copyAssetToFile(assetUrl: newFileURI) { tempFileUrl, error in
                        if let error = error {
                            group.leave()
                            return reject("RN Uploader", "Asset could not be copied to temp file.", nil)
                        }
                        fileURI = tempFileUrl
                        group.leave()
                    }
                    group.wait()
                }
                
                var uploadTask: URLSessionUploadTask?
                let taskDescription = customUploadId ?? "\(FileUploaderService.uploadId)"
                
                if uploadType == "multipart" {
                    let uuidStr = UUID().uuidString
                    request.setValue("multipart/form-data; boundary=\(uuidStr)", forHTTPHeaderField: "Content-Type")
                    
                    if let httpBody = createBody(withBoundary: uuidStr,
                                                 path: newFileURI,
                                                 parameters: parameters!,
                                                 fieldName: fieldName!) {
                        let contentLength = "\(httpBody.count)"
                        request.setValue(contentLength, forHTTPHeaderField: "Content-Length")
                        
                        do {
                            if let fileUrlOnDisk = try saveMultipartUploadDataToDisk(uploadId: taskDescription, data: httpBody) {
                                filesMap[taskDescription] = fileUrlOnDisk
                                uploadTask = urlSession(groupId: appGroup!).uploadTask(with: request, fromFile: fileUrlOnDisk)
                            } else {
                                throw NetworkError.multipartDataSaveFailure
                            }
                        } catch NetworkError.multipartDataSaveFailure {
                            NSLog("Cannot save multipart data file to disk. Falling back to the old method with stream.")
                            request.httpBodyStream = InputStream(data: httpBody)
                            uploadTask = urlSession(groupId: appGroup!).uploadTask(withStreamedRequest: request)
                        } catch let error {
                            NSLog("Error: \(error)")
                        }
                    }
                } else {
                    if let parameters = parameters, !parameters.isEmpty {
                        return reject("RN Uploader", "Parameters are supported only in multipart type", nil)
                    }
                    let fileUrl = URL(string: newFileURI)
                    uploadTask = urlSession(groupId: appGroup!).uploadTask(with: request, fromFile: fileUrl!)
                }
                
                uploadTask?.taskDescription = taskDescription
                uploadTask?.resume()
                resolve(uploadTask?.taskDescription)
            }
        } catch {
            reject("RN Uploader", "Error decoding options", error)
        }
    }
    
    /*
     * Cancels file upload
     * Accepts upload ID as a first argument, this upload will be cancelled
     * Event "cancelled" will be fired when upload is cancelled.
     */
    
    //MARK: - React Native Bridge - cancelUpload
    
    public func cancelUpload(_ cancelUploadId: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        guard let urlSession = urlSession else {
            // Handle the case when urlSession is nil, e.g., by calling the reject block
            reject("URLSessionError", "URLSession is nil", nil)
            return
        }
        
        urlSession.getTasksWithCompletionHandler { [weak self] (dataTasks, uploadTasks, downloadTasks) in
            guard let strongSelf = self else {
                return
            }
            
            for uploadTask in uploadTasks {
                if uploadTask.taskDescription == cancelUploadId {
                    uploadTask.cancel()
                    strongSelf.removeFilesForUpload(cancelUploadId)
                }
            }
            
            resolve(true)
        }
    }
    
    func createBody(withBoundary boundary: String, path: String, parameters: [String: String], fieldName: String) -> Data? {
        guard var components = URLComponents(string: path) else {
            return nil
        }
        
        components.percentEncodedQuery = nil
        guard let fileURL = components.url else {
            return nil
        }
        
        var httpBody = Data()
        
        do {
            let data = try Data(contentsOf: fileURL, options: .mappedIfSafe)
            let filename = (path as NSString).lastPathComponent
            let mimetype = path.guessMimeType()
            
            appendFormData(to: &httpBody,
                           withBoundary: boundary,
                           parameters: parameters)
            
            appendFileData(to: &httpBody,
                           withBoundary: boundary,
                           fieldName: fieldName,
                           filename: filename,
                           mimetype: mimetype,
                           data: data)
            
            return httpBody
        } catch {
            return nil
        }
    }
    
    func appendFormData(to httpBody: inout Data, withBoundary boundary: String, parameters: [String: String]) {
        for (parameterKey, parameterValue) in parameters {
            httpBody.append("--\(boundary)\r\n".data(using: .utf8)!)
            httpBody.append("Content-Disposition: form-data; name=\"\(parameterKey)\"\r\n\r\n".data(using: .utf8)!)
            httpBody.append("\(parameterValue)\r\n".data(using: .utf8)!)
        }
    }
    
    func appendFileData(to httpBody: inout Data, withBoundary boundary: String, fieldName: String, filename: String, mimetype: String, data: Data) {
        httpBody.append("--\(boundary)\r\n".data(using: .utf8)!)
        httpBody.append("Content-Disposition: form-data; name=\"\(fieldName)\"; filename=\"\(filename)\"\r\n".data(using: .utf8)!)
        httpBody.append("Content-Type: \(mimetype)\r\n\r\n".data(using: .utf8)!)
        httpBody.append(data)
        httpBody.append("\r\n".data(using: .utf8)!)
        httpBody.append("--\(boundary)--\r\n".data(using: .utf8)!)
    }
    
    // MARK: - URLSessionDelegate
    
    func urlSession(_ session: URLSession,
                    task: URLSessionTask,
                    didCompleteWithError error: Error?) {
        
        guard let taskDescription = task.taskDescription else {
            return
        }
        
        var data: [String: Any] = ["id": taskDescription]
        
        if let uploadTask = task as? URLSessionDataTask,
           let response = uploadTask.response as? HTTPURLResponse {
            data["responseCode"] = response.statusCode
        }
        
        if let responseData = _responsesData.removeValue(forKey: task.taskIdentifier),
           let responseString = String(data: responseData as Data, encoding: .utf8) {
            data["responseBody"] = responseString
        } else {
            data["responseBody"] = NSNull()
        }
        
        removeFilesForUpload(taskDescription)
        
        if let error = error {
            data["error"] = error.localizedDescription
            
            let eventName = (error as NSError).code == NSURLErrorCancelled ? "RNFileUploader-cancelled" : "RNFileUploader-error"
                        sendEvent(withName: eventName, body: data)
        } else {
                        sendEvent(withName: "RNFileUploader-completed", body: data)
        }
    }
    
    func urlSession(_ session: URLSession, task: URLSessionTask, didSendBodyData bytesSent: Int64, totalBytesSent: Int64, totalBytesExpectedToSend: Int64) {
        var progress: Float = -1
        
        if totalBytesExpectedToSend > 0 {
            progress = Float(totalBytesSent) / Float(totalBytesExpectedToSend) * 100.0
        }
        
        let bodyData: [String: Any] = [
            "id": task.taskDescription ?? "",
            "progress": progress
        ]
        
        sendEvent(withName: "RNFileUploader-progress", body: bodyData)
    }
    
    func urlSession(_ session: URLSession, dataTask: URLSessionDataTask, didReceive data: Data) {
        guard data.count > 0 else {
            return
        }
        
        // Hold returned data so it can be picked up by the didCompleteWithError method later
        if var responseData = _responsesData[dataTask.taskIdentifier] {
            responseData.append(data)
        } else {
            let responseData = NSMutableData(data: data)
            _responsesData[dataTask.taskIdentifier] = responseData
        }
    }
    
    func urlSession(_ session: URLSession, task: URLSessionTask, needNewBodyStream completionHandler: @escaping (InputStream?) -> Void) {
        let inputStream = task.originalRequest?.httpBodyStream
        
        completionHandler(inputStream)
    }
    
}

//WIP: move to external file?

import UniformTypeIdentifiers

extension String {
    public func guessMimeType() -> String {
        if #available(iOS 14.0, *) {
            if let fileURL = URL(string: self),
               let uti = UTType(filenameExtension: fileURL.pathExtension),
               let mimeType = uti.preferredMIMEType {
                return mimeType
            } else {
                return "application/octet-stream"
            }
        } else {
            let url = NSURL(fileURLWithPath: self)
            let pathExtension = url.pathExtension
            if let uti = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, pathExtension! as NSString, nil)?.takeRetainedValue() {
                if let mimetype = UTTypeCopyPreferredTagWithClass(uti, kUTTagClassMIMEType)?.takeRetainedValue() {
                    return mimetype as String
                }
            }
            return "application/octet-stream"
        }
    }
}

//WIP: move to external file?

struct UploadOptions: Codable {
    let url: String
    let path: String?
    let method: String?
    let type: String?
    let field: String?
    let customUploadId: String?
    let appGroup: String?
    let headers: [String: String]?
    let parameters: [String: String]?
}

// reference methods that created method above

//extension String {
//    public func guessMimeType() -> String {
//        let url = NSURL(fileURLWithPath: self)
//        let pathExtension = url.pathExtension
//
//        if let uti = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, pathExtension! as NSString, nil)?.takeRetainedValue() {
//            if let mimetype = UTTypeCopyPreferredTagWithClass(uti, kUTTagClassMIMEType)?.takeRetainedValue() {
//                return mimetype as String
//            }
//        }
//        return "application/octet-stream"
//    }
//}
//
//@available(iOS 14, *)
//extension NSString {
//    public func mimeType() -> String {
//        if let mimeType = UTType(filenameExtension: self.pathExtension)?.preferredMIMEType {
//            return mimeType
//        }
//        else {
//            return "application/octet-stream"
//        }
//    }
//}
//
//@available(iOS 14, *)
//extension String {
//    public func mimeType() -> String {
//        return (self as NSString).mimeType()
//    }
//}
