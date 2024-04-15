package com.gempukku.lotro.cards.set17.orc;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.SelfExertEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPlayCardFromDeckEffect;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.SpotCondition;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Rise of Saruman
 * Side: Shadow
 * Culture: Orc
 * Twilight Cost: 4
 * Type: Minion • Orc
 * Strength: 11
 * Vitality: 3
 * Site: 4
 * Game Text: Hunter 2. While you can spot an [URUK-HAI] minion, each [ORC] Orc gains hunter 1 (if that [URUK-HAI]
 * minion is Ugluk, each [ORC] Orc gains an additional hunter 1). Shadow: Exert Grishnakh twice to play an [URUK-HAI]
 * hunter minion from your draw deck.
 */
public class Card17_071 extends AbstractMinion {
    public Card17_071() {
        super(4, 11, 3, 4, Race.ORC, Culture.ORC, "Grishnákh", "Treacherous Captain", true);
        addKeyword(Keyword.HUNTER, 2);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new KeywordModifier(self, Filters.and(Culture.ORC, Race.ORC),
                        new SpotCondition(Culture.URUK_HAI, CardType.MINION), Keyword.HUNTER, 1));
        modifiers.add(
                new KeywordModifier(self, Filters.and(Culture.ORC, Race.ORC),
                        new SpotCondition(Culture.URUK_HAI, CardType.MINION, Filters.name("Ugluk")), Keyword.HUNTER, 1));
        return modifiers;
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.SHADOW, self, 0)
                && PlayConditions.canSelfExert(self, 2, game)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendEffect(
                    new ChooseAndPlayCardFromDeckEffect(playerId, Culture.URUK_HAI, CardType.MINION, Keyword.HUNTER));
            return Collections.singletonList(action);
        }
        return null;
    }
}
