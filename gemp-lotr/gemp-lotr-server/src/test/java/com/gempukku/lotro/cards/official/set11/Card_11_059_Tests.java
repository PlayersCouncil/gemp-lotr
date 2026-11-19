package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "11_59");

					// [gondor] Men of each type, and characters who fail one word of "a [gondor] Man".
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, no ranger; "Knight." is his whole text
					put("knight2", "5_35");
					put("ranger", "7_116");     // Ranger of Osgiliath: [gondor] Man companion, a ranger; a regroup ability used only on purpose
					put("ranger2", "7_116");
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally; a maneuver ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; "Valiant." is his whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with two rivers that differ in another keyword: Buckleberry Ferry 1_330 as site 2 (River;
	// its text needs a fellowship of Hobbits alone) and Ford of Bruinen 1_338 as site 3 (River, Sanctuary; its text is
	// about Nazgul played there).  Site 1, Bag End, and site 4, Balin's Tomb, are no rivers.
	protected VirtualTableScenario GetRiverScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "11_59");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, no ranger; "Knight." is his whole text
					put("knight2", "5_35");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_330");
					put("site3", "1_338");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Buckleberry Ferry 1_330 as site 2: a river ("While only Hobbits are in the
	// fellowship, there are no assignment and skirmish phases at Buckleberry Ferry").  The Gondorian Merchant's home
	// is site 3 of the Towers path, so he has none on this one; he is the only [gondor] ally, and no [gondor] ally
	// is a ranger.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "11_59");
					put("blade2", "11_59");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, no ranger; "Knight." is his whole text
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, home 3T; a maneuver ability used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_330");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GondorianBladeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Gondorian Blade
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [gondor] Man.
		 * While bearer is a ranger or at a river site, he or she is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blade");

		assertEquals("Gondorian Blade", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void CanBePlayedOnAGondorManCompanionOrAllyAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var blade = scn.GetFreepsCard("blade");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(knight, ranger, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(blade);
		assertInHand(blade);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(blade));
		scn.FreepsPlayCard(blade);

		// Two [gondor] Man companions and the [gondor] Man ally; not the [gondor] Hobbit, the [rohan] Man or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, ranger, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merchant);

		assertAttachedTo(blade, merchant);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileBearerIsARangerHeIsDamagePlus1AwayFromAnyRiver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var ranger = scn.GetFreepsCard("ranger");
		var ranger2 = scn.GetFreepsCard("ranger2");
		scn.MoveCompanionsToTable(ranger, ranger2);
		assertEquals(0, scn.GetKeywordCount(ranger, Keyword.DAMAGE));

		scn.StartGame();
		assertEquals("Bag End", scn.GetCurrentSite().getBlueprint().getTitle());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		scn.AttachCardsTo(ranger, blade);

		// The bearer is a ranger; the other ranger, who does not bear it, gains nothing.
		assertEquals(1, scn.GetKeywordCount(ranger, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(ranger2, Keyword.DAMAGE));
	}

	@Test
	public void ABearerWhoIsNoRangerIsDamagePlus1OnlyWhileAtARiver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRiverScenario();

		var blade = scn.GetFreepsCard("blade");
		var knight = scn.GetFreepsCard("knight");
		var knight2 = scn.GetFreepsCard("knight2");
		scn.MoveCompanionsToTable(knight, knight2);
		scn.AttachCardsTo(knight, blade);
		assertAttachedTo(blade, knight);

		scn.StartGame();
		assertEquals("Bag End", scn.GetCurrentSite().getBlueprint().getTitle());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DAMAGE));

		scn.SkipToSite(2);
		assertEquals("Buckleberry Ferry", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		assertEquals(1, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		// The other Knight, at the same river, does not bear it.
		assertEquals(0, scn.GetKeywordCount(knight2, Keyword.DAMAGE));

		scn.SkipToSite(3);
		assertEquals("Ford of Bruinen", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertEquals(1, scn.GetKeywordCount(knight, Keyword.DAMAGE));

		scn.SkipToSite(4);
		assertEquals("Balin's Tomb", scn.GetCurrentSite().getBlueprint().getTitle());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BorneByAnAllyWithNoHomeSiteOnThePathItGivesNothingWithTheFellowshipAtARiver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var blade = scn.GetFreepsCard("blade");
		var blade2 = scn.GetFreepsCard("blade2");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(knight);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(knight, blade);
		scn.AttachCardsTo(merchant, blade2);

		scn.StartGame();
		scn.SkipToSite(2);
		assertEquals("Buckleberry Ferry", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));

		// The companion is with the fellowship, at the river.  The Merchant's home is on another block's path: on
		// this one he is at no site (CR 4.2 "ally").
		assertEquals(1, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(merchant, Keyword.DAMAGE));
	}
}
