package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The printed strength, vitality and resistance of the unhindered cards a character bears are added to his own, and
 * the printed site number of the cards a minion bears to its own.  Each number is one modifier of the rules for
 * every bearer, with no source card, so that taking a borne card's text away does not take its printed bonus with it.
 *
 * For the card's information ("Affecting card"), each of the four is shown as one line for each borne card whose
 * number counts, named for that card: "Glamdring: Strength +2", "Narya: Vitality +1" (Ketura's item, engine pass
 * 59).  A borne card whose number is 0 shows no line.  That showing is all the parts are for: the game reads the
 * sum, as before.
 */
public class StatModifiersRule {
    private final ModifiersLogic modifiersLogic;

    public StatModifiersRule(ModifiersLogic modifiersLogic) {
        this.modifiersLogic = modifiersLogic;
    }

    /** What one borne card adds to one number of its bearer: 0 where it adds nothing. */
    private interface BorneNumber {
        int of(LotroGame game, PhysicalCard borne, PhysicalCard bearer);
    }

    /** The same number as a modifier of its own, named for the borne card, for the card's information alone. */
    private interface ShownPart {
        Modifier of(PhysicalCard borne, PhysicalCard bearer, int value);
    }

    private static int sum(LotroGame game, PhysicalCard bearer, BorneNumber number) {
        int sum = 0;
        var gameState = game.getGameState();
        for (PhysicalCard attachedCard : gameState.getAttachedCards(bearer)) {
            if (gameState.isHindered(attachedCard))
                continue;
            sum += number.of(game, attachedCard, bearer);
        }
        return sum;
    }

    private static List<Modifier> parts(LotroGame game, PhysicalCard bearer, BorneNumber number, ShownPart shown) {
        List<Modifier> parts = new ArrayList<>();
        var gameState = game.getGameState();
        for (PhysicalCard attachedCard : gameState.getAttachedCards(bearer)) {
            if (gameState.isHindered(attachedCard))
                continue;
            final int value = number.of(game, attachedCard, bearer);
            if (value != 0)
                parts.add(shown.of(attachedCard, bearer, value));
        }
        return parts;
    }

    public void applyRule() {
        final BorneNumber strength = (game, borne, bearer) -> {
            final int value = borne.getBlueprint().getStrength();
            return (value <= 0 || modifiersLogic.appliesStrengthBonusModifier(game, borne, bearer)) ? value : 0;
        };
        final BorneNumber vitality = (game, borne, bearer) -> borne.getBlueprint().getVitality();
        final BorneNumber resistance = (game, borne, bearer) -> borne.getBlueprint().getResistance();
        final BorneNumber siteNumber = (game, borne, bearer) -> borne.getBlueprint().getSiteNumber();

        final var characterBearing = Filters.and(Filters.inPlay, Filters.character, Filters.hasAttached(Filters.any));
        final var minionBearing = Filters.and(Filters.inPlay, CardType.MINION, Filters.hasAttached(Filters.any));

        modifiersLogic.addAlwaysOnModifier(
                new StrengthModifier(null, characterBearing, null, (game, bearer) -> sum(game, bearer, strength), true) {
                    @Override
                    public List<Modifier> getShownAsAffecting(LotroGame game, PhysicalCard card) {
                        return parts(game, card, strength, (borne, bearer, value) -> new StrengthModifier(borne, bearer, null, value));
                    }
                });
        modifiersLogic.addAlwaysOnModifier(
                new VitalityModifier(null, characterBearing, null, (game, bearer) -> sum(game, bearer, vitality), true) {
                    @Override
                    public List<Modifier> getShownAsAffecting(LotroGame game, PhysicalCard card) {
                        return parts(game, card, vitality, (borne, bearer, value) -> new VitalityModifier(borne, bearer, null, value));
                    }
                });
        modifiersLogic.addAlwaysOnModifier(
                new ResistanceModifier(null, characterBearing, null, (game, bearer) -> sum(game, bearer, resistance), true) {
                    @Override
                    public List<Modifier> getShownAsAffecting(LotroGame game, PhysicalCard card) {
                        return parts(game, card, resistance, (borne, bearer, value) -> new ResistanceModifier(borne, bearer, null, value));
                    }
                });
        modifiersLogic.addAlwaysOnModifier(
                new MinionSiteNumberModifier(null, minionBearing, null, (game, bearer) -> sum(game, bearer, siteNumber), true) {
                    @Override
                    public List<Modifier> getShownAsAffecting(LotroGame game, PhysicalCard card) {
                        return parts(game, card, siteNumber, (borne, bearer, value) -> new MinionSiteNumberModifier(borne, bearer, null, value));
                    }
                });
    }
}
