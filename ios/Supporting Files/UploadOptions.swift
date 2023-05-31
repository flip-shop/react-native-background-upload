//
//  UploadOptions.swift
//  VydiaRNFileUploader
//
//  Created by Michael Czerniakowski on 31/05/2023.
//  Copyright © 2023 Marc Shilling. All rights reserved.
//

import Foundation

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
