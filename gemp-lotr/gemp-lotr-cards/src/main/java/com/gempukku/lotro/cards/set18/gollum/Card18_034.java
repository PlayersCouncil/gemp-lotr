package com.gempukku.lotro.cards.set18.gollum;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.AddThreatsEffect;
import com.gempukku.lotro.logic.effects.AddUntilEndOfTurnModifierEffect;
import com.gempukku.lotro.logic.effects.ForEachBurdenYouSpotEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPlayCardFromDiscardEffect;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Treachery & Deceit
 * Side: Shadow
 * Culture: Gollum
 * Twilight Cost: 6
 * Type: Minion • Spider
 * Strength: 8
 * Vitality: 8
 * Site: 8
 * Game Text: Fierce. Each time Shelob wins a skirmish, you may add threats up to the number of burdens you can spot.
 * Each time Shelob wins a skirmish, you may play Gollum from your discard pile; he is strength +3 and fierce until the end of turn.
 */
public class Card18_034 extends AbstractMinion {
    public Card18_034() {
        super(6, 8, 8, 8, Race.SPIDER, Culture.GOLLUM, "Shelob", "Menace", true);
        addKeyword(Keyword.FIERCE);
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(final String playerId, LotroGame game, EffectResult effectResult, final PhysicalCard self) {
        if (TriggerConditions.winsSkirmish(game, effectResult, self)) {
            List<OptionalTriggerAction> actions = new LinkedList<>();

            final OptionalTriggerAction action1 = new OptionalTriggerAction(self.getCardId() + "-1", self);
            action1.setText("Add threats");
            action1.appendEffect(
                    new ForEachBurdenYouSpotEffect(playerId) {
                        @Override
                        protected void burdensSpotted(int burdensSpotted) {
                            action1.appendEffect(
                                    new AddThreatsEffect(playerId, self, burdensSpotted));
                        }
                    });
            actions.add(action1);

            if (PlayConditions.canPlayFromDiscard(playerId, game, Filters.gollum)) {
                final OptionalTriggerAction action2 = new OptionalTriggerAction(self.getCardId() + "-2", self);
                action2.setText("Play Gollum from discard");
                action2.appendEffect(
                        new ChooseAndPlayCardFromDiscardEffect(playerId, game, Filters.gollum) {
                            @Override
                            protected void afterCardPlayed(PhysicalCard cardPlayed) {
                                action2.appendEffect(
                                        new AddUntilEndOfTurnModifierEffect(
                                                new StrengthModifier(self, cardPlayed, 3)));
                                action2.appendEffect(
                                        new AddUntilEndOfTurnModifierEffect(
                                                new AddKeywordModifier(self, cardPlayed, Keyword.FIERCE)));
                            }
                        });
                actions.add(action2);
            }

            return actions;
        }
        return null;
    }
}
