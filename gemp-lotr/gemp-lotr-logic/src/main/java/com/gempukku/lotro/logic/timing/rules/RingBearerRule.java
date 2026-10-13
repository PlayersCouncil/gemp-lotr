package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.logic.modifiers.CantDiscardFromPlayModifier;
import com.gempukku.lotro.logic.modifiers.CantReturnToHandModifier;
import com.gempukku.lotro.logic.modifiers.ModifiersLogic;

public class RingBearerRule {
    private final ModifiersLogic _modifiersLogic;

    public RingBearerRule(ModifiersLogic modifiersLogic) {
        _modifiersLogic = modifiersLogic;
    }

    public void applyRule() {
        _modifiersLogic.addAlwaysOnModifier(
                new CantDiscardFromPlayModifier(null, "Can't be discarded from play", null, null, Filters.ringBearer, Filters.any));
        _modifiersLogic.addAlwaysOnModifier(
                new CantReturnToHandModifier(null, "Can't be returned to hand", null, Filters.ringBearer, Filters.any));
        // "The Ring-bearer and One Ring should never be discarded for any reason" (Ketura on Q-0109; F-0175).
        // Like the Ring-bearer's, this is a rule's modifier (no source card), so it holds while the Ring is
        // hindered: ModifiersLogic.getModifiersProtectingCard.
        _modifiersLogic.addAlwaysOnModifier(
                new CantDiscardFromPlayModifier(null, "Can't be discarded from play", null, null, Filters.theOneRing, Filters.any));
    }
}
