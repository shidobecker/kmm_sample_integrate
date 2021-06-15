package br.com.ampli.kmmsharedmodule.data

import platform.Foundation.NSUUID

actual fun randomUUID() = NSUUID().UUIDString()