package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.game.ActionProxy;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

public class AddUntilEndOfTurnActionProxyEffect extends UnrespondableEffect {
    private final ActionProxy _actionProxy;

    public AddUntilEndOfTurnActionProxyEffect(ActionProxy actionProxy) {
        _actionProxy = actionProxy;
    }

    @Override
    public void doPlayEffect(LotroGame game) {
        game.getActionsEnvironment().addUntilEndOfTurnActionProxy(_actionProxy);
    }
}