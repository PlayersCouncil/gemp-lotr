package com.gempukku.lotro.logic.timing;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.modifiers.ModifiersLogic;
import com.gempukku.lotro.logic.timing.rules.*;

public class RuleSet {
    private final LotroGame _game;
    private final DefaultActionsEnvironment _actionsEnvironment;
    private final ModifiersLogic _modifiersLogic;

    public RuleSet(LotroGame game, DefaultActionsEnvironment actionsEnvironment, ModifiersLogic modifiersLogic) {
        _game = game;
        _actionsEnvironment = actionsEnvironment;
        _modifiersLogic = modifiersLogic;
    }

    public void applyRuleSet() {
        new RoamingRule(_modifiersLogic).applyRule();
        new EnduringRule(_modifiersLogic).applyRule();
        new HunterRule(_modifiersLogic).applyRule();

        new MusterRule(_actionsEnvironment).applyRule();

        new AmbushRule(_actionsEnvironment).applyRule();

        new ResolveSkirmishRule(_game, _actionsEnvironment).applyRule();

        new SanctuaryRule(_actionsEnvironment, _modifiersLogic).applyRule();

        new DiscardedCardRule(_actionsEnvironment).applyRule();

        new KilledCardRule(_actionsEnvironment).applyRule();

        new RingRelatedRule(_modifiersLogic).applyRule();

        new RingBearerRule(_modifiersLogic).applyRule();

        new ThreatRule(_actionsEnvironment).applyRule();

        new CunningRule(_modifiersLogic).applyRule();

        new ToilRule(_modifiersLogic).applyRule();

        new TransferItemRule(_actionsEnvironment).applyRule();

        new StatModifiersRule(_modifiersLogic).applyRule();

        new FollowerRule(_actionsEnvironment).applyRule();

        new PlayCardInPhaseRule(_actionsEnvironment).applyRule();
        new PlayResponseEventRule(_actionsEnvironment).applyRule();

        new ActivateResponseAbilitiesRule(_actionsEnvironment).applyRule();
        new ActivatePhaseActionsRule(_actionsEnvironment).applyRule();
        new ActivatePhaseActionsFromHandRule(_actionsEnvironment).applyRule();
        new ActivatePhaseActionsFromDiscardRule(_actionsEnvironment).applyRule();
        new ActivatePhaseActionsFromDrawDeckRule(_actionsEnvironment).applyRule();
        new ActivatePhaseActionsFromStackedRule(_actionsEnvironment).applyRule();

        new RequiredTriggersRule(_actionsEnvironment).applyRule();
        new OptionalTriggersRule(_actionsEnvironment).applyRule();
        new OptionalTriggersFromHandRule(_actionsEnvironment).applyRule();

        new HealByDiscardRule(_actionsEnvironment).applyRule();

        new TakeOffRingRule(_actionsEnvironment).applyRule();
        new ConcealedRule(_actionsEnvironment).applyRule();
    }
}
