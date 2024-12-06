package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.Preventable;
import com.gempukku.lotro.logic.timing.results.SiteControlledResult;

public class TakeControlOfASiteEffect extends AbstractEffect implements Preventable {
    private final PhysicalCard _source;
    private final String _playerId;

    private boolean _prevented;

    public TakeControlOfASiteEffect(PhysicalCard source, String playerId) {
        _source = source;
        _playerId = playerId;
    }

    public PhysicalCard getSource() {
        return _source;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return getFirstControllableSite(game) != null;
    }

    private PhysicalCard getFirstControllableSite(LotroGame game) {
        int maxUnoccupiedSite = Integer.MAX_VALUE;
        for (String playerId : game.getGameState().getPlayerOrder().getAllPlayers())
            maxUnoccupiedSite = Math.min(maxUnoccupiedSite, game.getGameState().getPlayerPosition(playerId) - 1);

        for (int i = 1; i <= maxUnoccupiedSite; i++) {
            final PhysicalCard site = game.getGameState().getSite(i);
            if (site.getCardController() == null)
                return site;
        }

        return null;
    }

    @Override
    public String getText(LotroGame game) {
        return "Take control of a site";
    }

    @Override
    public Effect.Type getType() {
        return Type.BEFORE_TAKE_CONTROL_OF_A_SITE;
    }

    @Override
    public void prevent() {
        _prevented = true;
    }

    @Override
    public boolean isPrevented(LotroGame game) {
        return _prevented;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        PhysicalCard site = getFirstControllableSite(game);
        if (site != null && !_prevented) {
            game.getGameState().takeControlOfCard(_playerId, game, site, Zone.SUPPORT);
            game.getGameState().sendMessage(_playerId + " took control of " + GameUtils.getCardLink(site));
            game.getActionsEnvironment().emitEffectResult(new SiteControlledResult(_playerId));
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
