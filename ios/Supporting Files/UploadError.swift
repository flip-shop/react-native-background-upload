//
//  UploadError.swift
//  VydiaRNFileUploader
//
//  Created by Michael Czerniakowski on 31/05/2023.
//  Copyright © 2023 Marc Shilling. All rights reserved.
//

import Foundation

enum UploadError: Error {
    case fileDeletionFailed
    case directoryCreationFailed
    case dataSavingFailed
    case invalidURL // use or remove
    case multipartDataSaveFailure
}
