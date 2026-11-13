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
if [ ! -d "$APP_DIR/compose-resources" ]; then
  if [ -d "shared/build/bin/iosArm64/${FW_DIR}/WombatShared.framework/compose-resources" ]; then
    cp -R "shared/build/bin/iosArm64/${FW_DIR}/WombatShared.framework/compose-resources" "$APP_DIR/"
  elif [ -d "shared/build/generated/compose/resourceGenerator/preparedResources/commonMain/composeResources" ]; then
    mkdir -p "$APP_DIR/compose-resources/composeResources/wombat.shared.generated.resources"
    cp -R shared/build/generated/compose/resourceGenerator/preparedResources/commonMain/composeResources/* \
      "$APP_DIR/compose-resources/composeResources/wombat.shared.generated.resources/"
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
