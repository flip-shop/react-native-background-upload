#import <Foundation/Foundation.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTBridgeModule.h>
#import "react_native_background_upload-Swift.h"

@interface VydiaRNFileUploader : RCTEventEmitter <RCTBridgeModule, NSURLSessionTaskDelegate>
{
    FileUploaderService *fileUploader;
}
@end

@implementation VydiaRNFileUploader

RCT_EXPORT_MODULE();

@synthesize bridge = _bridge;

-(id) init {
  self = [super init];
  if (self) {
      fileUploader = [FileUploaderService init];
  }
  return self;
}

RCT_EXPORT_METHOD(getFileInfo:(NSString *)path resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
{
    [fileUploader getFileInfo:path resolve:resolve reject:reject];
}

RCT_EXPORT_METHOD(startUpload:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [fileUploader startUpload:options resolve:resolve reject:reject];
}

RCT_EXPORT_METHOD(cancelUpload: (NSString *)cancelUploadId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [fileUploader cancelUpload:cancelUploadId resolve:resolve reject:reject];
}

@end
