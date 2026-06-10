package com.github.martyanovav.otuskotlin.fitbridge.mappers.v1.exceptions

class UnknownRequestClass(clazz: Class<*>) : RuntimeException("Class $clazz cannot be mapped to FitBridgeContext")
