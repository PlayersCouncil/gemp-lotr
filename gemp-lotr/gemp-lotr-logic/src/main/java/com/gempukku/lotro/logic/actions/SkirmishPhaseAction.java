package com.gempukku.lotro.logic.actions;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.effects.ResolveSkirmishEffect;
import com.gempukku.lotro.logic.effects.TriggeringResultEffect;
import com.gempukku.lotro.logic.modifiers.ModifiersLogic;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import com.gempukku.lotro.logic.timing.results.EndOfPhaseResult;
import com.gempukku.lotro.logic.timing.results.SkirmishAboutToEndResult;
import com.gempukku.lotro.logic.timing.results.StartOfPhaseResult;

import java.util.HashSet;
import java.util.Set;

public class SkirmishPhaseAction extends SystemQueueAction {
    public SkirmishPhaseAction(final PhysicalCard fellowshipCharacter, final Set<PhysicalCard> shadowCharacters) {
        appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        game.getGameState().startSkirmish(fellowshipCharacter, shadowCharacters);
                        game.getGameState().setCurrentPhase(Phase.SKIRMISH);
                    }
                });
        appendEffect(
                new TriggeringResultEffect(null, new StartOfPhaseResult(Phase.SKIRMISH), "Start of skirmish phase"));
        appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        ((ModifiersLogic) game.getModifiersEnvironment()).signalStartOfPhase(Phase.SKIRMISH);
                        ((DefaultActionsEnvironment) game.getActionsEnvironment()).signalStartOfPhase(Phase.SKIRMISH);
                    }
                });
        appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        game.getActionsEnvironment().addActionToStack(new SkirmishActionProcedureAction());
                    }
                });
        appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        if (!game.getGameState().getSkirmish().isCancelled()) {
                            insertEffect(
                                    new ResolveSkirmishEffect());
                        }
                    }
                });
        // Step 5: the skirmish is about to end, involving the characters it began with and any who came into it in
        // another's place, in that one's stead (F-0563); read from the skirmish now, not as it began.
        appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        var skirmish = game.getGameState().getSkirmish();
                        var result = (skirmish == null)
                                ? new SkirmishAboutToEndResult(fellowshipCharacter, shadowCharacters)
                                : new SkirmishAboutToEndResult(skirmish.getInvolvedFellowshipCharacter(),
                                        new HashSet<>(skirmish.getInvolvedShadowCharacters()));
                        insertEffect(new TriggeringResultEffect(null, result, "Skirmish about to end"));
                    }
                });
        appendEffect(
                new TriggeringResultEffect(null, new EndOfPhaseResult(Phase.SKIRMISH), "End of skirmish phase"));
        appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        game.getGameState().finishSkirmish();
                    }
                });

        appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        ((ModifiersLogic) game.getModifiersEnvironment()).signalEndOfPhase(Phase.SKIRMISH);
                        ((DefaultActionsEnvironment) game.getActionsEnvironment()).signalEndOfPhase(Phase.SKIRMISH);
                    }
                });
    }
}
