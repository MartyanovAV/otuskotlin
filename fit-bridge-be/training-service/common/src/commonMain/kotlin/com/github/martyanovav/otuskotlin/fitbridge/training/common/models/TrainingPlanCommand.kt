package com.github.martyanovav.otuskotlin.fitbridge.training.common.models

enum class TrainingPlanCommand : FBCommand {
    NONE,
    CREATE,
    READ,
    UPDATE,
    ARCHIVE,
    SEARCH,
    COMPLETE
}
