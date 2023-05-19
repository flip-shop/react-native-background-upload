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
