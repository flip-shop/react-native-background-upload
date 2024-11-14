import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  run(
    url: string,
    token: string,
    debug: boolean,
    extraHeaders: Object,
    namespaces: ReadonlyArray<string>
  ): void;
  emit(eventName: string, data: Object): void;
  disconnect(): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('RNUploaderModule');
