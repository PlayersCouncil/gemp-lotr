package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_133_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trooper", "17_133");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Three skirmishes on one table: a hunter's fought while the Trooper waits, the Trooper's own, and a hunter's
	// fought once the Trooper's is over.
	protected VirtualTableScenario GetThreeSkirmishesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trooper", "17_133");
					put("aragorn", "15_54");    // Aragorn, Swift Hunter: [gondor] ranger, hunter 1; Skirmish: exert him to make a hunter companion +1
					put("eomer", "15_123");     // Éomer, Horsemaster: [rohan] Man, hunter 2; Skirmish: discard a [rohan] card from hand to make him +2
					put("gimli", "1_13");       // Gimli, Son of Glóin: no hunter; Skirmish: exert him to make him +2
					put("sword", "4_272");      // Éowyn's Sword: a [rohan] card for Éomer's cost; it stays in hand
					put("swordsman", "1_117");  // Swordsman of the Northern Kingdom: Free Peoples skirmish event, a ranger +2
					put("guard", "17_126");     // White Hand Guard: an [uruk-hai] hunter minion; Skirmish: exert it to make an [uruk-hai] hunter minion +2
					put("runner", "1_178");     // Goblin Runner: strength 5; its text is about being played
					put("ghastly", "18_117");   // Ghastly Wound: Shadow skirmish event, an [uruk-hai] minion +2 and damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandTrooperStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: White Hand Trooper
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>.<br>While this minion is assigned to a skirmish, each hunter companion loses its special abilities and skirmish events can not be played in those companions skirmishes.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trooper");

		assertEquals("White Hand Trooper", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void HunterCompanionsLoseTheirSpecialAbilitiesUntilItsSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreeSkirmishesScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var eomer = scn.GetFreepsCard("eomer");
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn, eomer, gimli);
		scn.MoveCardsToHand(sword);

		var trooper = scn.GetShadowCard("trooper");
		var guard = scn.GetShadowCard("guard");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(trooper, guard, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, guard},
				new PhysicalCardImpl[]{gimli, trooper},
				new PhysicalCardImpl[]{eomer, runner});

		// Aragorn's skirmish, with the Trooper assigned to Gimli's still to come.  Neither hunter companion may
		// use his ability; Gimli, who is no hunter, may use his, and so may the Guard, a hunter minion.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharAssignedAgainst(gimli, trooper));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(aragorn));
		assertFalse(scn.FreepsActionAvailable(eomer));
		assertTrue(scn.FreepsActionAvailable(gimli));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(guard));
		scn.ShadowPass();

		// The Trooper's own skirmish: it is still assigned to it, and Aragorn's ability is still shut off.
		// Aragorn lost to the Guard's 9 and took a wound; with 3 vitality left he could still exert.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(aragorn));
		assertFalse(scn.FreepsActionAvailable(eomer));
		assertTrue(scn.FreepsActionAvailable(gimli));
		scn.PassSkirmishActions();

		// Éomer's skirmish: the Trooper's is over and it is assigned to nothing, though the Runner is assigned.
		// Both hunter companions have their abilities back.
		scn.FreepsResolveSkirmish(eomer);
		assertInZone(Zone.SHADOW_CHARACTERS, trooper);
		assertFalse(scn.IsCharAssigned(trooper));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(aragorn));
		assertTrue(scn.FreepsActionAvailable(eomer));
	}

	@Test
	public void NoSkirmishEventIsPlayedInAHunterCompanionsSkirmishWhileItIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreeSkirmishesScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var eomer = scn.GetFreepsCard("eomer");
		var gimli = scn.GetFreepsCard("gimli");
		var swordsman = scn.GetFreepsCard("swordsman");
		scn.MoveCompanionsToTable(aragorn, eomer, gimli);
		scn.MoveCardsToHand(swordsman);

		var trooper = scn.GetShadowCard("trooper");
		var guard = scn.GetShadowCard("guard");
		var runner = scn.GetShadowCard("runner");
		var ghastly = scn.GetShadowCard("ghastly");
		scn.MoveCardsToHand(ghastly);

		scn.StartGame();
		scn.MoveMinionsToTable(trooper, guard, runner);
		// Enough twilight for the Shadow player's event (2) whatever the moves add.
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, guard},
				new PhysicalCardImpl[]{gimli, trooper},
				new PhysicalCardImpl[]{eomer, runner});

		// A hunter companion's skirmish while the Trooper waits: neither player's skirmish event can be played.
		// (The Swordsman may make any ranger +2, Aragorn being one; Ghastly Wound any [uruk-hai] minion.)
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharAssignedAgainst(gimli, trooper));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(swordsman));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.GetTwilight() >= 2);
		assertFalse(scn.ShadowPlayAvailable(ghastly));
		scn.ShadowPass();

		// Gimli's skirmish against the Trooper: Gimli is no hunter, so both events can be played there.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(swordsman));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(ghastly));
		scn.ShadowPass();

		// Éomer's skirmish, the Trooper's being over: a hunter's skirmish takes skirmish events again.
		scn.FreepsResolveSkirmish(eomer);
		assertFalse(scn.IsCharAssigned(trooper));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(swordsman));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(ghastly));
	}
}
