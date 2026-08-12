# Mononote development commands

default: check

# Run all quality gates: ktlint + detekt + unit tests
check: lint detekt test

# Build the debug APK
build:
    ./gradlew :app:assembleDebug

# Install the debug APK on a connected device/emulator
install:
    adb install -r app/build/outputs/apk/debug/app-debug.apk

# ktlint check
lint:
    ./gradlew :app:ktlintCheck

# Auto-fix ktlint formatting
format:
    ./gradlew :app:ktlintFormat

# detekt static analysis
detekt:
    ./gradlew :app:detekt

# Unit tests (JUnit 5)
test:
    ./gradlew :app:testDebugUnitTest

# Compile instrumented Compose UI tests (running needs an emulator/device)
androidtest:
    ./gradlew :app:assembleDebugAndroidTest

# Clean all build outputs
clean:
    ./gradlew clean

# Install lefthook git hooks (run once after clone)
hooks:
    npm install
