# TBOX Decompiled Project (Fixed)

This project contains the decompiled source code of TBOX_v1.6.5.apk with the following fixes applied:

1. All Java sources from Jadx decompilation placed under `src/main/java`.
2. All resources (including AndroidManifest.xml) placed under `src/main/res`.
3. Native libraries (.so files) placed under `src/main/jniLibs`.
4. A minimal stub for the XposedBridge API (`de.robv.android.xposed.*`) provided as a library module `xposed-api` to satisfy imports.
5. Gradle build files (`build.gradle`, `settings.gradle`) configured for Android Gradle Plugin.

## How to Build

1. Install Android Studio (or the Android SDK and Gradle).
2. Open the `FixedProject` directory as a Gradle project.
3. Sync the project. Ensure the compileSdkVersion (36) and dependencies are satisfied.
4. Build the project.

## Notes

- The XposedBridge API stub contains only the classes and methods referenced in the decompiled code. It does not provide actual Xposed functionality; it is only for compilation.
- To run the module on a device with Xposed/LSPosed, you must replace the stub with the real XposedBridge API (available from the Xposed framework).
- Some decompilation errors may remain in the source code (Jadx reported 35 errors). Those would need manual fixes if you intend to produce a fully functional APK.
- This project is intended for learning and research purposes only.

## Directory Structure

```
FixedProject/
├─ app/
│  ├─ src/
│  │  ├─ main/
│  │  │  ├─ java/        # Decompiled Java sources
│  │  │  ├─ res/         # Resources
│  │  │  ├─ jniLibs/     # Native .so libraries
│  │  │  └─ AndroidManifest.xml
│  └─ build.gradle
├─ xposed-api/
│  └─ src/main/java/de/robv/android/xposed/...  # Stub API classes
├─ build.gradle
└─ settings.gradle
