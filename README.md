# Desi Turbo Rush 🛺

A 2D retro pixel-art arcade rickshaw runner game built natively with Android Kotlin & Jetpack Compose.

---

## 🚀 Running in VS Code on macOS

### 1. Prerequisites Requirements

Before building and running the project locally on macOS, ensure you have the following installed:

1. **Java Development Kit (JDK 17)**
   - Recommended: OpenJDK 17 or Eclipse Temurin 17.
   - Install via Homebrew:
     ```bash
     brew install openjdk@17
     ```
   - Verify installation:
     ```bash
     java -version
     ```

2. **Android Command Line Tools & Android SDK**
   - You can install the SDK via **Android Studio** (easiest) or via Homebrew:
     ```bash
     brew install --cask android-commandlinetools
     ```
   - Set environment variables in your `~/.zshrc` (or `~/.bash_profile`):
     ```bash
     export ANDROID_HOME=$HOME/Library/Android/sdk
     export PATH=$PATH:$ANDROID_HOME/emulator
     export PATH=$PATH:$ANDROID_HOME/platform-tools
     export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
     ```
   - Reload your terminal configuration:
     ```bash
     source ~/.zshrc
     ```

3. **Android Device or Emulator**
   - **Option A (Android Emulator)**: Create an AVD via Android Studio or command line (`avdmanager`).
   - **Option B (Physical Device)**: Enable **Developer Options** and **USB Debugging** on your Android phone and connect via USB.

---

### 2. Recommended VS Code Extensions

Open VS Code and install these recommended extensions:
- **Kotlin** (`fwcd.kotlin`) — Syntax highlighting and Kotlin support.
- **Android** (`vsciot-vscode.vscode-arduino` or `mathiasfrohlich.Kotlin`)
- **Android Emulator** (`mobile.android-emulator`) — View and manage emulators directly inside VS Code.
- **Extension Pack for Java** (`vscjava.vscode-java-pack`)

---

### 3. Step-by-Step Execution Guide

#### Step 1: Open Terminal in VS Code
Open VS Code, press `Cmd + Shift + P` or open the integrated terminal (`Ctrl + ~`).

#### Step 2: Grant Execution Permissions to Gradle Wrapper
In the workspace root directory, run:
```bash
chmod +x gradlew
```

#### Step 3: Connect an Emulator or Physical Device
Check that your target device/emulator is detected:
```bash
adb devices
```
*(You should see your device or `emulator-5554` listed as `device`).*

#### Step 4: Build and Run
Run the following command to compile, install, and launch the app automatically:
```bash
./gradlew installDebug && adb shell am start -n com.aistudio.desiturborush.kxmpzq/com.example.MainActivity
```

#### Step 5: (Optional) VS Code Build Tasks
You can also run pre-configured tasks directly in VS Code:
1. Press `Cmd + Shift + B`.
2. Select **`Gradle: Build Debug APK`** or **`Gradle: Install & Run on Connected Device / Emulator`**.

---

## 🛠 Useful Commands

- **Build Debug APK only:**
  ```bash
  ./gradlew assembleDebug
  ```
  *(Output APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`)*

- **Run Unit Tests:**
  ```bash
  ./gradlew :app:testDebugUnitTest
  ```

- **Clean Build Directory:**
  ```bash
  ./gradlew clean
  ```

---

## ⚙️ Environment Variables & API Keys
If you need environment variables or API keys, place them in a `.env` file in the root directory (based on `.env.example`).
