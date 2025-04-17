package com.gempukku.lotro.game.state;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

import java.util.LinkedHashSet;
import java.util.Set;

public class Skirmish {
    private PhysicalCard _fellowshipCharacter;
    private final Set<PhysicalCard> _shadowCharacters;
    private boolean _cancelled;

    private Evaluator _fpStrengthOverrideEvaluator;
    private Evaluator _shadowStrengthOverrideEvaluator;

    private final Set<PhysicalCard> _removedFromSkirmish = new LinkedHashSet<>();

    public Skirmish(PhysicalCard fellowshipCharacter, Set<PhysicalCard> shadowCharacters) {
        _fellowshipCharacter = fellowshipCharacter;
        _shadowCharacters = shadowCharacters;
    }

    public Evaluator getFpStrengthOverrideEvaluator() {
        return _fpStrengthOverrideEvaluator;
    }

    public void setFpStrengthOverrideEvaluator(Evaluator fpStrengthOverrideEvaluator) {
        _fpStrengthOverrideEvaluator = fpStrengthOverrideEvaluator;
    }

    public Evaluator getShadowStrengthOverrideEvaluator() {
        return _shadowStrengthOverrideEvaluator;
    }

    public void setShadowStrengthOverrideEvaluator(Evaluator shadowStrengthOverrideEvaluator) {
        _shadowStrengthOverrideEvaluator = shadowStrengthOverrideEvaluator;
    }

    public PhysicalCard getFellowshipCharacter() {
        return _fellowshipCharacter;
    }

    public Set<PhysicalCard> getShadowCharacters() {
        return _shadowCharacters;
    }

    public void setFellowshipCharacter(PhysicalCard fellowshipCharacter) {
        _fellowshipCharacter = fellowshipCharacter;
    }

    public void addRemovedFromSkirmish(PhysicalCard loser) {
        _removedFromSkirmish.add(loser);
    }

    public Set<PhysicalCard> getRemovedFromSkirmish() {
        return _removedFromSkirmish;
    }

    public void cancel() {
        _cancelled = true;
    }

    public boolean isCancelled() {
        return _cancelled;
    }
}
