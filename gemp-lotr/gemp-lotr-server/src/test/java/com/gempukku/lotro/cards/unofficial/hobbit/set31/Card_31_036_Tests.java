package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_036_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  The maneuver phase
	// of turn 1 is at The Troll Hoard 30_50 (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goblin", "31_36");

					put("fury", "30_3");        // Battle Fury: [dwarven] Skirmish event
					put("lore", "30_13");       // Lore of Imladris: [dwarven] Maneuver event
					put("axe", "31_1");         // Dwarven Axe: [dwarven] possession, no event
					put("arms", "30_31");       // Take up arms. Fight!: [gandalf] Skirmish event
					put("worms", "32_33");      // Were-worms: a Shadow card for the Shadow player's own hand, not revealed
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TheGreatGoblinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: The Great Goblin, Chieftain of the Misty Mountains
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Fierce</b>.
		 * <b>Maneuver:</b> Exert The Great Goblin twice to reveal the Free Peoples player's hand. Choose and discard a revealed [dwarven] event.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("goblin");

		assertEquals("The Great Goblin", card.getBlueprint().getTitle());
		assertEquals("Chieftain of the Misty Mountains", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverActionExertsHimTwiceToRevealTheHandAndDiscardAChosenDwarvenEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goblin = scn.GetShadowCard("goblin");
		var fury = scn.GetFreepsCard("fury");
		var lore = scn.GetFreepsCard("lore");
		var axe = scn.GetFreepsCard("axe");
		var arms = scn.GetFreepsCard("arms");
		var worms = scn.GetShadowCard("worms");
		scn.MoveCardsToHand(fury, lore, axe, arms, worms);

		scn.StartGame();
		assertInZone(Zone.HAND, fury, lore, axe, arms, worms);
		scn.MoveMinionsToTable(goblin);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(goblin));
		assertEquals(4, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, fury, lore, axe, arms, worms);
		assertTrue(scn.ShadowActionAvailable(goblin));
		scn.ShadowUseCardAction(goblin);

		assertEquals(2, scn.GetWoundsOn(goblin));
		// The whole hand is shown to the Shadow player, and the Free Peoples player is shown nothing.
		assertEquals(4, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(fury, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(lore, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(arms, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(worms, false));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		// The Shadow player chooses: either [dwarven] event, not the [dwarven] possession or the [gandalf] event.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(fury, lore));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axe, arms));
		scn.ShadowChooseCard(lore);

		assertInZone(Zone.DISCARD, lore);
		assertInZone(Zone.HAND, fury, axe, arms);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoDwarvenEventInHandTheHandIsRevealedAndNothingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goblin = scn.GetShadowCard("goblin");
		var axe = scn.GetFreepsCard("axe");
		var arms = scn.GetFreepsCard("arms");
		scn.MoveCardsToHand(axe, arms);

		scn.StartGame();
		assertInZone(Zone.HAND, axe, arms);
		scn.MoveMinionsToTable(goblin);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(goblin));
		assertInZone(Zone.HAND, axe, arms);
		assertTrue(scn.ShadowActionAvailable(goblin));
		scn.ShadowUseCardAction(goblin);

		assertEquals(2, scn.GetWoundsOn(goblin));
		assertEquals(2, scn.ShadowGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		assertInZone(Zone.HAND, axe, arms);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedWhenHeCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goblin = scn.GetShadowCard("goblin");
		var fury = scn.GetFreepsCard("fury");
		scn.MoveCardsToHand(fury);

		scn.StartGame();
		assertInZone(Zone.HAND, fury);
		scn.MoveMinionsToTable(goblin);
		scn.AddWoundsToChar(goblin, 1);
		assertEquals(1, scn.GetWoundsOn(goblin));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Vitality 3 with 1 wound: he can exert once, not twice.
		assertEquals(1, scn.GetWoundsOn(goblin));
		assertInZone(Zone.HAND, fury);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(goblin));
	}
}
