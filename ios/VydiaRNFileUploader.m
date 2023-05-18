#import <Foundation/Foundation.h>
#import <MobileCoreServices/MobileCoreServices.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTBridgeModule.h>
#import <Photos/Photos.h>

@interface VydiaRNFileUploader : RCTEventEmitter <RCTBridgeModule, NSURLSessionTaskDelegate>
{
  NSMutableDictionary *_responsesData;
  NSMutableDictionary<NSString*, NSURL*> *_filesMap;
}
@end

@implementation VydiaRNFileUploader

RCT_EXPORT_MODULE();

@synthesize bridge = _bridge;
static int uploadId = 0;
static RCTEventEmitter* staticEventEmitter = nil;
static NSString *BACKGROUND_SESSION_ID = @"ReactNativeBackgroundUpload";
NSURLSession *_urlSession = nil;
NSFileManager *fileManager = nil;

+ (BOOL)requiresMainQueueSetup {
    return NO;
}

-(id) init {
  self = [super init];
  if (self) {
    staticEventEmitter = self;
    _responsesData = [NSMutableDictionary dictionary];
    _filesMap = @{}.mutableCopy;
      fileManager = [NSFileManager defaultManager];
  }
  return self;
}

- (void)_sendEventWithName:(NSString *)eventName body:(id)body {
  if (staticEventEmitter == nil)
    return;
  [staticEventEmitter sendEventWithName:eventName body:body];
}

- (NSArray<NSString *> *)supportedEvents {
    return @[
        @"RNFileUploader-progress",
        @"RNFileUploader-error",
        @"RNFileUploader-cancelled",
        @"RNFileUploader-completed"
    ];
}

/*
 Gets file information for the path specified.  Example valid path is: file:///var/mobile/Containers/Data/Application/3C8A0EFB-A316-45C0-A30A-761BF8CCF2F8/tmp/trim.A5F76017-14E9-4890-907E-36A045AF9436.MOV
 Returns an object such as: {mimeType: "video/quicktime", size: 2569900, exists: true, name: "trim.AF9A9225-FC37-416B-A25B-4EDB8275A625.MOV", extension: "MOV"}
 */

RCT_EXPORT_METHOD(getFileInfo:(NSString *)path resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
{
    @try {
        // Escape non-latin characters in the filename
        NSString *escapedPath = [path stringByAddingPercentEncodingWithAllowedCharacters:NSCharacterSet.URLQueryAllowedCharacterSet];
        
        NSURL *fileUri = [NSURL URLWithString:escapedPath];
        
        NSString *pathWithoutProtocol = [fileUri path];
        NSString *name = [fileUri lastPathComponent];
        NSString *extension = [name pathExtension];

        bool exists = [fileManager fileExistsAtPath:pathWithoutProtocol];
        
        NSMutableDictionary *params = [NSMutableDictionary dictionaryWithObject:name forKey:@"name"];
        [params setObject:extension forKey:@"extension"];
        [params setObject:@(exists) forKey:@"exists"];
        
        if (exists) {
            NSString *mimeType = [self guessMIMETypeFromFileName:name];
            [params setObject:mimeType forKey:@"mimeType"];
            
            NSError *error;
            NSDictionary<NSFileAttributeKey, id> *attributes = [fileManager attributesOfItemAtPath:pathWithoutProtocol error:&error];
            
            if (error == nil) {
                unsigned long long fileSize = [attributes fileSize];
                [params setObject:@(fileSize) forKey:@"size"];
            }
        }
        resolve(params);
    }
    
    @catch (NSException *exception) {
        reject(@"RN Uploader", exception.name, nil);
    }
    
}

/*
 Borrowed from http://stackoverflow.com/questions/2439020/wheres-the-iphone-mime-type-database
*/
- (NSString *)guessMIMETypeFromFileName: (NSString *)fileName {
    CFStringRef UTI = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, (__bridge CFStringRef)[fileName pathExtension], NULL);
    CFStringRef MIMEType = UTTypeCopyPreferredTagWithClass(UTI, kUTTagClassMIMEType);
    
    if (UTI) {
        CFRelease(UTI);
    }
  
    if (!MIMEType) {
        return @"application/octet-stream";
    }
    return (__bridge NSString *)(MIMEType);
}

/*
 Utility method to copy a PHAsset file into a local temp file, which can then be uploaded.
 */
- (void)copyAssetToFile: (NSString *)assetUrl completionHandler: (void(^)(NSString *__nullable tempFileUrl, NSError *__nullable error))completionHandler {
    NSURL *url = [NSURL URLWithString:assetUrl];
    PHAsset *asset = [PHAsset fetchAssetsWithALAssetURLs:@[url] options:nil].lastObject;
    if (!asset) {
        NSMutableDictionary* details = [NSMutableDictionary dictionary];
        [details setValue:@"Asset could not be fetched.  Are you missing permissions?" forKey:NSLocalizedDescriptionKey];
        completionHandler(nil,  [NSError errorWithDomain:@"RNUploader" code:5 userInfo:details]);
        return;
    }
    PHAssetResource *assetResource = [[PHAssetResource assetResourcesForAsset:asset] firstObject];
    NSString *pathToWrite = [NSTemporaryDirectory() stringByAppendingPathComponent:[[NSUUID UUID] UUIDString]];
    NSURL *pathUrl = [NSURL fileURLWithPath:pathToWrite];
    NSString *fileURI = pathUrl.absoluteString;

    PHAssetResourceRequestOptions *options = [PHAssetResourceRequestOptions new];
    options.networkAccessAllowed = YES;

    [[PHAssetResourceManager defaultManager] writeDataForAssetResource:assetResource toFile:pathUrl options:options completionHandler:^(NSError * _Nullable e) {
        if (e == nil) {
            completionHandler(fileURI, nil);
        }
        else {
            completionHandler(nil, e);
        }
    }];
}

- (void)POCcopyAssetToFile:(NSString *)assetUrl completionHandler:(void (^)(NSString * _Nullable tempFileUrl, NSError * _Nullable error))completionHandler {
    NSURL *url = [NSURL URLWithString:assetUrl];
    PHAsset *asset = [PHAsset fetchAssetsWithALAssetURLs:@[url] options:nil].lastObject;
    
    if (!asset) {
        NSDictionary *details = @{ NSLocalizedDescriptionKey: @"Asset could not be fetched. Are you missing permissions?" };
        NSError *error = [NSError errorWithDomain:@"RNUploader" code:5 userInfo:details];
        completionHandler(nil, error);
        return;
    }
    
    PHAssetResource *assetResource = [[PHAssetResource assetResourcesForAsset:asset] firstObject];
    NSString *pathToWrite = [NSTemporaryDirectory() stringByAppendingPathComponent:[[NSUUID UUID] UUIDString]];
    NSURL *pathURL = [NSURL fileURLWithPath:pathToWrite];
    NSString *fileURI = pathURL.absoluteString;
    
    PHAssetResourceRequestOptions *options = [PHAssetResourceRequestOptions new];
    options.networkAccessAllowed = YES;
    
    [[PHAssetResourceManager defaultManager] writeDataForAssetResource:assetResource toFile:pathURL options:options completionHandler:^(NSError * _Nullable error) {
        if (error == nil) {
            completionHandler(fileURI, nil);
        } else {
            completionHandler(nil, error);
        }
    }];
}


- (NSURL *)saveMultipartUploadDataToDisk:(NSString *)uploadId data:(NSData *)data {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    NSString *cacheDirectory = paths.firstObject;
    NSString *path = [NSString stringWithFormat:@"%@.multipart", uploadId];

    NSString *uploaderDirectory = [cacheDirectory stringByAppendingPathComponent:@"uploader"];
//    NSString *uploaderDirectory = [cacheDirectory stringByAppendingPathComponent:@"/uploader"];

    NSString *filePath = [uploaderDirectory stringByAppendingPathComponent:path];
    NSLog(@"Path to save: %@", filePath);

    NSError *error = nil;

    // Remove file if needed
    if ([fileManager fileExistsAtPath:filePath]) {
        if (![fileManager removeItemAtPath:filePath error:&error]) {
            NSLog(@"Cannot delete file at path: %@. Error: %@", filePath, error.localizedDescription);
            return nil;
        }
    }

    // Create directory if needed
    if (![fileManager fileExistsAtPath:uploaderDirectory]) {
        if (![fileManager createDirectoryAtPath:uploaderDirectory withIntermediateDirectories:NO attributes:nil error:&error]) {
            NSLog(@"Cannot save data at path %@. Error: %@", filePath, error.localizedDescription);
            return nil;
        }
    }

    // Save NSData to file
    if (![data writeToFile:filePath options:NSDataWritingAtomic error:&error]) {
        NSLog(@"Cannot save data at path %@. Error: %@", filePath, error.localizedDescription);
        return nil;
    }

    return [NSURL fileURLWithPath:filePath];
}


- (void)removeFilesForUpload:(NSString *)uploadId {
    NSError *error = nil;

    NSURL *fileURL = _filesMap[uploadId];

    if (fileURL == nil) {
        return;
    }

    if (![fileManager removeItemAtURL:fileURL error:&error]) {
        NSLog(@"Cannot delete file at path %@. Error: %@", fileURL.absoluteString, error.localizedDescription);
    }

    [_filesMap removeObjectForKey:uploadId];
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
RCT_EXPORT_METHOD(startUpload:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
{
    int thisUploadId;
    @synchronized(self.class)
    {
        thisUploadId = uploadId++;
    }

    NSString *uploadUrl = options[@"url"];
    __block NSString *fileURI = options[@"path"];
    NSString *method = options[@"method"] ?: @"POST";
    NSString *uploadType = options[@"type"] ?: @"raw";
    NSString *fieldName = options[@"field"];
    NSString *customUploadId = options[@"customUploadId"];
    NSString *appGroup = options[@"appGroup"];
    NSDictionary *headers = options[@"headers"];
    NSDictionary *parameters = options[@"parameters"];

    @try {
        NSURL *requestUrl = [NSURL URLWithString: uploadUrl];
        if (requestUrl == nil) {
            return reject(@"RN Uploader", @"URL not compliant with RFC 2396", nil);
        }

        NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:requestUrl];
        [request setHTTPMethod: method];

        [headers enumerateKeysAndObjectsUsingBlock:^(id  _Nonnull key, id  _Nonnull val, BOOL * _Nonnull stop) {
            if ([val respondsToSelector:@selector(stringValue)]) {
                val = [val stringValue];
            }
            if ([val isKindOfClass:[NSString class]]) {
                [request setValue:val forHTTPHeaderField:key];
            }
        }];


        // asset library files have to be copied over to a temp file.  they can't be uploaded directly
        if ([fileURI hasPrefix:@"assets-library"]) {
            dispatch_group_t group = dispatch_group_create();
            dispatch_group_enter(group);
            [self copyAssetToFile:fileURI completionHandler:^(NSString * _Nullable tempFileUrl, NSError * _Nullable error) {
                if (error) {
                    dispatch_group_leave(group);
                    reject(@"RN Uploader", @"Asset could not be copied to temp file.", nil);
                    return;
                }
                fileURI = tempFileUrl;
                dispatch_group_leave(group);
            }];
            dispatch_group_wait(group, DISPATCH_TIME_FOREVER);
        }

        NSURLSessionUploadTask *uploadTask;
        NSString *taskDescription = customUploadId ? customUploadId : [NSString stringWithFormat:@"%i", thisUploadId];

        if ([uploadType isEqualToString:@"multipart"]) {
            NSString *uuidStr = [[NSUUID UUID] UUIDString];
            [request setValue:[NSString stringWithFormat:@"multipart/form-data; boundary=%@", uuidStr] forHTTPHeaderField:@"Content-Type"];

         //   NSInputStream *httpBody = [self createBodyWithBoundary:uuidStr path:fileURI parameters: parameters fieldName:fieldName];
         //   [request setHTTPBodyStream:httpBody];
            
//            NSString *uuidStr = [[NSUUID UUID] UUIDString];
            [request setValue:[NSString stringWithFormat:@"multipart/form-data; boundary=%@", uuidStr] forHTTPHeaderField:@"Content-Type"];

            NSData *httpBody = [self createBodyWithBoundary:uuidStr path:fileURI parameters: parameters fieldName:fieldName];
            [request setHTTPBodyStream: [NSInputStream inputStreamWithData:httpBody]];
            [request setValue:[NSString stringWithFormat:@"%zd", httpBody.length] forHTTPHeaderField:@"Content-Length"];

            NSURL *fileUrlOnDisk = [self saveMultipartUploadDataToDisk:taskDescription data:httpBody];
            if (fileUrlOnDisk) {
              _filesMap[taskDescription] = fileUrlOnDisk;
              uploadTask = [[self urlSession: appGroup] uploadTaskWithRequest:request fromFile:fileUrlOnDisk];
            } else {
              NSLog(@"Cannot save multipart data file to disk, Fallback to old method wtih stream");
              [request setHTTPBodyStream: [NSInputStream inputStreamWithData:httpBody]];
              uploadTask = [[self urlSession: appGroup] uploadTaskWithStreamedRequest:request];
            }
        } else {
            if (parameters.count > 0) {
                reject(@"RN Uploader", @"Parameters supported only in multipart type", nil);
                return;
            }

            uploadTask = [[self urlSession: appGroup] uploadTaskWithRequest:request fromFile:[NSURL URLWithString: fileURI]];
        }

        uploadTask.taskDescription = taskDescription;

        [uploadTask resume];
        resolve(uploadTask.taskDescription);
    }
    @catch (NSException *exception) {
        reject(@"RN Uploader", exception.name, nil);
    }
}

/*
 * Cancels file upload
 * Accepts upload ID as a first argument, this upload will be cancelled
 * Event "cancelled" will be fired when upload is cancelled.
 */
RCT_EXPORT_METHOD(cancelUpload: (NSString *)cancelUploadId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    __weak typeof(self) weakSelf = self;
    
    [_urlSession getTasksWithCompletionHandler:^(NSArray *dataTasks,
                                                 NSArray *uploadTasks,
                                                 NSArray *downloadTasks) {
        __strong typeof(self) strongSelf = weakSelf;
        
        for (NSURLSessionTask *task in uploadTasks) {
            if ([task.taskDescription isEqualToString:cancelUploadId]) {
                [task cancel];
                [strongSelf removeFilesForUpload:cancelUploadId];
            }
        }
        resolve(@(YES));
    }];
}

- (NSData *)createBodyWithBoundary:(NSString *)boundary
                         path:(NSString *)path
                         parameters:(NSDictionary *)parameters
                         fieldName:(NSString *)fieldName {

    NSMutableData *httpBody = [NSMutableData data];

    // Escape non latin characters in filename
    NSString *escapedPath = [path stringByAddingPercentEncodingWithAllowedCharacters: NSCharacterSet.URLQueryAllowedCharacterSet];

    // resolve path
    NSURL *fileUri = [NSURL URLWithString: escapedPath];
    
    NSError* error = nil;
    NSData *data = [NSData dataWithContentsOfURL:fileUri options:NSDataReadingMappedAlways error: &error];

    if (data == nil) {
        NSLog(@"Failed to read file %@", error);
    }

    NSString *filename  = [path lastPathComponent];
    NSString *mimetype  = [self guessMIMETypeFromFileName:path];
    
    /// enumarateKeysAndObjectsUsingBlock is faster than normal for-loop.
    /// https://www.mikeash.com/pyblog/friday-qa-2010-04-09-comparison-of-objective-c-enumeration-techniques.html

    [parameters enumerateKeysAndObjectsUsingBlock:^(NSString *parameterKey, NSString *parameterValue, BOOL *stop) {
        [httpBody appendData:[[NSString stringWithFormat:@"--%@\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
        [httpBody appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"%@\"\r\n\r\n", parameterKey] dataUsingEncoding:NSUTF8StringEncoding]];
        [httpBody appendData:[[NSString stringWithFormat:@"%@\r\n", parameterValue] dataUsingEncoding:NSUTF8StringEncoding]];
    }];

    [httpBody appendData:[[NSString stringWithFormat:@"--%@\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
    [httpBody appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"%@\"; filename=\"%@\"\r\n", fieldName, filename] dataUsingEncoding:NSUTF8StringEncoding]];
    [httpBody appendData:[[NSString stringWithFormat:@"Content-Type: %@\r\n\r\n", mimetype] dataUsingEncoding:NSUTF8StringEncoding]];
    [httpBody appendData:data];
    [httpBody appendData:[@"\r\n" dataUsingEncoding:NSUTF8StringEncoding]];

    [httpBody appendData:[[NSString stringWithFormat:@"--%@--\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];

    return httpBody;
}

//- Proof of concept
//
//- (NSInputStream *)POCcreateBodyWithBoundary:(NSString *)boundary
//                                     path:(NSString *)path
//                               parameters:(NSDictionary *)parameters
//                                fieldName:(NSString *)fieldName {
//
//    NSInputStream *fileStream = [NSInputStream inputStreamWithFileAtPath:path];
//
//    if (!fileStream) return nil;
//
//    [fileStream open];
//
//    // Escape non-latin characters in filename
//    NSString *escapedPath = [path stringByAddingPercentEncodingWithAllowedCharacters: NSCharacterSet.URLQueryAllowedCharacterSet];
//
//    // Resolve path
//    NSURL *fileUri = [NSURL URLWithString:escapedPath];
//
//    NSString *filename  = [path lastPathComponent];
//    NSString *mimetype  = [self guessMIMETypeFromFileName:path];
//
//    // Prepare for multipart form data
//    NSMutableData *httpBodyStart = [NSMutableData data];
//    NSMutableData *httpBodyEnd = [NSMutableData data];
//
//    // Add parameters
//    [parameters enumerateKeysAndObjectsUsingBlock:^(NSString *parameterKey, NSString *parameterValue, BOOL *stop) {
//        [httpBodyStart appendData:[[NSString stringWithFormat:@"--%@\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
//        [httpBodyStart appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"%@\"\r\n\r\n", parameterKey] dataUsingEncoding:NSUTF8StringEncoding]];
//        [httpBodyStart appendData:[[NSString stringWithFormat:@"%@\r\n", parameterValue] dataUsingEncoding:NSUTF8StringEncoding]];
//    }];
//
//    // Add file
//    [httpBodyStart appendData:[[NSString stringWithFormat:@"--%@\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
//    [httpBodyStart appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"%@\"; filename=\"%@\"\r\n", fieldName, filename] dataUsingEncoding:NSUTF8StringEncoding]];
//    [httpBodyStart appendData:[[NSString stringWithFormat:@"Content-Type: %@\r\n\r\n", mimetype] dataUsingEncoding:NSUTF8StringEncoding]];
//
//    // Add end boundary
//    [httpBodyEnd appendData:[@"\r\n" dataUsingEncoding:NSUTF8StringEncoding]];
//    [httpBodyEnd appendData:[[NSString stringWithFormat:@"--%@--\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
//
//    // Create NSInputStream from NSData objects and the file stream
//    NSInputStream *httpBodyStartStream = [NSInputStream inputStreamWithData:httpBodyStart];
//    NSInputStream *httpBodyEndStream = [NSInputStream inputStreamWithData:httpBodyEnd];
//
////    NSInputStream *inputStream = [NSInputStream inputStreamWithInputStreams:@[httpBodyStartStream, fileStream, httpBodyEndStream]];
//
//    return inputStream;
//}

- (NSURLSession *)urlSession:(NSString *)groupId {
    if (!_urlSession) {
        NSURLSessionConfiguration *sessionConfiguration = [NSURLSessionConfiguration backgroundSessionConfigurationWithIdentifier:BACKGROUND_SESSION_ID];
        if (groupId.length > 0) {
            sessionConfiguration.sharedContainerIdentifier = groupId;
        }
        
        _urlSession = [NSURLSession sessionWithConfiguration:sessionConfiguration delegate:self delegateQueue:nil];
    }

    return _urlSession;
}

#pragma NSURLSessionTaskDelegate

- (void)URLSession:(NSURLSession *)session
              task:(NSURLSessionTask *)task
didCompleteWithError:(NSError *)error {
    NSMutableDictionary *data = [NSMutableDictionary dictionaryWithObjectsAndKeys:task.taskDescription, @"id", nil];
    NSURLSessionDataTask *uploadTask = (NSURLSessionDataTask *)task;
    NSHTTPURLResponse *response = (NSHTTPURLResponse *)uploadTask.response;
    
    if (response != nil) {
        [data setObject:@(response.statusCode) forKey:@"responseCode"];
    }

    //Add data that was collected earlier by the didReceiveData method

    NSMutableData *responseData = _responsesData[@(task.taskIdentifier)];
    [_responsesData removeObjectForKey:@(task.taskIdentifier)];
    
    if (responseData) {
        NSString *responseString = [[NSString alloc] initWithData:responseData encoding:NSUTF8StringEncoding];
        [data setObject:responseString forKey:@"responseBody"];
    } else {
        [data setObject:[NSNull null] forKey:@"responseBody"];
    }
    
    [self removeFilesForUpload:task.taskDescription];
    
    if (error == nil) {
        [self _sendEventWithName:@"RNFileUploader-completed" body:data];
    } else {
        [data setObject:error.localizedDescription forKey:@"error"];
        
        if (error.code == NSURLErrorCancelled) {
            [self _sendEventWithName:@"RNFileUploader-cancelled" body:data];
        } else {
            [self _sendEventWithName:@"RNFileUploader-error" body:data];
        }
    }
}

- (void)URLSession:(NSURLSession *)session
              task:(NSURLSessionTask *)task
   didSendBodyData:(int64_t)bytesSent
    totalBytesSent:(int64_t)totalBytesSent
totalBytesExpectedToSend:(int64_t)totalBytesExpectedToSend {
    float progress = -1;
    
    if (totalBytesExpectedToSend > 0) { //see documentation.  For unknown size it's -1 (NSURLSessionTransferSizeUnknown)
        progress = (float)totalBytesSent / (float)totalBytesExpectedToSend * 100.0;
    }
    
    NSDictionary *bodyData = @{
        @"id": task.taskDescription,
        @"progress": @(progress)
    };
    
    [self _sendEventWithName:@"RNFileUploader-progress" body:bodyData];
}

- (void)URLSession:(NSURLSession *)session dataTask:(NSURLSessionDataTask *)dataTask didReceiveData:(NSData *)data {
    if (!data.length) {
        return;
    }
    //Hold returned data so it can be picked up by the didCompleteWithError method later
    NSMutableData *responseData = _responsesData[@(dataTask.taskIdentifier)];
    if (!responseData) {
        responseData = [NSMutableData dataWithData:data];
        _responsesData[@(dataTask.taskIdentifier)] = responseData;
    } else {
        [responseData appendData:data];
    }
}

- (void)URLSession:(NSURLSession *)session
              task:(NSURLSessionTask *)task
 needNewBodyStream:(void (^)(NSInputStream *bodyStream))completionHandler {

    NSInputStream *inputStream = task.originalRequest.HTTPBodyStream;

    if (completionHandler) {
        completionHandler(inputStream);
    }
}

@end
