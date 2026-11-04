package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_007_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ready", "6_7");

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

	protected VirtualTableScenario GetBreachedWallScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ready", "6_7");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, cost 2, site 3, no text
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					// Breached Wall: a Towers site 5, Shadow number 6, battleground; "The count of sites each Shadow
					// player controls is +1."  The fellowship's path otherwise as usual.
					put("site5", "102_70");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ReadytoFallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Ready to Fall
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * <b>Shadow:</b> Spot 2 sites you control and remove (2) to play a [dunland] Man from your discard pile.
		*/

		var scn = GetScenario();

		var ready = scn.GetFreepsCard("ready");

		assertEquals("Ready to Fall", ready.getBlueprint().getTitle());
		assertNull(ready.getBlueprint().getSubtitle());
		assertFalse(ready.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, ready.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, ready.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, ready.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(ready, Keyword.SUPPORT_AREA));
		assertEquals(1, ready.getBlueprint().getTwilightCost());
	}

	@Test
	public void OfferedOnlyWithTwoSitesYouControlItPlaysADunlandManFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ready = scn.GetShadowCard("ready");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		var madman = scn.GetFreepsCard("madman");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToSupportArea(ready);
		// A [dunland] Man in the other player's discard pile, of a title in none of the Shadow player's piles.
		scn.MoveCardsToDiscard(brigand, savage, saruman, axeman, madman);
		assertInDiscard(brigand, savage, saruman, axeman, madman);
		// Sites 1 and 2: one the Shadow player's, one the Free Peoples player's.
		scn.ShadowTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());

		// To The Bridge of Khazad-dum, site 5: no Dunland minion, of site 3, roams there.
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		// One site you control, and one the other player controls: not 2.
		assertFalse(scn.ShadowActionAvailable(ready));

		// Site 3, the Shadow player's second.
		scn.ShadowTakeControlOfSite();
		assertEquals(2, scn.GetShadowControlledSiteCount());
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(ready));

		scn.ShadowUseCardAction(ready);
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

		var ready = scn.GetShadowCard("ready");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToSupportArea(ready);
		scn.MoveCardsToDiscard(brigand);
		assertInDiscard(brigand);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertEquals(2, scn.GetShadowControlledSiteCount());
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());

		// 2 to remove and 2 for the Brigand (not roaming at site 5): 3 is one short.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ready));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(ready));

		// The one [dunland] Man there is played unasked.
		scn.ShadowUseCardAction(ready);
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		// 4 - 2 removed - 2 for the Brigand
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithNoDunlandManInYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ready = scn.GetShadowCard("ready");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		var madman = scn.GetFreepsCard("madman");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToSupportArea(ready);
		scn.MoveCardsToDiscard(saruman, axeman, madman);
		assertInDiscard(saruman, axeman, madman);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertEquals(2, scn.GetShadowControlledSiteCount());
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		// Two sites controlled, and the pool could pay for any of them: the [dunland] Wizard, the [raider] Man and
		// the other player's [dunland] Man are no [dunland] Man in your discard pile.
		assertEquals(20, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ready));
	}

	@Test
	public void AtBreachedWallOneSiteControlledIsSpottedAsTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBreachedWallScenario();

		var ready = scn.GetShadowCard("ready");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToSupportArea(ready);
		scn.MoveCardsToDiscard(brigand);
		assertInDiscard(brigand);
		scn.ShadowTakeControlOfSite();
		assertEquals(1, scn.GetShadowControlledSiteCount());

		// To Breached Wall: one site really controlled, and one more by the Wall's count.
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals("Breached Wall", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(ready));

		scn.ShadowUseCardAction(ready);
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
