package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import org.json.simple.JSONObject;

/**
 * "A Shadow player has replaced the fellowship's current site this turn": true while the fellowship is at a site
 * that a player other than the Free Peoples player replaced this turn while the fellowship was at it.  The site
 * that counts is the one the fellowship is at when this is asked: once the fellowship has moved on from a site so
 * replaced, it is false again for the site moved to (Q-0234).  PlaySiteEffect lays the mark this reads.
 */
public class ShadowPlayerReplacedCurrentSite implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object);

        return (actionContext) -> {
            final LotroGame game = actionContext.getGame();
            return game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.SHADOW_PLAYER_REPLACED_CURRENT_SITE);
        };
    }
}
