package com.gempukku.lotro.bots.rl.fotrstarters.models.multiplechoice;

public class AnotherMoveTrainer extends AbstractMultipleChoiceTrainer {
    @Override
    protected String getTextTrigger() {
        return "another move";
    }

    @Override
    protected String getPositiveOption() {
        return "Yes";
    }
}
