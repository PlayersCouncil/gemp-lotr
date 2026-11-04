package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_121_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("forager", "11_121");
					put("forager2", "11_121");

					// Denizen of Khazad-dum: an [orc] Orc, cost 3; strength +2 at an underground site only, and none is reached.
					put("denizen", "11_115");
					// Half-troll of Far Harad: an [orc] minion who is no Orc.  Its trigger at the start of the assignment
					// phase is never reached.
					put("halftroll", "14_12");
					// Goblin Patrol Troop: a [moria] Orc, no text.
					put("troop", "1_177");
					// The Beckoning Shadow: an [orc] support-area condition, no minion (nothing below 5 burdens).
					put("beckoning", "12_83");

					// Aragorn, Ranger of the North: a [gondor] Man, resistance 6.  A maneuver ability, never used.
					put("aragorn", "1_89");
					// Farin, Emissary of Erebor: a [dwarven] Dwarf, resistance 6.  Muster at a battleground or underground
					// site only.
					put("farin", "11_7");
					// Gimli, Lively Combatant: a [dwarven] Dwarf, resistance 7.  Draws when he wins a skirmish, never here.
					put("gimli", "11_8");
					// Bounder: a Hobbit ally (resistance 0, as every ally has).  A skirmish ability, never used.
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ForagingOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Foraging Orc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: When you play this minion, you may spot another [orc] minion and a companion who has resistance 3 or less to add (2).
		*/

		var scn = GetScenario();

		var forager = scn.GetFreepsCard("forager");

		assertEquals("Foraging Orc", forager.getBlueprint().getTitle());
		assertNull(forager.getBlueprint().getSubtitle());
		assertFalse(forager.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, forager.getBlueprint().getSide());
		assertEquals(Culture.ORC, forager.getBlueprint().getCulture());
		assertEquals(CardType.MINION, forager.getBlueprint().getCardType());
		assertEquals(Race.ORC, forager.getBlueprint().getRace());
		assertEquals(3, forager.getBlueprint().getTwilightCost());
		assertEquals(9, forager.getBlueprint().getStrength());
		assertEquals(2, forager.getBlueprint().getVitality());
		assertEquals(4, forager.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedBesideAnOrcOrcAndAManOfResistance3ItMayAdd2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var forager = scn.GetShadowCard("forager");
		var denizen = scn.GetShadowCard("denizen");
		scn.MoveCardsToHand(forager);

		scn.StartGame();
		scn.AddBurdens(3);
		// 6 less 3 burdens: exactly the bound.
		assertEquals(3, scn.GetResistance(aragorn));
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(denizen);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(forager);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(forager));
		int pool = scn.GetTwilight();

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcMinionWhoIsNoOrcAndADwarfOfResistance3AlsoLetItAdd2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(farin);

		var forager = scn.GetShadowCard("forager");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCardsToHand(forager);

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetResistance(farin));
		// Frodo, 10 less 3, is the only other companion.
		assertEquals(7, scn.GetResistance(scn.GetRingBearer()));
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(halftroll);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(forager);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(forager));
		int pool = scn.GetTwilight();

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedNothingIsAddedAndASecondCopyPlayedAfterItOffersOnlyItsOwnAdd() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var forager = scn.GetShadowCard("forager");
		var denizen = scn.GetShadowCard("denizen");
		var forager2 = scn.GetShadowCard("forager2");
		scn.MoveCardsToHand(forager, denizen, forager2);

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetResistance(aragorn));
		scn.SetTwilight(20);
		scn.FreepsPass();

		// The Denizen in play first, so that the Forager's spot is met.
		scn.ShadowPlayCard(denizen);
		assertInZone(Zone.SHADOW_CHARACTERS, denizen);
		scn.ShadowPlayCard(forager);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(forager));
		int pool = scn.GetTwilight();

		scn.ShadowDeclineOptionalTrigger();
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second Foraging Orc played while the first is in play, both spots still met: the second copy offers its
		// own add, and the first answers its own play only.
		scn.ShadowPlayCard(forager2);
		assertInZone(Zone.SHADOW_CHARACTERS, forager2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(forager2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(forager));
		int poolAtSecondOffer = scn.GetTwilight();
		scn.ShadowAcceptOptionalTrigger();
		// One add of (2), the second copy's.
		assertEquals(poolAtSecondOffer + 2, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoOtherOrcMinionCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var forager = scn.GetShadowCard("forager");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(forager);

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetResistance(aragorn));
		scn.SetTwilight(10);
		scn.FreepsPass();
		// A [moria] Orc and an [orc] card that is no minion; the Forager itself is the only [orc] minion.
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(beckoning);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(forager);
		assertInZone(Zone.SHADOW_CHARACTERS, forager);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoCompanionHasResistance3OrLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(bounder);

		var forager = scn.GetShadowCard("forager");
		var denizen = scn.GetShadowCard("denizen");
		scn.MoveCardsToHand(forager);

		scn.StartGame();
		scn.AddBurdens(3);
		// One over the bound; an ally with resistance 0 who is no companion; Frodo at 10 less 3.
		assertEquals(4, scn.GetResistance(gimli));
		assertEquals(0, scn.GetResistance(bounder));
		assertEquals(7, scn.GetResistance(scn.GetRingBearer()));
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(denizen);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(forager);
		assertInZone(Zone.SHADOW_CHARACTERS, forager);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
