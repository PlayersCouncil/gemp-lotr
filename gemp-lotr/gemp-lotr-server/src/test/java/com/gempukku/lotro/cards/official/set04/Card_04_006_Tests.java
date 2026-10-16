package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("threatening", "4_6");

					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, cost 2, site 3, no text
					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, cost 4, site 3, no text
					// Dunlending Madman: [dunland] Man, cost 2, site 3; a trigger on winning a skirmish.  Only the Free
					// Peoples player's copy is laid, in his own discard pile.
					put("madman", "4_12");
					// Saruman, Rabble-rouser: the one [dunland] minion who is no Man (a Wizard), cost 4, site 4.
					put("saruman", "4_33");
					// Easterling Axeman: [raider] Man, cost 2, site 4; strength +3 and fierce only at 2 burdens.
					put("axeman", "4_224");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ConstantlyThreateningStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Constantly Threatening
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * <b>Shadow:</b> If the fellowship has moved more than once this turn, remove (2) to play a [dunland] Man from your discard pile.
		 */

		var scn = GetScenario();

		var threatening = scn.GetFreepsCard("threatening");

		assertEquals("Constantly Threatening", threatening.getBlueprint().getTitle());
		assertNull(threatening.getBlueprint().getSubtitle());
		assertFalse(threatening.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, threatening.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, threatening.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, threatening.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(threatening, Keyword.SUPPORT_AREA));
		assertEquals(1, threatening.getBlueprint().getTwilightCost());
	}

	@Test
	public void OfferedOnlyOnceTheFellowshipHasMovedTwiceThisTurnItPlaysADunlandManFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threatening = scn.GetShadowCard("threatening");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		var madman = scn.GetFreepsCard("madman");
		scn.MoveCardsToSupportArea(threatening);
		// A [dunland] Man in the other player's discard pile, of a title in none of the Shadow player's piles.
		scn.MoveCardsToDiscard(brigand, savage, saruman, axeman, madman);

		scn.StartGame();
		assertInDiscard(brigand, savage, saruman, axeman, madman);
		scn.SetTwilight(10);

		// The first move of the turn, to site 2.
		scn.FreepsPass();
		// 10 set + 2 for Ettenmoors + 1 companion
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(threatening));

		// No minion is played: straight to the regroup phase, and the fellowship moves again, to site 3.
		scn.ShadowPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 13 + 0 for Rivendell Valley + 1 companion
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(threatening));

		scn.ShadowUseCardAction(threatening);
		// Two [dunland] Men of different titles are offered; the [dunland] Wizard, the [raider] Man and the other
		// player's [dunland] Man are not, though the pool could pay for each.
		assertTrue(scn.ShadowHasCardChoiceAvailable(brigand, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman, axeman, madman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(savage);

		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInDiscard(brigand, saruman, axeman, madman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ThePoolMustPayForTheRemovalAndTheDunlandManTogether() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threatening = scn.GetShadowCard("threatening");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(threatening);
		scn.MoveCardsToDiscard(brigand);

		scn.StartGame();
		assertInDiscard(brigand);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());

		// Moved twice.  2 to remove and 2 for the Brigand (site 3: not roaming at site 3): 3 is one short.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(threatening));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(threatening));

		// The one [dunland] Man there is played unasked.
		scn.ShadowUseCardAction(threatening);
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		// 4 - 2 removed - 2 for the Brigand
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithNoDunlandManInYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threatening = scn.GetShadowCard("threatening");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		var madman = scn.GetFreepsCard("madman");
		scn.MoveCardsToSupportArea(threatening);
		scn.MoveCardsToDiscard(saruman, axeman, madman);

		scn.StartGame();
		assertInDiscard(saruman, axeman, madman);
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());

		// Moved twice, and the pool could pay for any of them: the [dunland] Wizard, the [raider] Man and the
		// other player's [dunland] Man are no [dunland] Man in your discard pile.
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(threatening));
	}
}
