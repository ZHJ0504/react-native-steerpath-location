import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-steerpath-location' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const SteerpathLocation = NativeModules.SteerpathLocation
  ? NativeModules.SteerpathLocation
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function start() {
  SteerpathLocation.start();
}

export function stop() {
  SteerpathLocation.stop();
}

export function hasStarted(): Promise<boolean> {
  return SteerpathLocation.hasStarted();
}

export function getUserLocation(): Promise<any> {
  return SteerpathLocation.getUserLocation();
}
