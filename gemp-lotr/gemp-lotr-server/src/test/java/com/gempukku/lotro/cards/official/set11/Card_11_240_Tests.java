package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_240_Tests
{

	// The turn-skippers walk a Fellowship-block path, which they cannot do on a Shadows-block one.  Each adventure
	// deck has Flats of Rohan in place of site 9, and the tests put the Shadow player's copy on the path as site 2.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability never used here
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf, strength 6; a skirmish ability never used here
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man, strength 7; a skirmish ability never used here
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer, strength 3
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally, strength 4, who exerts to take part in skirmishes
					// Minions of two cultures and two races, with no text that changes a strength.
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5; adds (2) when played, and is cheated in
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("savage1", "1_151");    // Uruk Savage: [isengard] Uruk-hai, strength 5, damage +1
					put("savage2", "1_151");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("flats", "11_240");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FlatsofRohanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Flats of Rohan
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 2
		 * Direction: Left
		 * Game Text: <b>Plains</b>. Each unbound companion skirmishing more than 1 minion is strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("flats");

		assertEquals("Flats of Rohan", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void EachUnboundCompanionSkirmishingTwoMinionsIsStrengthPlusThreeInHisOwnSkirmishAndOneSkirmishingOneIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var flats = scn.GetShadowSite("flats");

		scn.StartGame();
		scn.MoveCardToAdventurePath(flats);
		scn.FreepsPass();
		assertSame(flats, scn.GetCurrentSite());

		scn.MoveMinionsToTable(runner1, runner2, runner3, savage1, savage2);
		scn.SkipToAssignments();
		// The Free Peoples player assigns one minion to each of three companions; the Shadow player puts the two left
		// over beside the first two, so that Aragorn and Gimli each face an Orc and an Uruk-hai.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner1},
				new PhysicalCardImpl[]{gimli, runner2},
				new PhysicalCardImpl[]{boromir, runner3});
		scn.ShadowAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage1},
				new PhysicalCardImpl[]{gimli, savage2});

		// Assigned to two minions, but nobody is skirmishing yet.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(7, scn.GetStrength(boromir));

		scn.FreepsResolveSkirmish(aragorn);
		// 8 + 3; Gimli waits for his own skirmish, and the minions are not touched.
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(5, scn.GetStrength(runner1));
		assertEquals(5, scn.GetStrength(savage1));
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		scn.FreepsResolveSkirmish(gimli);
		// 6 + 3; Aragorn's skirmish is over.
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(savage2));
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		scn.FreepsResolveSkirmish(boromir);
		// One minion is not more than 1.
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, runner3));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ARingBoundCompanionAndAnAllySkirmishingTwoMinionsAreNotStrengthened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(thrarin);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var flats = scn.GetShadowSite("flats");

		scn.StartGame();
		scn.MoveCardToAdventurePath(flats);
		scn.FreepsPass();
		assertSame(flats, scn.GetCurrentSite());

		scn.MoveMinionsToTable(runner1, runner2, savage1, savage2);
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in skirmishes.
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{sam, runner1},
				new PhysicalCardImpl[]{thrarin, runner2});
		scn.ShadowAssignToMinions(
				new PhysicalCardImpl[]{sam, savage1},
				new PhysicalCardImpl[]{thrarin, savage2});

		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, runner1));
		assertTrue(scn.IsCharSkirmishingAgainst(sam, savage1));
		// Sam is Ring-bound: 3, unchanged.
		assertEquals(3, scn.GetStrength(sam));
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		scn.FreepsResolveSkirmish(thrarin);
		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, runner2));
		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, savage2));
		// An ally is no companion: 4, unchanged.
		assertEquals(4, scn.GetStrength(thrarin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftTheFlatsAnUnboundCompanionSkirmishingTwoMinionsIsNotStrengthened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var runner1 = scn.GetShadowCard("runner1");
		var savage1 = scn.GetShadowCard("savage1");
		var flats = scn.GetShadowSite("flats");

		scn.StartGame();
		scn.MoveCardToAdventurePath(flats);
		scn.FreepsPass();
		assertSame(flats, scn.GetCurrentSite());

		// With no minion there is no skirmish at the Flats; the fellowship moves on to Rivendell Valley, site 3.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertSame(flats, scn.GetSite(2));

		scn.MoveMinionsToTable(runner1, savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner1);
		scn.ShadowAssignToMinions(aragorn, savage1);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner1));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage1));
		// 8, with the Flats behind the fellowship.
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
