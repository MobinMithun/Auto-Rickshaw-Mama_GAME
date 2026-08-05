#!/bin/bash
export JAVA_HOME="$HOME/jdk/jdk-17.0.13+11/Contents/Home"
export ANDROID_HOME="/opt/homebrew/share/android-commandlinetools"
export PATH="$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin"
cd "$(dirname "$0")"
gradle assembleDebug
