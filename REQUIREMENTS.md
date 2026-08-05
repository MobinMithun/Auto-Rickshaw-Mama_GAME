# macOS & VS Code Development Requirements

This document details all system requirements, software prerequisites, VS Code extensions, environment variables, and execution steps needed to run and build **Desi Turbo Rush** in **VS Code on macOS**.

---

## 📋 System & Software Requirements

| Requirement | Recommended Version | Description |
| :--- | :--- | :--- |
| **Operating System** | macOS 12+ (Monterey or newer) | Intel or Apple Silicon (M1/M2/M3/M4) |
| **Java Development Kit (JDK)** | OpenJDK 17 or Temurin 17 | Required for Kotlin & Gradle compilation |
| **Android SDK** | API Level 34 (Android 14) | Required for compiling and running the app |
| **Android Build Tools** | 34.0.0+ | Installed via Android SDK Manager |
| **Android Emulator / Device** | Android 8.0+ (API 26+) | Physical device or AVD (Android Virtual Device) |
| **IDE / Editor** | Visual Studio Code | Version 1.85 or newer |

---

## 🛠 macOS Prerequisites Installation

### 1. Install Homebrew (if not already installed)
```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

### 2. Install JDK 17
```bash
brew install openjdk@17
```

Configure macOS system symlink for Java 17:
```bash
sudo ln -sfn $(brew --prefix)/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

Verify Java installation:
```bash
java -version
```

### 3. Install Android SDK & Command Line Tools
You can install **Android Studio** (easiest way to manage SDKs and Emulators) or use Homebrew:

**Option A: Android Studio (Recommended for macOS)**
1. Download & install [Android Studio](https://developer.android.com/studio).
2. Open Android Studio → SDK Manager → Install **Android 14.0 (API 34)** and **Android SDK Build-Tools**.
3. Create an Android Virtual Device (AVD) via Device Manager.

**Option B: Command Line Tools via Homebrew**
```bash
brew install --cask android-commandlinetools
```

---

## 🌐 Environment Variables Setup (`.zshrc`)

Add the following to your `~/.zshrc` file (or `~/.bash_profile` if using bash):

```bash
# Java 17 Path
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Android SDK Paths
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/emulator
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/tools
```

Apply the environment changes:
```bash
source ~/.zshrc
```

---

## 🔌 Recommended VS Code Extensions

Open VS Code, go to the **Extensions tab** (`Cmd + Shift + X`), and install:

1. **Kotlin** (`fwcd.kotlin`)
   - Syntax highlighting, code navigation, and autocomplete for Kotlin files.
2. **Extension Pack for Java** (`vscjava.vscode-java-pack`)
   - Language support for Java & Gradle project recognition.
3. **Android Emulator** (`mobile.android-emulator`)
   - Launch and view Android emulators directly inside VS Code.
4. **Gradle for Java** (`vscjava.vscode-gradle`)
   - Gradle task explorer and build execution management.

---

## 🚀 How to Build and Run in VS Code

### Step 1: Grant Executable Permissions to Gradle Wrapper
In VS Code integrated terminal (`Ctrl + ~`), run:
```bash
chmod +x gradlew
```

### Step 2: Verify Device Connection
Ensure your physical phone (with USB Debugging enabled) or Android Emulator is connected:
```bash
adb devices
```
*Expected output:* `emulator-5554   device` or `<device_serial_id>   device`

### Step 3: Run the App
Run this single command to compile the APK, install it on your device, and launch the game:
```bash
./gradlew installDebug && adb shell am start -n com.aistudio.desiturborush.kxmpzq/com.example.MainActivity
```

---

## ⚡ Useful Terminal Commands

- **Build Debug APK:**
  ```bash
  ./gradlew assembleDebug
  ```
  *(APK will be at `app/build/outputs/apk/debug/app-debug.apk`)*

- **Run Unit & Robolectric Tests:**
  ```bash
  ./gradlew :app:testDebugUnitTest
  ```

- **Clean Project Build Cache:**
  ```bash
  ./gradlew clean
  ```

- **Check Connected Android Devices:**
  ```bash
  adb devices
  ```
