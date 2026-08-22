package com.github.martyanovav.otuskotlin.fitbridge.training.common.models

enum class ClientCardCommand : FBCommand {
    NONE,
    CREATE,
    READ,
    UPDATE,
    ARCHIVE,
    SEARCH
}
