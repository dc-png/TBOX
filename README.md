# TBOX‑Xposed‑模块（修改版）

> **一个可直接在 Android Studio 中编译的 Android Studio 项目，用于 TBOX Xposed 模块（我的世界 PE 辅助）。**  
> 源码通过 Jadx 反编译获得，保留了原生 `.so` 库，并提供了一个最小的 XposedBridge 桩，使项目能够在没有真正 Xposed 框架的情况下编译。  
> 我们已经把验证逻辑改写为 **任意输入均可通过**——便于学习、调试和实验。

---

## 目录
- [项目概述](#项目概述)
- [功能特点](#功能特点)
- [前置条件](#前置条件)
- [快速开始](#快速开始)
  - [1. 获取源码](#1-获取源码)
  - [2. 在 Android Studio 中打开](#2-在-android-studio-中打开)
  - [3. 确认验证绕过](#3-确认验证绕过)
- [项目结构](#项目结构)
- [编译 APK](#编译-apk)
  - [调试构建（推荐用于测试）](#调试构建推荐用于测试)
  - [发布构建（签名）](#发布构建签名)
- [在设备上安装与运行](#在设备上安装与运行)
  - [准备手机（Xposed/LSPosed）](#准备手机xposedlsposted)
  - [安装 APK](#安装-apk)
  - [启用模块并重启](#启用模块并重启)
  - [启动目标游戏](#启动目标游戏)
- [开发教程——如何修改并重新编译](#开发教程——如何修改并重新编译)
  - [步骤工作流](#步骤工作流)
  - [常见修改点](#常见修改点)
- [故障排除](#故障排除)
- [许可证](#许可证)
- [致谢](#致谢)

---

## 项目概述
TBOX 是一个 **Xposed 模块**，它在国服版《我的世界：基岩版》（包名 `com.netease.x19`）中注入代码，提供各种辅助/作弊功能（自动攻击、飞行、ESP 等）。  
原始 APK `TBOX_v1.6.5.apk` 使用 Jadx 1.4.7 反编译后得到：

* **Java 源码** (`src/main/java/…`) — 约 3100 个文件  
* **资源** (`src/main/res/`) — 布局、图片、值文件、`AndroidManifest.xml`  
* **原生库** (`src/main/jniLibs/`) — `libTBOX.so`、`libc++_shared.so`、`libecmod.so`、`libunisec2.so` 等（按 ABI 分目录）

因为该模块依赖 Xposed 框架，我们提供了一个 **最小的 XposedBridge 桩**（`xposed-api.jar`），仅包含代码中实际引用的类/接口（`IXposedHookLoadPackage`、`IXposedHookZygoteInit`、`XposedBridge`、`XC_LoadPackage`），使得项目可以在没有真实 Xposed 框架的情况下编译。

我们还修改了验证对话框：在 `Loading.showKamiDialog()` 中，**登录按钮**不再调用本地方法 `nativeKamiSubmit`，而是直接调用 `Loading.onKamiAuthResult(true)`，从而实现 **任意输入均视为成功** 的验证绕过。

---

## 功能特点
- ✅ 完整的 Java 源码 + 资源，可直接在 Android Studio 中编译  
- ✅ 原生 `.so` 库自动通过 `jniLibs` 打包  
- ✅ 最小的 XposedBridge 桩，无需外部 Xposed 依赖即可编译  
- ✅ **验证绕过**：卡密对话框任意输入均可通过  
- ✅ 标准 Gradle 构建（AGP 8.1.0，`compileSdkVersion=36`，`minSdkVersion=26`）  
- ✅ 可通过 Android Studio 的 **Run** 按钮一键安装调试 APK（通过 ADB）  

---

## 前置条件
| 项目 | 最低版本 / 备注 |
|------|-----------------|
| **Android Studio** | Arctic Fox 2020.3.1 或更高（自带 AGP 8.1+） |
| **JDK** | JDK 17（Android Studio 自带） |
| **Android SDK** | API 36（Android 13）平台及 Build‑Tools 34.0.0+ |
| **设备 / 模拟器** | Android 6.0+（API 23），模块最低支持 `minSdkVersion=26` |
| **Xposed 框架**（实际运行时必需） | **LSPosed**（Zygisk 版），支持 Android 12‑13 |
| **目标游戏** | `com.netease.x19`（国服版 *Minecraft* PE）已安装于设备 |
| **Git**（可选） | 用于克隆仓库 |

> **注意**：仅凭提供的 XposedBridge 桩可以**编译**；要真正运行该模块，设备上必须安装 LSPosed（或其他 Xposed 实现）。

---

## 快速开始

### 1. 获取源码
若您得到了一个 ZIP 包，请解压到工作目录，例如：

```bash
cd /path/to/workspace
unzip FixedProject.zip   # 或者手动复制 FixedProject 文件夹
```

如果您希望使用 Git：

```bash
git clone https://github.com/dc-png/TBOX.git
cd TBOX
```

### 2. 在 Android Studio 中打开
1. 启动 Android Studio。  
2. 选择 **File → New → Import Project**。  
3. 浏览到包含 `build.gradle` 的文件夹（**FixedProject**），选定它。  
4. Android Studio 会自动识别为 Gradle 项目并开始同步，等待同步完成（底部状态栏显示 “Gradle sync finished”）。

### 3. 确认验证绕过
打开 `app/src/main/java/com/TBOX/Loading.java`，搜索注释 `// bypass verification`，应看到类似代码：

```java
                        button2.setOnClickListener(new View.OnClickListener() { // from class: com.TBOX.Loading.6.3
                            @Override // android.view.View.OnClickListener
                            public void onClick(View view) {
                                // bypass verification: accept any input
                                Loading.onKamiAuthResult(true);
                            }
                        });
```

如果仍然是原来的 `Loading.nativeKamiSubmit(trim);`，请将其替换为上面的片段（或者直接使用本仓库中已修改好的文件）。

---

## 项目结构
```
FixedProject/
├─ app/
│  ├─ build.gradle                 # 模块级 Gradle 配置
│  ├─ src/
│  │  ├─ main/
│  │  │  ├─ java/                 # 反编译得到的 Java 源码
│  │  │  │  ├─ com/TBOX/          # 主包（Loading、HookEntry、EcModBridge …）
│  │  │  │  ├─ com/cos30/ecmod/   # 原始 Xposed 代码的核心与功能模块
│  │  │  │  └─ …（支持库等）
│  │  │  ├─ res/                  # 资源（布局、drawable、values、AndroidManifest.xml）
│  │  │  └─ jniLibs/              # 原生 .so 库（armeabi-v7a、arm64-v8a 等）
│  ├─ libs/
│  │  └─ xposed-api.jar           # 最小的 XposedBridge 桩（源码位于 xposed-api/）
│  └─ proguard-rules.pro          # （空文件，因 minifyEnabled=false）
├─ xposed-api/                    # XposedBridge 桩的源码（仅供参考）
│  └─ src/main/java/de/robv/android/xposed/…
├─ build.gradle                   # 顶层 Gradle（AGP classpath）
├─ settings.gradle                # 包含 ':app'
└─ README.md
```

---

## 编译 APK

### 调试构建（推荐用于测试）
生成未签名的 `app-debug.apk`，可通过 Android Studio 的 **Run** 按钮直接安装到已连接的设备。

**通过 Android Studio GUI**
1. 点击工具栏上的绿色三角形（`Run ‘app’`），选择已连接的设备/模拟器。  
2. Android Studio 会先同步 Gradle（如是首次打开可能需要几分钟），随后编译并通过 `adb install -r` 安装 debug APK。  
3. 底部的 **Run** 窗口会显示类似：  
   ```
   Installing APK 'app-debug.apk' on 'XXXXXXXXXXXX'
   Success
   ```

**通过命令行（可选）**
```bash
./gradlew assembleDebug   # Windows: gradlew.bat assembleDebug
```
生成的 APK 路径：`app/build/outputs/apk/debug/app-debug.apk`

### 发布构建（签名）
如果您需要一个可分发的签名 APK：

1. **创建签名密钥（仅需一次）**  
   - 在 Android Studio 中选择 **Build → Generate Signed Bundle / APK → Generate Signed APK → Create new…**  
   - 填写密钥库路径、密码、别名、有效期（建议 ≥ 25 年）以及证书信息 → **OK**。  
   - Android Studio 会在您指定的位置生成 `.jks` 文件。

2. **构建签名 APK**  
   - **Build → Generate Signed Bundle / APK → Generate Signed APK**  
   - 选择之前创建的密钥库，输入密码，选择 **release** 构建类型，点击 **Finish**。  
   - 生成的签名 APK 位于 `app/build/outputs/apk/release/app-release.apk`。

> **提示**：若只需要未签名的 release APK（用于内部测试），可直接运行 `./gradlew assembleRelease`，输出同样位于 `release` 目录，但未签名。

---

## 在设备上安装与运行

### 准备手机（Xposed/LSPosed）
1. **安装 LSPosed（Zygisk 版）**  
   - 从 [LSPosed 官方 GitHub Releases](https://github.com/LSPosed/LSPosed/releases) 下载最新的 Zygisk 安装包（ZIP）。  
   - 用 **Magisk**（Modules → Install from storage）或自定义 recovery 刷入。  
   - 打开 **LSPosed Manager**，授予所需权限（无障碍、悬浮窗、后台弹窗等）。  
   - **重启**设备（或点 LSPosed Manager 中的 *Soft Reboot*）。

2. **允许安装未知来源应用**  
   - 设置 → 应用 → 特殊应用访问 → 安装未知来源应用 → 选择您用来传输 APK 的文件管理器或 Android Studio（通过 ADB） → 开启 **允许**。

3. **打开 USB 调试**  
   - 设置 → 关于手机 → 连续点击 “版本号” 7 次打开 **开发者选项** → 开启 **USB 调试**。  
   - 首次连接 PC 时会弹出授权对话框，点击 **允许**。

### 安装 APK
- 若您使用了 Android Studio 的 **Run** 按钮，APK 已经自动安装。  
- 否则手动安装（以调试 APK 为例）：
  ```bash
  adb install -r path/to/app-debug.apk   # -r 允许覆盖已有版本
  ```
  若是发布版则把 `app-debug.apk` 换成 `app-release.apk`。

### 启用模块并重启
1. 打开 **LSPosed Manager** → **Modules** 标签。  
2. 您应能看到名为 **TBOX** 的条目（名称来源于 `AndroidManifest.xml` 中 `<application android:label="@string/app_name">`）。  
3. 将其开关打开（**ON**）。  
4. 点击 **Soft Reboot**（或完全重启设备），使框架重新加载模块。

### 启动目标游戏
1. 启动游戏 `com.netease.x19`（国服版 *Minecraft* PE）。  
2. 游戏启动后不久会弹出一个标题为 **“TBOX”** 的对话框，内含一个输入框，提示文本为 **“卡密”**。  
3. **在此处输入任意内容**（甚至直接留空），然后点击 **登录**。  
4. 对话框消失，屏幕底部会出现 toast：**“TBOX加载成功”**。  
5. 此时模块的各项功能（自动攻击、飞行、ESP 等）已经生效。

> 若未看到卡密框，请查看 Android Studio 的 **Logcat**，过滤关键字 `TBOX` 或 `Loading`，查看是否有 `showKamiDialog` 被调用的日志；若没有，则说明 hook 未触发，请检查 `HookEntry.java` 中的目标包名和主 activity 类名是否与实际游戏匹配。

---

## 开发教程——如何修改并重新编译

下面给出一个可重复的工作流程，帮助您在修改代码后快速重新编译、安装和测试。

### 步骤工作流
1. **进行修改**  
   - 编辑 `app/src/main/java/` 下的任意 Java 文件（例如更改 UI、添加功能开关）。  
   - 修改资源请放在 `app/src/main/res/`（布局、drawable、values 等）。  
   - 如需更改原生行为，请将对应的 `.so` 文件替换到 `app/src/main/jniLibs/<abi>/` 目录下，保持目录结构不变。  
   - 保持包名不变（**com.TBOX**），否则 manifest 将无法识别。

2. **验证编译**  
   - 点击 **Build → Make Project**（或锤子图标）。  
   - 查看底部的 **Build** 窗口，确保没有错误。  
   - 若出现 “cannot find symbol” 等错误，请检查是否遗漏了导入或需要在 Xposed 桩中添加对应的 stub 方法（极少情况下需要）。

3. **重新安装**  
   - **方案 A（最快）**：再次点击 **Run ‘app’**——Android Studio 会重新编译并覆盖安装 debug APK。  
   - **方案 B（手动）**：运行 `./gradlew assembleDebug`，然后执行  
     ```bash
     adb install -r app/build/outputs/apk/debug/app-debug.apk
     ```

4. **激活 / 测试**  
   - 在 LSPosed Manager 中确认模块仍处于启用状态（重新安装不会自动禁用它）。  
   - 若修改了原生库或 Xposed 入口点，请执行 **Soft Reboot** 或完全重启。  
   - 启动目标游戏，观察效果是否符合预期。

5. **迭代**  
   - 根据测试结果回到第 1 步继续修改。

### 常见修改点
| 您可能想要改动的内容 | 对应文件 / 位置 |
|----------------------|-----------------|
| **绕过或调整验证流程** | `Loading.showKamiDialog()` 中登录按钮的点击处（已改为 `Loading.onKamiAuthResult(true)`） |
| **启用 / 关闭特定作弊功能** | `com/cos30/ecmod/feature/` 下的各种 `*FeatureController` 类，它们通常读取 SharedPreferences 或配置文件；可硬码标志或添加 UI 开关 |
| **更改目标游戏或启动 Activity** | `Hookentry.java`：`TARGET_PACKAGE` 与 `MAIN_ACTIVITY_CLASS` |
| **添加或移除原生库调用** | 在 `Loading.java` 中声明的 `native*` 方法；若增删，需要同时更新对应的 `.so` 导出符号（若您有源码可重新编译；否则只能保持原有签名） |
| **增加新资源（图标、字符串）** | 放置于 `app/src/main/res/` 对应目录（drawable、mipmap、values 等），在 Java 或 XML 中通过 `@drawable/xxx`、`@string/xxx` 引用 |
| **调整最低 / 目标 SDK 版本** | `app/build.gradle` 中 `defaultConfig { minSdkVersion … targetSdkVersion … }`；同时确认 `compileSdkVersion` 也不低于目标版本 |
| **添加新的 Xposed hook** | 在 `HookEntry.handleLoadPackage` 中继续使用 `XposedHelpers.findAndHookMethod` 添加更多 hook，注意保持方法签名与类加载器的一致性 |

> **添加新原生方法的高级步骤**（仅供参考）  
> 1. 在 `Loading.java` 中声明 `public static native void nativeNewMethod(...);`  
> 2. 使用 Android Studio 的 **Generate JNI Header** 或 `javah` 生成头文件。  
> 3. 在相应的 `.c`/`.cpp` 文件中实现该函数（需要原始源码或通过反汇编/重新组装获得）。  
> 4. 使用 Android NDK（`ndk-build` 或 CMake）重新编译 `.so`，并替换 `app/src/main/jniLibs/<abi>/` 下的文件。  
> 5. 重新编译 APK 并测试。

> 由于原始 `.so` 为闭源二进制，大多数开发者选择在 Java 层进行修改（hook 注册、特性开关、UI），而不去触碰原生库。

---

## 故障排除

| 症状 | 可能原因 | 检查 / 解决办法 |
|------|----------|-----------------|
| **Logcat 中未看到 `Loading.showKamiDialog`** | `HookEntry` 没有成功 hook 目标进程（目标包名或 Activity 类名错误） | 检查 `HookEntry.java`：<br>• `TARGET_PACKAGE` 是否等于游戏的实际包名（可用 `adb shell dumpsys package com.netease.x19` 查看）<br>• `MAIN_ACTIVITY_CLASS` 是否与游戏的启动 Activity 完全匹配（可用 `adb shell cmd package resolve-activity --brief com.netease.x19`）<br>修改后重新编译并安装。 |
| **重新安装后 LSPosed Manager 仍显示旧版本** | LSPosed 按 **packageName + versionCode** 缓存模块；版本号未变化导致它认为是同一个版本 | 在 `app/build.gradle` 的 `defaultConfig` 中递增 `versionCode`（例如从 6 改到 7），重新构建并安装；或者在 LSPosed Manager 中点 **Settings → Clear cache**。 |
| **游戏启动后立即崩溃** | 原生库与设备 CPU 架构不匹配或缺少依赖的系统库 | 运行 `adb shell getprop ro.product.cpu.abi` 查看设备 ABI（如 `arm64-v8a`、`armeabi-v7a`）。<br>确认 `app/src/main/jniLibs/` 下存在对应的目录且包含所需的 `.so`。<br>若只有 64 位库而在 32 位设备上运行，需同时提供 `armeabi-v7a` 版本的 `.so`（可从原始 APK 的 `lib/` 目录复制）。 |
| **未看到 toast “TBOX加载成功”** | `Loading.onKamiAuthResult(true)` 未被调用（可能是 dialog 未弹出或后面的初始化抛异常） | 在 Logcat 中过滤 `TBOX` 或 `Loading`，查看是否有 `showKamiDialog failed:` 或其他异常堆栈。<br>若有空指针等错误，检查 `Loading.activity` 是否在调用时为 null（确认在 UI 线程中正确获取了 Activity 引用）。 |
| **部分功能未生效（如没有自动攻击）** | 那些功能可能依赖了在 Jadx 反编译时报告的 35 个错误中的方法或字段，导致代码不完整 | 查看 Jadx 的错误报告（编译时控制台会有 `ERROR - finished with errors, count: 35`）。<br>可以使用 `jadx --show-bad-code` 导出可疑代码，手动检查并修正对应的 `.java` 文件，或考虑用 smali 直接补 patch。 |
| **安装失败，提示 `INSTALL_FAILED_INVALID_APK`** | APK 损坏（通常是 Gradle 构建中途中断） | 执行 `./gradlew clean` 删除旧的构建产物，然后重新执行 `./gradlew assembleDebug` 或点击 **Run**。确保磁盘空间足够，JDK 版本与 AGP 兼容。 |
| **运行时崩溃，日志中出现 `ClassNotFoundException: de.robv.android.xposed.XposedBridge`** | 您误删了 `app/libs/xposed-api.jar`，或者 Gradle 依赖未正确引用它 | 确认 `app/libs/` 目录下存在 `xposed-api.jar`，且 `app/build.gradle` 中有：<br>`dependencies { implementation fileTree(dir: 'libs', include: ['*.jar']) }` <br>若不存在，请把我们提供的桩重新复制过去。 |
| **修改后仍需联网验证** | 意外保留了原来的 `Loading.nativeKamiSubmit(trim)` 调用 | 再次打开 `Loading.java`，确认登录按钮的点击处 **只有** `Loading.onKamiAuthResult(true);`，没有其他调用。 |

如果遇到未列出的错误，请将 **Logcat**（过滤 `TBOX` 或 `AndroidRuntime`）和 **Gradle 控制台输出**完整贴出，我会帮您进一步定位。

---

## 许可证
本项目仅用于 **学习和研究**目的。  
原始 TBOX APK 版权归其作者所有，我们仅提供了反编译得到的源码用于教育目的。  
请 **不要** 将此模块用于实际作弊，以免违反游戏的服务条款或当地法律法规。

```
MIT License

Copyright (c) 2025  <Your Name or Organization>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
DEALINGS IN THE SOFTWARE.
```

有关详细许可证，请参见仓库根目录下的 `LICENSE` 文件。

---

## 致谢
- **Jadx 团队** – 提供强大的 Dex 到 Java 反编译工具，使源码得以恢复。  
- **LSPosed 团队** – 提供现代、开源的 Xposed 框架，使我们能够在 Android 上进行 hook 实验。  
- **原始 TBOX 作者** – 他们的工作为本学习提供了参考实例。  
- **Android Open Source Project (AOSP)** – 提供了平台和工具链，使得这一切成为可能。  

---

祝您使用愉快，若在使用过程中遇到任何问题，欢迎在 issue 中提出或直接联系我。记住：**仅用于学习研究，请勿用于作弊或传播**。 🚀
