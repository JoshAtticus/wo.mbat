import UIKit
import WombatShared

// Stub out SIMD float16 matrix invert symbols emitted by Kotlin/Native's platform.darwin
// cinterop bridges when building Debug frameworks with Xcode 16+ / 26+ SDKs for iOS < 17.
@_cdecl("__invert_h2")
public func __invert_h2() {}

@_cdecl("__invert_h3")
public func __invert_h3() {}

@_cdecl("__invert_h4")
public func __invert_h4() {}

