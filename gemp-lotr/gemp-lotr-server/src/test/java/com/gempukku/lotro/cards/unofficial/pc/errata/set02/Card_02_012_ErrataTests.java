package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_012_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("realm", "52_12");

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
					put("realm", "52_12");
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

	protected VirtualTableScenario GetSixCardDeckScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("realm", "52_12");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion
					// The Free Peoples player's draw deck is his copy of the troop and the five fillers: 6 cards.
					put("troop", "1_177");      // Goblin Patrol Troop: minion, strength 13, no text
					put("filler1", "1_177");
					put("filler2", "1_177");
					put("filler3", "1_177");
					put("filler4", "1_177");
					put("filler5", "1_177");
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
		 * Game Text: Skirmish: Discard the top 3 cards from your draw deck to make a Dwarf strength +1 (limit +3) and, if underground, damage +1 (limit +3).  You may then discard up to 30 cards from your draw deck 3 at a time.
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

		// The further discards are offered, and declined.
		assertTrue(scn.FreepsDecisionAvailable("discard"));
		scn.FreepsChooseNo();

		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertEquals(discard + 3, scn.GetFreepsDiscardCount());
		assertEquals(7, scn.GetStrength(gimli));
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
		scn.FreepsChooseNo();
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// The limit is the card's, not each Dwarf's: +2 to Gimli and +1 to the guard reach it.
		scn.ShadowPass();
		scn.FreepsUseCardAction(realm);
		scn.FreepsChooseCard(guard);
		scn.FreepsChooseNo();
		assertEquals(5, scn.GetStrength(guard));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));

		scn.ShadowPass();
		scn.FreepsUseCardAction(realm);
		scn.FreepsChooseCard(gimli);
		scn.FreepsChooseNo();
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(3, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// A fourth use is still offered and its cost is paid, but it gives nothing.
		scn.ShadowPass();
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsActionAvailable(realm));
		scn.FreepsUseCardAction(realm);
		scn.FreepsChooseCard(guard);
		scn.FreepsChooseNo();
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
		scn.FreepsChooseNo();
		assertEquals(5, scn.GetStrength(guard));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheDamageLimitIsTheCardsSoNoDwarfGetsMoreOnceItIsReached() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var realm = scn.GetFreepsCard("realm");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCardsToSupportArea(realm);
		scn.MoveCompanionsToTable(gimli, guard);

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
			scn.FreepsChooseNo();
			scn.ShadowPass();
		}
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(4, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));

		int deck = scn.GetFreepsDeckCount();
		scn.FreepsUseCardAction(realm);
		scn.FreepsChooseCard(guard);
		scn.FreepsChooseNo();

		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void FurtherDiscardsGoThreeAtATimeUpToThirtyCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var realm = scn.GetFreepsCard("realm");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCardsToSupportArea(realm);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, troop);
		scn.FreepsResolveSkirmish(gimli);

		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();
		assertTrue(deck >= 50);

		// Two groups of 3, then stop.
		scn.FreepsUseCardAction(realm);
		scn.FreepsChooseYes();
		assertEquals(deck - 6, scn.GetFreepsDeckCount());
		scn.FreepsChooseYes();
		assertEquals(deck - 9, scn.GetFreepsDeckCount());
		scn.FreepsChooseNo();
		assertEquals(deck - 9, scn.GetFreepsDeckCount());
		assertEquals(discard + 9, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Ten groups of 3 is the most: no eleventh is offered.
		scn.ShadowPass();
		scn.FreepsUseCardAction(realm);
		for (int i = 0; i < 10; i++) {
			assertTrue(scn.FreepsDecisionAvailable("discard"));
			scn.FreepsChooseYes();
		}
		assertEquals(deck - 9 - 3 - 30, scn.GetFreepsDeckCount());
		assertEquals(discard + 9 + 3 + 30, scn.GetFreepsDiscardCount());
		assertTrue(scn.GetFreepsDeckCount() > 0);
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
	public void FurtherDiscardsAreNotOfferedOnceTheCostHasEmptiedTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShortDeckScenario();

		var realm = scn.GetFreepsCard("realm");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCardsToSupportArea(realm);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, troop);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(3, scn.GetFreepsDeckCount());
		scn.FreepsUseCardAction(realm);

		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(7, scn.GetStrength(gimli));
		assertFalse(scn.FreepsDecisionAvailable("discard"));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void FurtherDiscardsAreNotOfferedOnceAFurtherDiscardHasEmptiedTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSixCardDeckScenario();

		var realm = scn.GetFreepsCard("realm");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCardsToSupportArea(realm);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, troop);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(6, scn.GetFreepsDeckCount());
		scn.FreepsUseCardAction(realm);

		// The cost leaves 3 cards, so a further group is still offered.
		assertEquals(3, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsDecisionAvailable("discard"));
		scn.FreepsChooseYes();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(6, scn.GetFreepsDiscardCount());
		assertFalse(scn.FreepsDecisionAvailable("discard"));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAllyWhoseHomeIsNotUndergroundGetsNoDamageWithTheFellowshipUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

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
		// Site 4, Balin's Tomb, is underground; Grimir's home, site 3, is Rivendell Valley, which is not.
		assertEquals("Balin's Tomb", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertFalse(scn.HasKeyword(scn.GetSite(3), Keyword.UNDERGROUND));
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		int grimirStrength = scn.GetStrength(grimir);

		// The companion is with the fellowship, underground: strength +1 and damage +1.
		scn.FreepsUseCardAction(realm);
		scn.FreepsChooseCard(guard);
		assertEquals(5, scn.GetStrength(guard));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		scn.FreepsChooseNo();

		// Grimir is at his home site, Rivendell Valley (CR 4.2 "ally"): strength +1 and no damage.
		scn.ShadowPass();
		scn.FreepsUseCardAction(realm);
		scn.FreepsChooseCard(grimir);
		assertEquals(grimirStrength + 1, scn.GetStrength(grimir));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		scn.FreepsChooseNo();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
