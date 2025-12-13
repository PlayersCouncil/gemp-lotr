package com.gempukku.lotro.game.state;

import com.gempukku.lotro.game.PhysicalCard;

import java.util.HashSet;
import java.util.Set;

public class Assignment {
    protected final PhysicalCard _fellowshipCharacter;
    protected final Set<PhysicalCard> _shadowCharacters;

    public Assignment(PhysicalCard fellowshipCharacter, Set<PhysicalCard> shadowCharacters) {
        _fellowshipCharacter = fellowshipCharacter;
        _shadowCharacters = shadowCharacters;
    }

    public PhysicalCard getFellowshipCharacter() {
        return _fellowshipCharacter;
    }

    public Set<PhysicalCard> getShadowCharacters() {
        return _shadowCharacters;
    }

    public Assignment replaceCharacter(PhysicalCard oldCard, PhysicalCard newCard) {
        if(_fellowshipCharacter == oldCard) {
            return new Assignment(newCard, _shadowCharacters);
        }
        var newShadow = new HashSet<>(_shadowCharacters);
        for(var shadow : _shadowCharacters) {
            if(shadow == oldCard) {
                newShadow.remove(shadow);
                newShadow.add(newCard);
                return new Assignment(_fellowshipCharacter, newShadow);
            }
        }

        return null;
    }
}
