package com.github.martyanovav.otuskotlin.fitbridge.training.common.models

interface FBCommand

enum class FBCommandBase : FBCommand {
    NONE,
    INIT,
    FINISH,
}
