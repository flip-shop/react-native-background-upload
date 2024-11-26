import { TurboModule, TurboModuleRegistry } from 'react-native';
import type {EventEmitter} from 'react-native/Libraries/Types/CodegenTypes';
export interface Spec extends TurboModule {
  getFileInfo(path: string): Promise<Object>;
  startUpload(options: Object): Promise<boolean>;
  cancelUpload(cancelUploadId: string): Promise<boolean>;
  stopAllUploads(): Promise<boolean>;

  readonly onProgress: EventEmitter<Object>
  readonly onError: EventEmitter<Object>
  readonly onCancelled: EventEmitter<Object>
  readonly onCompleted: EventEmitter<Object>

}
export default TurboModuleRegistry.getEnforcing<Spec>('RNUploaderModule');
