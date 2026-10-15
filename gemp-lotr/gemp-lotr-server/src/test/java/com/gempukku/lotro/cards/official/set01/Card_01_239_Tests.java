package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_239_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("allthought", "1_239");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf, an archery ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf, vitality 3; a skirmish ability
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man; a skirmish ability
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit; his abilities answer nothing here
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, home 2F; a skirmish ability

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; answers only a weapon played on him
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, no Orc; damage +1 only
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc; strength and fierce only at 2 wounds on the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AllThoughtBentonItStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: All Thought Bent on It
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Search</b>.<br><b>Maneuver:</b> Spot a [sauron] Orc and 5 companions to make the Free Peoples player exert a companion for each companion over 4.
		*/

		var scn = GetScenario();

		var allthought = scn.GetFreepsCard("allthought");

		assertEquals("All Thought Bent on It", allthought.getBlueprint().getTitle());
		assertNull(allthought.getBlueprint().getSubtitle());
		assertFalse(allthought.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, allthought.getBlueprint().getSide());
		assertEquals(Culture.SAURON, allthought.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, allthought.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(allthought, Timeword.MANEUVER));
		assertTrue(scn.HasKeyword(allthought, Keyword.SEARCH));
		assertEquals(2, allthought.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithFiveCompanionsTheFreePeoplesPlayerExertsOneCompanionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var allthought = scn.GetShadowCard("allthought");
		scn.MoveCardsToHand(allthought);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, legolas, gimli, boromir);
		scn.MoveAlliesToTable(bounder);

		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		// Vitality 3 with 2 wounds: Gimli is exhausted.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(allthought));
		scn.ShadowPlayCard(allthought);

		// Five companions, one over 4: one exertion.  The ally does not count and cannot be chosen, nor can an
		// exhausted companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, legolas, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, bounder, chieftain));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(legolas);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertInZone(Zone.DISCARD, allthought);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithSixCompanionsTwoExertionsMayBothFallOnOneCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var allthought = scn.GetShadowCard("allthought");
		scn.MoveCardsToHand(allthought);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, legolas, gimli, boromir, sam);

		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(allthought));
		scn.ShadowPlayCard(allthought);

		// Six companions, two over 4: two exertions, and "for each" lets one companion take both (CR 4.2 "for each").
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, legolas, gimli, boromir, sam));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertInZone(Zone.DISCARD, allthought);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithFourCompanionsAndAnAllyUntilAFifthCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var allthought = scn.GetShadowCard("allthought");
		scn.MoveCardsToHand(allthought);
		assertInHand(allthought);

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, legolas, gimli);
		scn.MoveAlliesToTable(bounder);

		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// Frodo and three more: four companions, and an ally beside them.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(allthought));

		scn.MoveCompanionsToTable(boromir);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(allthought));
	}

	@Test
	public void CannotBePlayedBesideASauronMinionWhoIsNoOrcAndAnOrcOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var allthought = scn.GetShadowCard("allthought");
		scn.MoveCardsToHand(allthought);
		assertInHand(allthought);

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, legolas, gimli, boromir);

		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(guard, hound);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(allthought));

		scn.MoveMinionsToTable(chieftain);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(allthought));
	}
}
