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

@objc(FileUploaderService)
@objcMembers
public class FileUploaderService: NSObject {
    
    var _filesMap: [String: URL] = [:]
    var _responsesData: [Int: NSMutableData] = [:]
    
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

    // MARK: - URLSessionDelegate

    func urlSession(_ session: URLSession, task: URLSessionTask, didCompleteWithError error: Error?) {
        var data: [String: Any] = ["id": task.taskDescription ?? ""]
        if let uploadTask = task as? URLSessionDataTask,
           let response = uploadTask.response as? HTTPURLResponse {
            data["responseCode"] = response.statusCode
        }
        
        // Add data that was collected earlier by the didReceiveData method
        if let responseData = _responsesData[task.taskIdentifier] {
            if let responseString = String(data: responseData as Data, encoding: .utf8) {
                data["responseBody"] = responseString
            } else {
                data["responseBody"] = NSNull()
            }
            _responsesData.removeValue(forKey: task.taskIdentifier)
        }
        
        removeFilesForUpload(task.taskDescription ?? "")
        
        if let error = error {
            data["error"] = error.localizedDescription
            
            if (error as NSError).code == NSURLErrorCancelled {
//                _sendEvent(withName: "RNFileUploader-cancelled", body: data)
            } else {
//                _sendEvent(withName: "RNFileUploader-error", body: data)
            }
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
    
//    func isEqual(_ object: Any?) -> Bool {
//        //
//    }
//
//    var hash: Int = 0
//
//    var superclass: AnyClass?
//
//    func `self`() -> Self {
//        //
//    }
//
//    func perform(_ aSelector: Selector!) -> Unmanaged<AnyObject>! {
//        //
//    }
//
//    func perform(_ aSelector: Selector!, with object: Any!) -> Unmanaged<AnyObject>! {
//        //
//    }
//
//    func perform(_ aSelector: Selector!, with object1: Any!, with object2: Any!) -> Unmanaged<AnyObject>! {
//        //
//    }
//
//    func isProxy() -> Bool {
//        //
//    }
//
//    func isKind(of aClass: AnyClass) -> Bool {
//        //
//    }
//
//    func isMember(of aClass: AnyClass) -> Bool {
//        //
//    }
//
//    func conforms(to aProtocol: Protocol) -> Bool {
//        //
//    }
//
//    func responds(to aSelector: Selector!) -> Bool {
//        //
//    }
//
//    var description: String = ""
    
    
}
