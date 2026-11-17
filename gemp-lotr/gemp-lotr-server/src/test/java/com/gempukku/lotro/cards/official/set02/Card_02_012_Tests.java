package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_012_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("realm", "2_12");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, damage +1, a skirmish ability only
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, strength 4, no text but its own play requirement
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is not a Dwarf

					put("troop", "1_177");      // Goblin Patrol Troop: minion, strength 13, no text
					put("troop2", "1_177");

					// Draw deck fillers: no text at all.
					for (int i = 1; i <= 50; i++) {
						put("filler" + i, "1_177");
					}
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetShortDeckScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("realm", "2_12");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion
					// The Free Peoples player's draw deck is his copy of the troop and the two fillers: 3 cards.
					put("troop", "1_177");      // Goblin Patrol Troop: minion, strength 13, no text
					put("filler1", "1_177");
					put("filler2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Two Dwarves at an underground site, and a draw deck deep enough for four uses.
	protected VirtualTableScenario GetTwoDwarvesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("realm", "2_12");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, damage +1
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, strength 4, no text in play
					put("troop", "1_177");      // Goblin Patrol Troop: minion, strength 13, no text
					for (int i = 1; i <= 20; i++) {
						put("filler" + i, "1_177");
					}
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path: site 4 is Balin's Tomb 1_343, underground (its maneuver ability needs a tale).  Grimir's
	// home, site 3, is Rivendell Valley 1_341, a sanctuary and no more.  No Dwarf ally has a home that is
	// underground: each is of site 3 of the Fellowship path.  Aragorn fights the skirmish; the Dwarves are in none.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("realm", "2_12");
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, strength 4, no text in play
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, home 3F; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8; a maneuver ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: minion, strength 13, no text
					for (int i = 1; i <= 20; i++) {
						put("filler" + i, "1_177");
					}
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RealmofDwarrowdelfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Realm of Dwarrowdelf
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br><b>Skirmish:</b> Discard the top 3 cards from your draw deck to make a Dwarf strength +1 (limit +3) and, if underground, <b>damage +1</b> (limit +3).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("realm");

		assertEquals("Realm of Dwarrowdelf", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardingTheTopThreeCardsMakesADwarfStrengthPlusOneAndNotDamagePlusOneAwayFromUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var realm = scn.GetFreepsCard("realm");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCardsToSupportArea(realm);
		scn.MoveCompanionsToTable(gimli, guard, aragorn);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, troop);
		scn.FreepsResolveSkirmish(gimli);

		// Site 2, Ettenmoors, is a plains site.
		assertEquals(2, scn.GetCurrentSiteNumber());
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(realm));

		scn.FreepsUseCardAction(realm);
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertEquals(discard + 3, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, troop));
		scn.FreepsChooseCard(gimli);

		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void UndergroundBothBonusesAreTheCardsToPlusThreeInEachSkirmishPhaseAndLastThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var realm = scn.GetFreepsCard("realm");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");

		scn.MoveCardsToSupportArea(realm);
		scn.MoveCompanionsToTable(gimli, guard);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, troop}, new PhysicalCardImpl[]{guard, troop2});
		scn.FreepsResolveSkirmish(gimli);

		// Site 4, Balin's Tomb, is underground.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));

		scn.FreepsUseCardAction(realm);
		scn.FreepsChooseCard(gimli);
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// The limit is the card's, not each Dwarf's: +2 to Gimli and +1 to the guard reach it.
		scn.ShadowPass();
		scn.FreepsUseCardAction(realm);
		scn.FreepsChooseCard(guard);
		assertEquals(5, scn.GetStrength(guard));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));

		scn.ShadowPass();
		scn.FreepsUseCardAction(realm);
		scn.FreepsChooseCard(gimli);
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(3, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// A fourth use is still offered and its cost is paid, but it gives nothing.
		scn.ShadowPass();
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsActionAvailable(realm));
		scn.FreepsUseCardAction(realm);
		scn.FreepsChooseCard(guard);
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertEquals(5, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(3, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// Gimli's skirmish resolves (8 against 13); in the next skirmish phase the bonuses are gone and the limit is new.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(guard);

		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));

		scn.FreepsUseCardAction(realm);
		scn.FreepsChooseCard(guard);
		assertEquals(5, scn.GetStrength(guard));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	// "... and, if underground, damage +1 (limit +3)": the limit is the card's for the whole phase, whichever
	// Dwarf is chosen (F-0135, F-0125; the shape of F-0213 on the erratum 52_12).  The definition counted the
	// damage bonus for each Dwarf apart, so a second Dwarf got damage +1 after the first had had +3.
	// F-0456.
	@Test
	public void TheDamageLimitIsTheCardsSoNoDwarfGetsMoreOnceItIsReached() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoDwarvesScenario();

		var realm = scn.GetFreepsCard("realm");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToSupportArea(realm);
		scn.MoveCompanionsToTable(gimli, guard);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, troop);
		scn.FreepsResolveSkirmish(gimli);

		// Site 4, Balin's Tomb, is underground.
		assertEquals(4, scn.GetCurrentSiteNumber());
		for (int i = 0; i < 3; i++) {
			scn.FreepsUseCardAction(realm);
			scn.FreepsChooseCard(gimli);
			scn.ShadowPass();
		}
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(4, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));

		// A fourth use, on the other Dwarf: the cost is paid and neither limit has anything left to give.
		int deck = scn.GetFreepsDeckCount();
		scn.FreepsUseCardAction(realm);
		scn.FreepsChooseCard(guard);

		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWithTwoCardsInTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShortDeckScenario();

		var realm = scn.GetFreepsCard("realm");
		var gimli = scn.GetFreepsCard("gimli");
		var filler2 = scn.GetFreepsCard("filler2");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCardsToSupportArea(realm);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(filler2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, troop);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(2, scn.GetFreepsDeckCount());
		assertFalse(scn.FreepsActionAvailable(realm));

		scn.MoveCardsToTopOfDeck(filler2);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsActionAvailable(realm));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllyWhoseHomeIsNotUndergroundGetsNoDamageWithTheFellowshipUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var realm = scn.GetFreepsCard("realm");
		var guard = scn.GetFreepsCard("guard");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(realm);
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.MoveAlliesToTable(grimir);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("Balin's Tomb", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		int grimirStrength = scn.GetStrength(grimir);

		// The companion is with the fellowship, underground: strength +1 and damage +1.
		scn.FreepsUseCardAction(realm);
		scn.FreepsChooseCard(guard);
		assertEquals(5, scn.GetStrength(guard));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));

		// Grimir is at his home site, Rivendell Valley (CR 4.2 "ally"): strength +1 and no damage.
		scn.ShadowPass();
		scn.FreepsUseCardAction(realm);
		scn.FreepsChooseCard(grimir);
		assertEquals(grimirStrength + 1, scn.GetStrength(grimir));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
