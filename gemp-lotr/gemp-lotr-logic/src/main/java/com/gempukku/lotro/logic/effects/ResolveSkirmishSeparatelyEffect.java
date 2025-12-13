package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.AbstractEffect;

public class ResolveSkirmishSeparatelyEffect extends AbstractEffect {
    private final PhysicalCard _firstMinion;
    private final Filterable[] _targetSkirmisher;

    public ResolveSkirmishSeparatelyEffect(PhysicalCard firstMinion, Filterable... skirmisher) {
        _firstMinion = firstMinion;
        _targetSkirmisher = skirmisher;
    }

    @Override
    public String getText(LotroGame game) {
        return null;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return Filters.countActive(game, Filters.and(_targetSkirmisher, Filters.inSkirmish)) > 0
                && Filters.countActive(game, Filters.inSkirmishAgainst(_targetSkirmisher), _firstMinion) > 0
                && Filters.countActive(game, Filters.inSkirmishAgainst(_targetSkirmisher), Filters.not(_firstMinion)) > 0;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        if (isPlayableInFull(game)) {
            var gameState = game.getGameState();
            var skirmish = gameState.getSkirmish();

            gameState.breakSkirmishIntoSubSkirmishes(skirmish.getFellowshipCharacter(), _firstMinion);

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
