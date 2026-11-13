#!/usr/bin/env bash
set -euo pipefail

# Default to Debug for fast local builds (skips Kotlin/Native whole-program LTO).
# Pass "release" (./build-ios-ipa.sh release) for a Release build.
MODE="${1:-debug}"

if [[ "$MODE" == "release" || "$MODE" == "Release" ]]; then
  XC_CONFIG="Release"
  GRADLE_TASK=":shared:linkReleaseFrameworkIosArm64"
  FW_DIR="releaseFramework"
else
  XC_CONFIG="Debug"
  GRADLE_TASK=":shared:linkDebugFrameworkIosArm64"
  FW_DIR="debugFramework"
fi

echo "==> [1/3] Building Kotlin iOS framework ($XC_CONFIG)..."
chmod +x ./gradlew
./gradlew "$GRADLE_TASK"

echo "==> [2/3] Building unsigned iOS app ($XC_CONFIG)..."
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration "$XC_CONFIG" \
  -sdk iphoneos \
  -arch arm64 \
  -derivedDataPath build/ios \
  OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED=YES \
  CODE_SIGN_IDENTITY="" \
  CODE_SIGNING_REQUIRED=NO \
  CODE_SIGNING_ALLOWED=NO \
  build

echo "==> [3/3] Packaging unsigned IPA for TrollStore..."
APP_DIR="build/ios/Build/Products/${XC_CONFIG}-iphoneos/wo.mbat.app"

# Copy app icons into the bundle root
cp iosApp/iosApp/AppIcon*.png "$APP_DIR/"

# Copy Compose Multiplatform resources to both root and compose-resources/ paths
if [ -d "shared/build/bin/iosArm64/${FW_DIR}/WombatShared.framework/compose-resources" ]; then
  cp -R "shared/build/bin/iosArm64/${FW_DIR}/WombatShared.framework/compose-resources" "$APP_DIR/"
  cp -R "shared/build/bin/iosArm64/${FW_DIR}/WombatShared.framework/compose-resources/"* "$APP_DIR/"
elif [ -d "shared/build/generated/compose/resourceGenerator/preparedResources/commonMain/composeResources" ]; then
  mkdir -p "$APP_DIR/compose-resources/composeResources/wombat.shared.generated.resources"
  mkdir -p "$APP_DIR/composeResources/wombat.shared.generated.resources"
  cp -R shared/build/generated/compose/resourceGenerator/preparedResources/commonMain/composeResources/* \
    "$APP_DIR/compose-resources/composeResources/wombat.shared.generated.resources/"
  cp -R shared/build/generated/compose/resourceGenerator/preparedResources/commonMain/composeResources/* \
    "$APP_DIR/composeResources/wombat.shared.generated.resources/"
fi

# Bundle any back-deployed Swift runtime libraries needed for iOS 14.x (when CODE_SIGNING_ALLOWED=NO skips CopySwiftLibs)
mkdir -p "$APP_DIR/Frameworks"
xcrun swift-stdlib-tool \
  --copy \
  --scan-executable "$APP_DIR/wo.mbat" \
  --platform iphoneos \
  --destination "$APP_DIR/Frameworks" || true
if otool -L "$APP_DIR/wo.mbat" | grep -q "libswift_Concurrency.dylib"; then
  CONCURRENCY_DYLIB="$(xcode-select -p)/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift-5.5/iphoneos/libswift_Concurrency.dylib"
  if [ -f "$CONCURRENCY_DYLIB" ] && [ ! -f "$APP_DIR/Frameworks/libswift_Concurrency.dylib" ]; then
    cp "$CONCURRENCY_DYLIB" "$APP_DIR/Frameworks/"
  fi
fi

rm -rf build/ipa build/wo.mbat-ios.ipa
mkdir -p build/ipa/Payload
cp -R "$APP_DIR" build/ipa/Payload/
(
  cd build/ipa
  zip -r -y -q ../wo.mbat-ios.ipa Payload
)

echo ""
echo "Done! IPA ready at: $(pwd)/build/wo.mbat-ios.ipa"
echo "AirDrop build/wo.mbat-ios.ipa to your device and open in TrollStore."
