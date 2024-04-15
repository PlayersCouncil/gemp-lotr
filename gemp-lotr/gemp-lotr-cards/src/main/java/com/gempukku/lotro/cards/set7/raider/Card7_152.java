package com.gempukku.lotro.cards.set7.raider;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.KillEffect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.CharacterWonSkirmishResult;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Shadow
 * Culture: Raider
 * Twilight Cost: 8
 * Type: Minion • Man
 * Strength: 16
 * Vitality: 4
 * Site: 4
 * Game Text: Southron. To play, spot a [RAIDER] Man. While you can spot 6 threats, each time this minion wins
 * a skirmish, the companion he was skirmishing is killed.
 */
public class Card7_152 extends AbstractMinion {
    public Card7_152() {
        super(8, 16, 4, 4, Race.MAN, Culture.RAIDER, "Mûmak Commander");
        addKeyword(Keyword.SOUTHRON);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Culture.RAIDER, Race.MAN);
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.winsSkirmishInvolving(game, effectResult, self, CardType.COMPANION)
                && game.getGameState().getThreats() >= 6) {
            CharacterWonSkirmishResult skirmishResult = (CharacterWonSkirmishResult) effectResult;
            final Collection<PhysicalCard> losingCompanion = Filters.filter(skirmishResult.getInvolving(), game, CardType.COMPANION);
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new KillEffect(losingCompanion, self, KillEffect.Cause.CARD_EFFECT));
            return Collections.singletonList(action);
        }
        return null;
    }
}
