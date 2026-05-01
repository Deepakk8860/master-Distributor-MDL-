package com.android.masterdistributormdl.gskDistributor.model.consultant

data class ConsultantResult(
    val consultant: Consultant,
    val escalation_manager: EscalationManager,
    val manager: Manager,
    val message: String,
    val status: Int,
    val trainer: Trainer
)