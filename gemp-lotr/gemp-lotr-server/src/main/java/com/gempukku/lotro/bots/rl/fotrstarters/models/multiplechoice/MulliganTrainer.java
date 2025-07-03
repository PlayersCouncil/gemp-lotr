package com.gempukku.lotro.bots.rl.fotrstarters.models.multiplechoice;


public class MulliganTrainer extends AbstractMultipleChoiceTrainer {
    @Override
    protected String getTextTrigger() {
        return "mulligan";
    }

    @Override
    protected String getPositiveOption() {
        return "Yes";
    }

    @Override
    protected String getNegativeOption() {
        return "No";
    }
}
