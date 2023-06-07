//
//  VydiaRNFileUploader.swift
//  VydiaRNFileUploader
//
//  Created by Michael Czerniakowski on 19/05/2023.
//  Copyright © 2023 Marc Shilling. All rights reserved.
//

import Foundation
import Photos
import MobileCoreServices

@available(iOS 12, *)
@objc(VydiaRNFileUploader)
@objcMembers
public class VydiaRNFileUploader: RCTEventEmitter {
    
    @objc public static var emitter: RCTEventEmitter?
    
    var filesMap: [String: URL] = [:]
    var responsesData: [Int: NSMutableData] = [:]
    var urlSession: URLSession?
    var fileManager: FileManager
    static var uploadId: Int = 0
    static let BACKGROUND_SESSION_ID: String = "ReactNativeBackgroundUpload"
    private static let synchronizationQueue = DispatchQueue(label: "com.example.synchronization")
    
    @objc override init() {
        self.fileManager = FileManager.default
        super.init()
        VydiaRNFileUploader.emitter = self
    }
    
    override public static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    override public func supportedEvents() -> [String] {
        return [
            "RNFileUploader-progress",
            "RNFileUploader-error",
            "RNFileUploader-cancelled",
            "RNFileUploader-completed"
        ]
    }
    
    override public func sendEvent(withName eventName: String, body: Any?) {
        if let emitter = VydiaRNFileUploader.emitter {
            //            emitter.sendEvent(withName: eventName, body: body)
            print("VNRF: event sent: \(eventName)")
        } else {
            print("VNRF: event emitter not initialized!!!!)")
        }
    }
    
    //    private static func sendSomeEvent(withName eventName: String, body: Any?) {
    //        if let emitter = self.emitter {
    //            emitter.sendEvent(withName: eventName, body: body)
    //            print("VNRF: event sent: \(eventName)")
    //        } else {
    //            print("VNRF: event emitter not initialized!!!!)")
    //        }
    //    }
    
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
            
            let exists = fileManager.fileExists(atPath: pathWithoutProtocol)
            
            var params: [String: Any] = ["name": name, "extension": fileExtension, "exists": exists]
            
            if exists {
                params["mimeType"] = name.guessMimeType()
                
                if let attributes = try? fileManager.attributesOfItem(atPath: pathWithoutProtocol),
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
        
        let thisUploadId: Int = VydiaRNFileUploader.getNextUploadId()
        
        do {
            let jsonData = try JSONSerialization.data(withJSONObject: options, options: [])
            let uploadOptions = try JSONDecoder().decode(UploadOptions.self, from: jsonData)
            
            print("VNRF: uploadOptions are  \(uploadOptions)")
            print("VNRF: fileURI will be \(uploadOptions.path)")
            
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
                print("VNRF: - newfileuri - \(newFileURI)")
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
                let taskDescription = customUploadId ?? "\(VydiaRNFileUploader.uploadId)"
                
                guard let urlSession = urlSession(groupId: appGroup) else {
                    throw UploadError.urlSessionCreationFailure
                }
                
                if uploadType == "multipart" {
                    let uuidStr = UUID().uuidString
                    request.setValue("multipart/form-data; boundary=\(uuidStr)", forHTTPHeaderField: "Content-Type")
                    
                    print("VNRF: multipart request is: \(request)")
                    
                    if let httpBody = createBody(withBoundary: uuidStr,
                                                 path: newFileURI,
                                                 parameters: parameters!,
                                                 fieldName: fieldName!) {
                        let contentLength = "\(httpBody.count)"
                        request.setValue(contentLength, forHTTPHeaderField: "Content-Length")
                        
                        do {
                            if let fileUrlOnDisk = try saveMultipartUploadDataToDisk(uploadId: taskDescription, data: httpBody) {
                                filesMap[taskDescription] = fileUrlOnDisk
                                uploadTask = urlSession.uploadTask(with: request, fromFile: fileUrlOnDisk)
                            } else {
                                throw UploadError.multipartDataSaveFailure
                            }
                        } catch UploadError.multipartDataSaveFailure {
                            NSLog("VNRF: Cannot save multipart data file to disk. Falling back to the old method with stream.")
                            request.httpBodyStream = InputStream(data: httpBody)
                            uploadTask = urlSession.uploadTask(withStreamedRequest: request)
                        } catch let error {
                            NSLog("VNRF: Error: \(error)")
                        }
                    }
                } else {
                    if let parameters = parameters, !parameters.isEmpty {
                        return reject("RN Uploader", "Parameters are supported only in multipart type", nil)
                    }
                    
                    let fileUrl = URL(string: newFileURI)
                    uploadTask = urlSession.uploadTask(with: request, fromFile: fileUrl!)
                }
                
                uploadTask?.taskDescription = taskDescription
                uploadTask?.resume()
                resolve(uploadTask?.taskDescription)
            }
        } catch {
            NSLog("VNRF: startUpload error: \(error)")
            reject("RN Uploader", "Error decoding options", error)
        }
    }
    
    private static func getNextUploadId() -> Int {
        defer {
            uploadId += 1
        }
        return uploadId
    }
    
    /*
     Utility method to copy a PHAsset file into a local temp file, which can then be uploaded.
     */
    
    private func copyAssetToFile(assetUrl: String, completionHandler: @escaping (_ tempFileUrl: String?, _ error: Error?) -> Void) {
        guard let url = URL(string: assetUrl) else {
            let details = [NSLocalizedDescriptionKey: "Invalid asset URL"]
            let error = NSError(domain: "RNUploader", code: 0, userInfo: details)
            completionHandler(nil, error)
            return
        }
        
        let fetchResults = PHAsset.fetchAssets(withALAssetURLs: [url], options: nil)
        print("VNRF: fetchResult is \(fetchResults.lastObject)")
        
        guard fetchResults != nil else {
            let details = [NSLocalizedDescriptionKey: "Asset were not fetched. Are you using good URL?"]
            let error = NSError(domain: "RNUploader", code: 0, userInfo: details)
            completionHandler(nil, error)
            return
        }
        
        guard let asset = fetchResults.lastObject else {
            let details = [NSLocalizedDescriptionKey: "Asset could not be fetched. Are you missing permissions?"]
            let error = NSError(domain: "RNUploader", code: 5, userInfo: details)
            completionHandler(nil, error)
            return
        }
        
        guard let assetResource = PHAssetResource.assetResources(for: asset).first else {
            let details = [NSLocalizedDescriptionKey: "Asset resource could not be retrieved"]
            let error = NSError(domain: "RNUploader", code: 0, userInfo: details)
            completionHandler(nil, error)
            return
        }
        
        let pathToWrite = NSTemporaryDirectory().appending(UUID().uuidString)
        let pathUrl = URL(fileURLWithPath: pathToWrite)
        let fileURI = pathUrl.absoluteString
        
        let options = PHAssetResourceRequestOptions()
        options.isNetworkAccessAllowed = true /// check availability for iOS 12 to 15
        
        PHAssetResourceManager.default().writeData(for: assetResource, toFile: pathUrl, options: options) { error in
            if let error = error {
                print("VNRF: asset failed and not copied to \(fileURI)")
                completionHandler(nil, error)
            } else {
                print("VNRF: asset files copied to \(fileURI)")
                completionHandler(fileURI, nil)
            }
        }
    }
    
    private func createBody(withBoundary boundary: String, path: String, parameters: [String: String], fieldName: String) -> Data? {
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
            
            print("VNRF: body generated: httpbody: \(httpBody)")
            return httpBody
        } catch {
            return nil
        }
    }
    
    private func appendFormData(to httpBody: inout Data,
                                withBoundary boundary: String,
                                parameters: [String: String]) {
        for (parameterKey, parameterValue) in parameters {
            httpBody.append("--\(boundary)\r\n".data(using: .utf8)!)
            httpBody.append("Content-Disposition: form-data; name=\"\(parameterKey)\"\r\n\r\n".data(using: .utf8)!)
            httpBody.append("\(parameterValue)\r\n".data(using: .utf8)!)
        }
    }
    
    private func appendFileData(to httpBody: inout Data,
                                withBoundary boundary: String,
                                fieldName: String,
                                filename: String,
                                mimetype: String,
                                data: Data) {
        httpBody.append("--\(boundary)\r\n".data(using: .utf8)!)
        httpBody.append("Content-Disposition: form-data; name=\"\(fieldName)\"; filename=\"\(filename)\"\r\n".data(using: .utf8)!)
        httpBody.append("Content-Type: \(mimetype)\r\n\r\n".data(using: .utf8)!)
        httpBody.append(data)
        httpBody.append("\r\n".data(using: .utf8)!)
        httpBody.append("--\(boundary)--\r\n".data(using: .utf8)!)
    }
    
    private func saveMultipartUploadDataToDisk(uploadId: String, data: Data) throws -> URL? {
        let paths = fileManager.urls(for: .cachesDirectory, in: .userDomainMask)
        guard let cacheDirectory = paths.first else {
            throw UploadError.directoryCreationFailed
        }
        
        let path = "\(uploadId).multipart"
        let uploaderDirectory = cacheDirectory.appendingPathComponent("uploader", isDirectory: true)
        let filePath = uploaderDirectory.appendingPathComponent(path)
        
        print("VNRF: path to save: \(filePath.path)")
        
        // Remove file if needed
        if fileManager.fileExists(atPath: filePath.path) {
            do {
                try fileManager.removeItem(at: filePath)
                print("VNRF: Sucesfully deleted data at path \(filePath.path))")
            } catch {
                print("Cannot delete file at path: \(filePath.path). Error: \(error.localizedDescription)")
                throw UploadError.fileDeletionFailed
            }
        }
        
        // Create directory if needed
        if !fileManager.fileExists(atPath: uploaderDirectory.path) {
            do {
                try fileManager.createDirectory(at: uploaderDirectory, withIntermediateDirectories: false, attributes: nil)
                print("VNRF: Sucesfully created directory at path \(uploaderDirectory.path))")
            } catch {
                print("VNRF: Cannot delete directory at path \(uploaderDirectory.path). Error: \(error.localizedDescription)")
                throw UploadError.directoryCreationFailed
            }
        }
        
        // Save Data to file
        do {
            try data.write(to: filePath, options: .atomic)
            print("VNRF: Sucesfully saved data at path \(filePath.path))")
        } catch {
            print("VNRF: Cannot save data at path \(filePath.path). Error: \(error.localizedDescription)")
            throw UploadError.dataSavingFailed
        }
        
        return filePath
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
    
    private func removeFilesForUpload(_ uploadId: String) {
        
        if let fileURL = filesMap[uploadId] {
            do {
                try fileManager.removeItem(at: fileURL)
            } catch {
                print("VNRF: Cannot delete file at path \(fileURL.absoluteString). Error: \(error.localizedDescription)")
            }
            filesMap.removeValue(forKey: uploadId)
        }
    }
    
}

// MARK: - URLSessionDelegate

extension VydiaRNFileUploader: URLSessionDelegate, URLSessionTaskDelegate {
    
    public func urlSession(_ session: URLSession,
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
        
        if let responseData = responsesData.removeValue(forKey: task.taskIdentifier),
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
    
    func urlSession(groupId: String?) -> URLSession? {
        if urlSession == nil {
            let sessionConfiguration = URLSessionConfiguration.background(withIdentifier: VydiaRNFileUploader.BACKGROUND_SESSION_ID)
            if let groupId = groupId, !groupId.isEmpty {
                sessionConfiguration.sharedContainerIdentifier = groupId
            }
            
            urlSession = URLSession(configuration: sessionConfiguration, delegate: self, delegateQueue: nil)
        }
        
        return urlSession
    }
    
    public func urlSession(_ session: URLSession, task: URLSessionTask, didSendBodyData bytesSent: Int64, totalBytesSent: Int64, totalBytesExpectedToSend: Int64) {
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
        if var responseData = responsesData[dataTask.taskIdentifier] {
            responseData.append(data)
        } else {
            let responseData = NSMutableData(data: data) // maybe move to other line?
            responsesData[dataTask.taskIdentifier] = responseData
        }
    }
    
    public func urlSession(_ session: URLSession, task: URLSessionTask, needNewBodyStream completionHandler: @escaping (InputStream?) -> Void) {
        let inputStream = task.originalRequest?.httpBodyStream
        
        completionHandler(inputStream)
    }
    
}
