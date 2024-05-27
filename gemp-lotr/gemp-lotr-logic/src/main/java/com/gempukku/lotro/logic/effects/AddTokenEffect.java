package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;

public class AddTokenEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final PhysicalCard _target;
    private final Token _token;
    private final int _count;

    public AddTokenEffect(PhysicalCard source, PhysicalCard target, Token token, int count) {
        _source = source;
        _target = target;
        _token = token;
        _count = count;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return _target.getZone().isInPlay() && !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_TOUCH_CULTURE_TOKENS);
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public String getText(LotroGame game) {
        if (_token != null)
            return "Add " + _count + " " + _token.getCulture().getHumanReadable() + " token" + (_count > 1 ? "s" : "");
        else
            return "Add " + _count + " culture token" + (_count > 1 ? "s" : "");
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        if (isPlayableInFull(game)) {
            if (_count > 0) {
                game.getGameState().addTokens(_target, _token, _count);
                game.getGameState().sendMessage(GameUtils.getCardLink(_source) + " added " + _count + " " + _token + " token" + ((_count > 1) ? "s" : "") + " to " + GameUtils.getCardLink(_target));
            }

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
