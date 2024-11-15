import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  getFileInfo(path: string): Promise<object>
  startUpload(options: object): Promise<boolean>
  cancelUpload(cancelUploadId: string): Promise<boolean>
  stopAllUploads(): Promise<boolean>
}

export default TurboModuleRegistry.getEnforcing<Spec>('RNUploaderModule');
