package com.gempukku.lotro.logic.timing;

import com.gempukku.lotro.logic.actions.OptionalTriggerAction;

import java.util.HashSet;
import java.util.Set;

public abstract class EffectResult {
    private final Set<String> _optionalTriggersUsed = new HashSet<>();

    public enum Type {
        // Translated to new format
        ANY_NUMBER_KILLED, FOR_EACH_KILLED, FOR_EACH_HEALED,
        FOR_EACH_WOUNDED, FOR_EACH_EXERTED, FOR_EACH_DISCARDED_FROM_PLAY,
        FOR_EACH_RETURNED_TO_HAND, FOR_EACH_HINDERED, FOR_EACH_RESTORED,

        FOR_EACH_DISCARDED_FROM_HAND,
        FOR_EACH_DISCARDED_FROM_DECK,

        FREE_PEOPLE_PLAYER_STARTS_ASSIGNING,
        FREE_PEOPLE_PLAYER_DECIDED_IF_MOVING,
        SKIRMISH_ABOUT_TO_END,
        THREAT_WOUND_TRIGGER,

        INITIATIVE_CHANGE,

        WHEN_MOVE_FROM, WHEN_FELLOWSHIP_MOVES, WHEN_MOVE_TO,

        START_OF_PHASE,
        END_OF_PHASE,

        START_OF_TURN,
        END_OF_TURN,

        AFTER_ALL_SKIRMISHES,

        RECONCILE,

        SKIRMISH_CANCELLED,

        ZERO_VITALITY,

        PLAY, ACTIVATE,

        DRAW_CARD_OR_PUT_INTO_HAND,

        PUT_ON_THE_ONE_RING,

        REMOVE_BURDEN, ADD_BURDEN, ADD_THREAT, ADD_TWILIGHT, ADD_CULTURE_TOKEN, REMOVE_CULTURE_TOKEN,

        FOR_EACH_REVEALED_FROM_HAND, FOR_EACH_REVEALED_FROM_DECK,

        SKIRMISH_FINISHED_WITH_OVERWHELM, SKIRMISH_FINISHED_NORMALLY,

        CHARACTER_WON_SKIRMISH, CHARACTER_LOST_SKIRMISH,

        ASSIGNED_AGAINST, ASSIGNED_TO_SKIRMISH, CARD_TRANSFERRED,

        REPLACE_SITE, CONTROL_SITE, LIBERATE_SITE,

        FINISHED_PLAYING_FELLOWSHIP,

        TAKE_OFF_THE_ONE_RING, NEW_RING_BEARER
    }

    private final Type _type;

    protected EffectResult(Type type) {
        _type = type;
    }

    public Type getType() {
        return _type;
    }

    public void optionalTriggerUsed(OptionalTriggerAction action) {
        _optionalTriggersUsed.add(action.getTriggerIdentifier());
    }

    public boolean wasOptionalTriggerUsed(OptionalTriggerAction action) {
        return _optionalTriggersUsed.contains(action.getTriggerIdentifier());
    }
}
