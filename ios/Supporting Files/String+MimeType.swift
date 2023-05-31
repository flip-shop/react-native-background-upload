//
//  String+MimeType.swift
//  VydiaRNFileUploader
//
//  Created by Michael Czerniakowski on 31/05/2023.
//  Copyright © 2023 Marc Shilling. All rights reserved.
//

import Foundation
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
