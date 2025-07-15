package com.gempukku.lotro.bots.rl.fotrstarters.models.cardaction;

public class OptionalResponsesCardActionTrainer extends AbstractPlayUseCardTrainer {
    @Override
    protected String getTextTrigger() {
        return "responses";
    }

    @Override
    protected String getAllowedActionText() {
        return ""; // Anything
    }

    @Override
    protected boolean isPlayTrainer() {
        return true;
    }

    @Override
    protected boolean isUseTrainer() {
        return true;
    }

    @Override
    protected boolean isTransferTrainer() {
        return false;
    }

    @Override
    protected boolean isHealTrainer() {
        return false;
    }
}
