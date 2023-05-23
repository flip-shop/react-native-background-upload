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
}

@available(iOS 12, *)
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
    
    /*
     Borrowed from http://stackoverflow.com/questions/2439020/wheres-the-iphone-mime-type-database
    */

//    func guessMIMETypeFromFileName(fileName: String) -> String {
//        let UTI = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, fileName.pathExtension as CFString, nil)
//        let MIMEType = UTTypeCopyPreferredTagWithClass(UTI!.takeRetainedValue(), kUTTagClassMIMEType)
//
//        if UTI != nil {
//            UTI!.release()
//        }
//
//        if MIMEType == nil {
//            return "application/octet-stream"
//        }
//
//        return MIMEType!.takeRetainedValue() as String
//    }
    
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
    
    func createBody(withBoundary boundary: String, path: String, parameters: [String: String], fieldName: String) -> Result<Data, Error> {
        guard var components = URLComponents(string: path) else {
            return .failure(NetworkError.invalidURL)
        }
        
        components.percentEncodedQuery = nil
        guard let fileURL = components.url else {
            return .failure(NetworkError.invalidURL)
        }
        
        var httpBody = Data()
        
        do {
            let data = try Data(contentsOf: fileURL, options: .mappedIfSafe)
            let filename = (path as NSString).lastPathComponent
//            let mimetype = guessMIMETypeFromFileName(path)
            let mimetype = ""
            
            appendFormData(to: &httpBody, withBoundary: boundary, parameters: parameters)
            
            httpBody.append("--\(boundary)\r\n".data(using: .utf8)!)
            httpBody.append("Content-Disposition: form-data; name=\"\(fieldName)\"; filename=\"\(filename)\"\r\n".data(using: .utf8)!)
            httpBody.append("Content-Type: \(mimetype)\r\n\r\n".data(using: .utf8)!)
            httpBody.append(data)
            httpBody.append("\r\n".data(using: .utf8)!)
            
            httpBody.append("--\(boundary)--\r\n".data(using: .utf8)!)
            
            return .success(httpBody)
        } catch {
            return .failure(error)
        }
    }

    func appendFormData(to httpBody: inout Data, withBoundary boundary: String, parameters: [String: String]) {
        for (parameterKey, parameterValue) in parameters {
            httpBody.append("--\(boundary)\r\n".data(using: .utf8)!)
            httpBody.append("Content-Disposition: form-data; name=\"\(parameterKey)\"\r\n\r\n".data(using: .utf8)!)
            httpBody.append("\(parameterValue)\r\n".data(using: .utf8)!)
        }
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


extension URL {
    var mime: String {
        guard
            let uti = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, pathExtension as CFString, nil)
        else { return "" }
        let mime = uti.takeRetainedValue() as String
        uti.release()
        return mime
    }
}

//extension String {
    
//    var guessMIMETypeFromFileName: String {
//        let UTI = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, pathExtension as CFString, nil)
//        let MIMEType = UTTypeCopyPreferredTagWithClass(UTI!.takeRetainedValue(), kUTTagClassMIMEType)
//
//        if UTI != nil {
//            UTI!.release()
//        }
//
//        if MIMEType == nil {
//            return "application/octet-stream"
//        }
//
//        return MIMEType!.takeRetainedValue() as String
//    }
//
//}

//import UniformTypeIdentifiers
//
//@available(iOS 14, *)
//extension NSURL {
//    public func mimeType() -> String {
//        if let pathExt = self.pathExtension,
//            let mimeType = UTType(filenameExtension: pathExt)?.preferredMIMEType {
//            return mimeType
//        }
//        else {
//            return "application/octet-stream"
//        }
//    }
//}
//
//@available(iOS 14, *)
//extension URL {
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
//@available(iOS 14, *)
//extension String {
//    public func mimeType() -> String {
//        return (self as NSString).mimeType()
//    }
//}
