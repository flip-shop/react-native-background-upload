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

@objc(FileUploaderService)
@objcMembers
public class FileUploaderService: NSObject, URLSessionDelegate {
    
    var _filesMap: [String: URL] = [:]
    var _responsesData: [Int: NSMutableData] = [:]
    var _urlSession: URLSession? = nil
    let fileManager: FileManager
    static let BACKGROUND_SESSION_ID: String = "ReactNativeBackgroundUpload"
    
    public override init() { //WIP!!!
        self.fileManager = FileManager()
    }
    
    func saveMultipartUploadDataToDisk(uploadId: String, data: Data) throws -> URL {
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
        
        if let fileURL = _filesMap[uploadId] {
            do {
                try FileManager.default.removeItem(at: fileURL)
            } catch {
                print("Cannot delete file at path \(fileURL.absoluteString). Error: \(error.localizedDescription)")
            }
            _filesMap.removeValue(forKey: uploadId)
        }
    }
    
    func urlSession(groupId: String) -> URLSession {
        if _urlSession == nil {
            let sessionConfiguration = URLSessionConfiguration.background(withIdentifier: FileUploaderService.BACKGROUND_SESSION_ID) //check if this is ok.
            if !groupId.isEmpty {
                sessionConfiguration.sharedContainerIdentifier = groupId
            }
            
            _urlSession = URLSession(configuration: sessionConfiguration, delegate: self, delegateQueue: nil)
        }
        
        return _urlSession!
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
            //            _sendEvent(withName: eventName, body: data)
        } else {
            //            _sendEvent(withName: "RNFileUploader-completed", body: data)
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
        
        //_sendEvent(withName: "RNFileUploader-progress", body: bodyData)
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
