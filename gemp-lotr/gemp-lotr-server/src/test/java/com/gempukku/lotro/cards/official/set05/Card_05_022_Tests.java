package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_022_Tests
{

	// FellowshipSites: none of its sites is a marsh as printed.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fens", "5_22");
					put("fens2", "5_22");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] Gollum, vitality 4; a regroup ability used only on purpose
					put("wraithgollum", "103_66"); // Gollum, Half a Wraith Himself: [wraith] Gollum, vitality 4; answers a Nazgul played (none is), an assignment ability
					put("shelob", "8_25");      // Shelob, Eater of Light: a [gollum] minion who is no Gollum, vitality 8; her play trigger is not met when cheated in
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, site 5; damage +1 is its whole text
					put("dunlending", "4_17");  // Dunlending Savage: [dunland] Man, cost 4, site 3, no text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, site 4, no text
					put("pinned", "1_192");     // Pinned Down: [moria] condition, cost 1, plays to the support area
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EvilsmellingFensStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Evil-smelling Fens
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>Each site on the adventure path is a <b>marsh</b>.<br><b>Shadow:</b> Exert Gollum twice and discard this condition to play a minion from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fens");

		assertEquals("Evil-smelling Fens", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhileInPlayEachSiteOnTheAdventurePathIsAMarshAndASiteOffThePathIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fens = scn.GetShadowCard("fens");
		scn.MoveCardsToHand(fens);

		scn.StartGame();
		var freepsSite3 = scn.GetFreepsSite(3);
		scn.FreepsPass();

		// Site 1 is the Free Peoples player's, site 2 the Shadow player's; site 3 is still in an adventure deck.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetSite(1), Keyword.MARSH));
		assertFalse(scn.HasKeyword(scn.GetSite(2), Keyword.MARSH));
		assertFalse(scn.HasKeyword(freepsSite3, Keyword.MARSH));
		assertTrue(scn.ShadowPlayAvailable(fens));

		scn.ShadowPlayCard(fens);
		assertInZone(Zone.SUPPORT, fens);
		assertTrue(scn.HasKeyword(scn.GetSite(1), Keyword.MARSH));
		assertTrue(scn.HasKeyword(scn.GetSite(2), Keyword.MARSH));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite3);
		assertFalse(scn.HasKeyword(freepsSite3, Keyword.MARSH));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A site laid afterwards is a marsh as well.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetSite(3), Keyword.MARSH));
		assertTrue(scn.HasKeyword(scn.GetSite(1), Keyword.MARSH));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityExertsGollumTwiceAndDiscardsThisCopyToPlayAMinionFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fens = scn.GetShadowCard("fens");
		var fens2 = scn.GetShadowCard("fens2");
		var gollum = scn.GetShadowCard("gollum");
		var savage = scn.GetShadowCard("savage");
		var dunlending = scn.GetShadowCard("dunlending");
		var pinned = scn.GetShadowCard("pinned");
		var freepsTroop = scn.GetFreepsCard("troop");
		scn.MoveCardsToSupportArea(fens, fens2);
		// In his own discard pile: an [isengard] Uruk-hai, a [dunland] Man and a Shadow condition.
		scn.MoveCardsToDiscard(savage, dunlending, pinned);
		// A minion in the OTHER player's discard pile (his own Goblin Patrol Troop lies in his draw deck).
		scn.MoveCardsToDiscard(freepsTroop);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(gollum);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.HasKeyword(scn.GetSite(2), Keyword.MARSH));
		assertTrue(scn.ShadowActionAvailable(fens));

		scn.ShadowUseCardAction(fens);
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertInDiscard(fens);
		assertInZone(Zone.SUPPORT, fens2);
		// The other copy keeps every site a marsh.
		assertTrue(scn.HasKeyword(scn.GetSite(2), Keyword.MARSH));

		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, dunlending));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(pinned, freepsTroop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(dunlending);

		assertInZone(Zone.SHADOW_CHARACTERS, dunlending);
		assertInDiscard(savage, pinned, freepsTroop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void GollumOfTheWraithCulturePaysTheTwoExertionsToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fens = scn.GetShadowCard("fens");
		var gollum = scn.GetShadowCard("wraithgollum");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(fens);
		scn.MoveCardsToDiscard(savage);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(gollum);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.ShadowActionAvailable(fens));

		scn.ShadowUseCardAction(fens);
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertInDiscard(fens);
		// The only minion there is played without a question.
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		// With the Fens gone, the sites are no marsh.
		assertFalse(scn.HasKeyword(scn.GetSite(2), Keyword.MARSH));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedUnlessGollumCanExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fens = scn.GetShadowCard("fens");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(fens);
		scn.MoveCardsToDiscard(savage);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		// Shelob, a [gollum] minion who is no Gollum, could exert twice; Gollum, with 2 wounds of 4, once only.
		scn.MoveMinionsToTable(gollum, shelob);
		scn.AddWoundsToChar(gollum, 2);
		scn.ShadowRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(fens));

		scn.RemoveWoundsFromChar(gollum, 1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertTrue(scn.ShadowActionAvailable(fens));

		scn.ShadowUseCardAction(fens);
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoMinionInTheDiscardPileThePoolCanPayFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fens = scn.GetShadowCard("fens");
		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");
		var pinned = scn.GetShadowCard("pinned");
		scn.MoveCardsToSupportArea(fens);
		scn.MoveCardsToDiscard(troop, pinned);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(gollum);

		// The troop costs 6, +2 roaming at site 2; the condition is no minion.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(fens));

		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(fens));

		scn.ShadowUseCardAction(fens);
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInDiscard(fens, pinned);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
