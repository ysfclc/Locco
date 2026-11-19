package com.locco.app

internal object NativeBridge {
    init {
        System.loadLibrary("locco_ffi")
    }

    external fun nativeAbiVersion(): Int
    external fun nativePing(): Int
}
