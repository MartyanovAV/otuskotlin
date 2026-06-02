package com.github.martyanovav.otuskotlin.fitbridge.common.models

interface FBCommand

enum class FBCommandBase : FBCommand { NONE }

enum class TrainerProfileCommand : FBCommand {
    NONE,
    CREATE_OR_UPDATE,
    READ_OWN
}

enum class ClientCardCommand : FBCommand {
    NONE,
    CREATE,
    READ,
    UPDATE,
    ARCHIVE,
    SEARCH
}

enum class TrainingPlanCommand : FBCommand {
    NONE,
    CREATE,
    READ,
    UPDATE,
    ARCHIVE,
    SEARCH
}
