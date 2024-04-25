package com.gempukku.lotro.game.adventure;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.SystemQueueAction;
import com.gempukku.lotro.logic.timing.PlayerOrderFeedback;
import com.gempukku.lotro.logic.timing.PregameSetupFeedback;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import com.gempukku.lotro.logic.timing.processes.GameProcess;
import com.gempukku.lotro.logic.timing.processes.pregame.SetupSoloAdventureGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.ShadowPhaseOfAIPlayerGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.ai.AIPlayerAssignsArcheryTotalGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.ai.AIPlayerAssignsMinionsGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.regroup.DiscardAllMinionsGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.regroup.ReturnFollowersToSupportGameProcess;
import com.gempukku.lotro.logic.timing.results.PlayCardResult;

import java.util.Set;

public class DefaultSoloAdventure extends SoloAdventure {
    private final LotroCardBlueprintLibrary _library;
    private final SiteSelection _siteSelection;
    private final ShadowAI _shadowAI;
    private final String _adventureCard;
    private final String _startingSite;

    public DefaultSoloAdventure(LotroCardBlueprintLibrary library,
                                SiteSelection siteSelection, ShadowAI shadowAI,
                                String adventureCard, String startingSite) {
        _library = library;
        _siteSelection = siteSelection;
        _shadowAI = shadowAI;
        _adventureCard = adventureCard;
        _startingSite = startingSite;
    }

    @Override
    public void appendNextSiteAction(SystemQueueAction action) {
        action.appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        final GameState gameState = game.getGameState();
                        try {
                            PhysicalCard newSite = gameState.createPhysicalCard("AI", _library, _siteSelection.getNextSite(game));
                            newSite.setSiteNumber(gameState.getCurrentSiteNumber() + 1);
                            gameState.addCardToZone(game, newSite, Zone.ADVENTURE_PATH);
                            gameState.sendMessage(newSite.getOwner() + " plays " + GameUtils.getCardLink(newSite));

                            game.getActionsEnvironment().emitEffectResult(new PlayCardResult(Zone.ADVENTURE_DECK, newSite, null, null, false));
                        } catch (CardNotFoundException exp) {
                            throw new RuntimeException("Unable to create a requested card", exp);
                        }
                    }
                });
    }

    @Override
    public GameProcess getStartingGameProcess(Set<String> players, PlayerOrderFeedback playerOrderFeedback, PregameSetupFeedback pregameSetupFeedback) {
        final String player = players.iterator().next();
        return new SetupSoloAdventureGameProcess(_adventureCard, _startingSite, player, playerOrderFeedback);
    }

    @Override
    public GameProcess getAfterFellowshipPhaseGameProcess() {
        return new ShadowPhaseOfAIPlayerGameProcess(_shadowAI);
    }

    @Override
    public GameProcess getAfterFellowshipArcheryGameProcess(int fellowshipArcheryTotal, GameProcess followingProcess) {
        return new AIPlayerAssignsArcheryTotalGameProcess(fellowshipArcheryTotal, followingProcess);
    }

    @Override
    public GameProcess getAfterFellowshipAssignmentGameProcess(Set<PhysicalCard> leftoverMinions, GameProcess followingProcess) {
        return new AIPlayerAssignsMinionsGameProcess(leftoverMinions, followingProcess);
    }

    @Override
    public GameProcess getBeforeFellowshipChooseToMoveGameProcess(GameProcess followingProcess) {
        return followingProcess;
    }

    @Override
    public GameProcess getPlayerStaysGameProcess(LotroGame game, GameProcess followingProcess) {
        return new ReturnFollowersToSupportGameProcess(
                new DiscardAllMinionsGameProcess(followingProcess));
    }

    @Override
    public boolean isSolo() {
        return true;
    }
}
