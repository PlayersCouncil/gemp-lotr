package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.SkirmishCancelledResult;

public class CancelSkirmishEffect extends AbstractEffect {
    private Filterable[] _involvementFilter;

    public CancelSkirmishEffect(Filterable... involvementFilter) {
        _involvementFilter = involvementFilter;
    }

    @Override
    public String getText(LotroGame game) {
        return "Cancel skirmish";
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return game.getGameState().getSkirmish() != null
                && !game.getGameState().getSkirmish().isCancelled()
                && (_involvementFilter == null || Filters.countActive(game, Filters.and(_involvementFilter, Filters.inSkirmish)) > 0)
                && game.getModifiersQuerying().canCancelSkirmish(game, game.getGameState().getSkirmish().getFellowshipCharacter())
                && (game.getFormat().canCancelRingBearerSkirmish() || Filters.countActive(game, Filters.ringBearer, Filters.inSkirmish) == 0);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        if (isPlayableInFull(game)) {
            final PhysicalCard fellowshipCharacter = game.getGameState().getSkirmish().getFellowshipCharacter();
            if (fellowshipCharacter != null)
                game.getActionsEnvironment().emitEffectResult(
                        new SkirmishCancelledResult(fellowshipCharacter));
            game.getGameState().sendMessage("Skirmish is cancelled");
            game.getGameState().getSkirmish().cancel();
            return new FullEffectResult(true);
        }
        game.getGameState().sendMessage("Skirmish could not be cancelled");
        return new FullEffectResult(false);
    }
}
