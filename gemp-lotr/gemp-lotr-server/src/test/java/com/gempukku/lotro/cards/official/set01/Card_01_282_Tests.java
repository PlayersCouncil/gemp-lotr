package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_282_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legacy", "1_282");

					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability used only on purpose
					put("warband", "1_272");    // Orc War Band: [sauron] Orc, vitality 2, cost 4, site 6; a skirmish ability used only on purpose
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai (no Orc), vitality 2, damage +1 and no other text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion; a maneuver ability used only on purpose
					put("rohirrim", "17_93");   // Aragorn, Defender of Rohan: the one [rohan] Aragorn; his text needs the Ring-bearer assigned
					put("boromir", "1_96");     // Boromir, Lord of Gondor: a [gondor] companion who is no Aragorn; his text is about being overwhelmed
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWeightofaLegacyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Weight of a Legacy
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype:
		 * Strength: -1
		 * Game Text: To play, exert a [sauron] Orc. Plays on Aragorn.
		*/

		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");

		assertEquals("The Weight of a Legacy", legacy.getBlueprint().getTitle());
		assertNull(legacy.getBlueprint().getSubtitle());
		assertFalse(legacy.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, legacy.getBlueprint().getSide());
		assertEquals(Culture.SAURON, legacy.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, legacy.getBlueprint().getCardType());
		assertEquals(0, legacy.getBlueprint().getTwilightCost());
		assertEquals(-1, legacy.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnTheRohanAragornByExertingAChosenSauronOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetShadowCard("legacy");
		var soldier = scn.GetShadowCard("soldier");
		var warband = scn.GetShadowCard("warband");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		var rohirrim = scn.GetFreepsCard("rohirrim");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToHand(legacy);
		// Aragorn is unique, so one Aragorn to a table: here the [rohan] one.
		scn.MoveCompanionsToTable(rohirrim, boromir);

		scn.StartGame();
		assertInZone(Zone.HAND, legacy);
		scn.MoveMinionsToTable(soldier, warband, sentry, troop);
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(warband));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(legacy));
		scn.ShadowPlayCard(legacy);

		// To play, exert a [sauron] Orc: not a [sauron] Uruk-hai, nor a [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, warband));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(warband);

		assertEquals(1, scn.GetWoundsOn(warband));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(troop));
		// Plays on Aragorn, the one bearer it may go on, so it goes on him unasked: not on Boromir or Frodo.
		assertAttachedTo(legacy, rohirrim);
		assertNotAttachedTo(legacy, boromir);
		assertNotAttachedTo(legacy, frodo);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilASauronOrcCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetShadowCard("legacy");
		var soldier = scn.GetShadowCard("soldier");
		var warband = scn.GetShadowCard("warband");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(legacy, warband);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		assertInZone(Zone.HAND, legacy, warband);
		scn.MoveMinionsToTable(soldier, sentry, troop);
		// Vitality 2 with 1 wound: the one [sauron] Orc in play is exhausted.  The Uruk-hai and the [moria] Orc
		// could exert.
		scn.AddWoundsToChar(soldier, 1);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(legacy));

		// A [sauron] Orc that can exert, played by legal play: the card can now be played.
		scn.ShadowPlayCard(warband);
		assertEquals(0, scn.GetWoundsOn(warband));
		assertTrue(scn.ShadowPlayAvailable(legacy));

		// The War Band is the one Orc able to pay, so it exerts unasked.
		scn.ShadowPlayCard(legacy);
		assertEquals(1, scn.GetWoundsOn(warband));
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertAttachedTo(legacy, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAnAragornAndIsPlayedOnHimOnceHeIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetShadowCard("legacy");
		var soldier = scn.GetShadowCard("soldier");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToHand(legacy);
		scn.MoveCompanionsToTable(boromir);

		scn.StartGame();
		assertInZone(Zone.HAND, legacy);
		scn.MoveMinionsToTable(soldier);
		assertEquals(0, scn.GetWoundsOn(soldier));
		scn.FreepsPass();

		// The Orc could exert, but Boromir, a [gondor] companion, and Frodo are no Aragorn.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(legacy));

		scn.MoveCompanionsToTable(aragorn);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(legacy));

		scn.ShadowPlayCard(legacy);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertAttachedTo(legacy, aragorn);
		assertNotAttachedTo(legacy, boromir);
		assertNotAttachedTo(legacy, frodo);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
