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
    LIST
}

enum class TrainingPlanCommand : FBCommand {
    NONE,
    CREATE,
    READ,
    UPDATE,
    ARCHIVE,
    GENERATE_PUBLIC_LINK,
    CLOSE_PUBLIC_LINK,
    READ_COMPLETION_STATUS
}

enum class DashboardCommand : FBCommand {
    NONE,
    GET_TRAINER_SUMMARY
}

enum class PublicPlanCommand : FBCommand {
    NONE,
    OPEN_BY_TOKEN,
    MARK_COMPLETION
}