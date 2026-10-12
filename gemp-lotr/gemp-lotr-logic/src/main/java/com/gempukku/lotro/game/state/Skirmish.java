package com.gempukku.lotro.game.state;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Skirmish {
    private PhysicalCard _fellowshipCharacter;
    private final Set<PhysicalCard> _shadowCharacters;
    //Used to handle effects like on V3 For My Old Gaffer
    private final Set<PhysicalCard> _pendingShadowCharacters;
    private boolean _cancelled;

    private Evaluator _fpStrengthOverrideEvaluator;
    private Evaluator _shadowStrengthOverrideEvaluator;

    private final Set<PhysicalCard> _removedFromSkirmish = new LinkedHashSet<>();
    // The strength each character removed from this skirmish had as it left (F-0480): what Legacy Ruling 2's final
    // removed character, the losing character of an emptied side, is measured by once it may be out of play.
    private final Map<PhysicalCard, Integer> _strengthWhenRemoved = new HashMap<>();

    // A card that lies where the fellowship character of this skirmish was (The One Ring, when its bearer was
    // killed here: Q-0232).  It is in the skirmish for the table to show and for nothing else: it is no
    // character, and the skirmish has no fellowship character while it lies there.
    private PhysicalCard _leftInFellowshipPlace;

    public Skirmish(PhysicalCard fellowshipCharacter, Set<PhysicalCard> shadowCharacters) {
        _fellowshipCharacter = fellowshipCharacter;
        _shadowCharacters = shadowCharacters;
        _pendingShadowCharacters = new HashSet<>();
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
        // Nothing lies in the place of a character who stands there
        if (fellowshipCharacter != null)
            _leftInFellowshipPlace = null;
    }

    /**
     * @return The card lying where the fellowship character of this skirmish was, or null.  Not a character and
     * not in the skirmish for any rule or card: only what the table shows in his place.
     */
    public PhysicalCard getCardLeftInFellowshipPlace() {
        return _leftInFellowshipPlace;
    }

    public void setCardLeftInFellowshipPlace(PhysicalCard card) {
        _leftInFellowshipPlace = card;
    }

    public void addRemovedFromSkirmish(PhysicalCard loser) {
        _removedFromSkirmish.add(loser);
    }

    public Set<PhysicalCard> getRemovedFromSkirmish() {
        return _removedFromSkirmish;
    }

    /**
     * Notes the strength a character of this skirmish has as it is about to leave it.  The first note for a card
     * stands: a card leaving play is noted before its own text stops, and not again as it is taken out of the
     * skirmish.
     */
    public void noteStrengthWhenRemoved(PhysicalCard card, int strength) {
        _strengthWhenRemoved.putIfAbsent(card, strength);
    }

    /**
     * @return The strength the card had as it left this skirmish, or null if none was noted.
     */
    public Integer getStrengthWhenRemoved(PhysicalCard card) {
        return _strengthWhenRemoved.get(card);
    }

    public void cancel() {
        _cancelled = true;
    }

    public boolean isCancelled() {
        return _cancelled;
    }

    public void replaceCharacterInSkirmish(PhysicalCard oldCard, PhysicalCard newCard) {
        if(_fellowshipCharacter == oldCard) {
            _fellowshipCharacter = newCard;
            return;
        }

        if(_shadowCharacters.contains(oldCard)) {
            _shadowCharacters.remove(oldCard);
            _shadowCharacters.add(newCard);
        }
    }

    public Skirmish copySkirmish() {
        var skirmish = new Skirmish(_fellowshipCharacter, new HashSet<>(_shadowCharacters));
        skirmish._cancelled = _cancelled;
        skirmish._removedFromSkirmish.addAll(_removedFromSkirmish);
        skirmish._strengthWhenRemoved.putAll(_strengthWhenRemoved);
        skirmish._leftInFellowshipPlace = _leftInFellowshipPlace;
        skirmish._fpStrengthOverrideEvaluator = _fpStrengthOverrideEvaluator;
        skirmish._shadowStrengthOverrideEvaluator = _shadowStrengthOverrideEvaluator;

        return skirmish;
    }
}
