package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_263_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("anguish", "7_263");
					put("anguish2", "7_263");

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; its text answers a weapon played on it
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7; a skirmish ability used only on purpose
					// Morgul Warden: a unique [sauron] Orc, strength 9 (+1 for each other [sauron] Orc, and there is none)
					put("warden", "1_259");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					// Cirith Ungol Guard: [sauron] Uruk-hai, a [sauron] minion who is no Orc, strength 9; Damage +1 is its whole text
					put("ungol", "10_81");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					// Bounder: Hobbit ally, strength 2, vitality 2, home site 2, where the first move takes the fellowship
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AnguishStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Anguish
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Remove 3 threats to make a character skirmishing a [sauron] Orc strength -3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("anguish");

		assertEquals("Anguish", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RemovingThreeThreatsMakesTheCompanionSkirmishingASauronOrcStrengthMinusThreeForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var anguish = scn.GetShadowCard("anguish");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(anguish);

		scn.StartGame();
		// Frodo, Aragorn and Gimli: room for three threats.
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		scn.MoveMinionsToTable(chieftain, soldier);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain}, new PhysicalCardImpl[]{gimli, soldier});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.ShadowPlayAvailable(anguish));
		scn.ShadowPlayCard(anguish);

		// The cost: the three threats.  Aragorn is the one character skirmishing a [sauron] Orc, so nothing is asked;
		// Gimli is assigned to a [sauron] Orc, but his skirmish has not begun.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertInDiscard(anguish);

		scn.FreepsPass();
		scn.ShadowPass();

		// 5 against the Chieftain's 7: Aragorn loses, and with the skirmish over he is himself again.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AnAllySkirmishingASauronOrcIsMadeStrengthMinusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);

		var anguish = scn.GetShadowCard("anguish");
		var warden = scn.GetShadowCard("warden");
		scn.MoveCardsToHand(anguish);

		scn.StartGame();
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		// Strength only to arrange the board: the Bounder's 2 would read 0 at -2 and at -3 alike.
		scn.ApplyAdHocModifier(new StrengthModifier(null, bounder, null, 3));
		scn.MoveMinionsToTable(warden);

		// The fellowship has moved to site 2, the Bounder's home.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bounder, warden);
		scn.FreepsResolveSkirmish(bounder);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(bounder));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowPlayAvailable(anguish));
		scn.ShadowPlayCard(anguish);

		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetStrength(bounder));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertInDiscard(anguish);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void PlayedInASkirmishAgainstAnOrcOfAnotherCultureOrASauronMinionWhoIsNoOrcItChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var anguish = scn.GetShadowCard("anguish");
		var anguish2 = scn.GetShadowCard("anguish2");
		var troop = scn.GetShadowCard("troop");
		var ungol = scn.GetShadowCard("ungol");
		scn.MoveCardsToHand(anguish, anguish2);

		scn.StartGame();
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		scn.MoveMinionsToTable(troop, ungol);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{gimli, ungol});

		// Aragorn against an Orc of [moria]: the event is played and paid for, and nobody is weakened.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowPlayAvailable(anguish));
		scn.ShadowPlayCard(anguish);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInDiscard(anguish);
		assertEquals(0, scn.GetThreats());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsPass();
		scn.ShadowPass();
		// 8 against 13: Aragorn loses and takes a wound.
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Three threats again for the second copy.
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());

		// Gimli against a [sauron] Uruk-hai.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.ShadowPlayAvailable(anguish2));
		scn.ShadowPlayCard(anguish2);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInDiscard(anguish2);
		assertEquals(0, scn.GetThreats());
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
	}

	@Test
	public void CannotBePlayedWithOnlyTwoThreatsToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var anguish = scn.GetShadowCard("anguish");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(anguish);
		assertInHand(anguish);

		scn.StartGame();
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.MoveMinionsToTable(chieftain);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, chieftain);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The move to site 2 left twilight enough for its (1); only the threats are short.
		assertTrue(scn.GetTwilight() >= 1);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(anguish));
		assertEquals(2, scn.GetThreats());
		assertInHand(anguish);
	}
}
