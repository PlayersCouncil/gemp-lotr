package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_246_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("evil", "1_246");
					put("evil2", "1_246");

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; its text answers a weapon played on it
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7; a skirmish ability used only on purpose
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
	public void EnduringEvilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Enduring Evil
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Spot X burdens to make a character skirmishing a [sauron] Orc strength -X.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("evil");

		assertEquals("Enduring Evil", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingThreeBurdensMakesTheCompanionSkirmishingASauronOrcStrengthMinusThreeForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var evil = scn.GetShadowCard("evil");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(evil);

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.MoveMinionsToTable(chieftain, soldier);

		// A skirmish event: not offered at the Shadow player's assignment actions.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(evil));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain}, new PhysicalCardImpl[]{gimli, soldier});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(evil));
		scn.ShadowPlayCard(evil);

		// X is the Shadow player's to name, from none to the 3 burdens there are.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("3");

		// Aragorn is the one character skirmishing a [sauron] Orc, so nothing more is asked.  Gimli is assigned
		// to a [sauron] Orc, but his skirmish has not begun.  The burdens are spotted, not removed.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(3, scn.GetBurdens());
		assertInZone(Zone.DISCARD, evil);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain, soldier);

		scn.FreepsPass();
		scn.ShadowPass();

		// 5 against the Chieftain's 7: Aragorn loses, and with the skirmish over he is himself again.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AnAllySkirmishingASauronOrcIsMadeWeakerByTheOneBurdenSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);

		var evil = scn.GetShadowCard("evil");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(evil);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.MoveMinionsToTable(chieftain);

		// The fellowship has moved to site 2, the Bounder's home.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bounder, chieftain);
		scn.FreepsResolveSkirmish(bounder);
		scn.FreepsPass();

		assertEquals(2, scn.GetStrength(bounder));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.ShadowPlayAvailable(evil));
		scn.ShadowPlayCard(evil);

		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");

		assertEquals(1, scn.GetStrength(bounder));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.DISCARD, evil);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void PlayedInASkirmishAgainstAnOrcOfAnotherCultureOrASauronMinionWhoIsNoOrcItChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var evil = scn.GetShadowCard("evil");
		var evil2 = scn.GetShadowCard("evil2");
		var troop = scn.GetShadowCard("troop");
		var ungol = scn.GetShadowCard("ungol");
		scn.MoveCardsToHand(evil, evil2);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(troop, ungol);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{gimli, ungol});

		// Aragorn against an Orc of [moria]: the event is played, and nobody is weakened.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowPlayAvailable(evil));
		scn.ShadowPlayCard(evil);
		scn.ShadowChoose("2");

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.DISCARD, evil);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsPass();
		scn.ShadowPass();

		// Gimli against a [sauron] Uruk-hai.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.ShadowPlayAvailable(evil2));
		scn.ShadowPlayCard(evil2);
		scn.ShadowChoose("2");

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.DISCARD, evil2);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetBurdens());
	}
}
