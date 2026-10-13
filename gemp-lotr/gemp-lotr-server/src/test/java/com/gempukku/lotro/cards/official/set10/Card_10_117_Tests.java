package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_117_Tests
{
	// The King path with Base of Mindolluin as its site 3.  Nothing on the way there asks anything of the
	// fellowships laid here (Edoras Hall, King's Tent).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a second companion; a maneuver ability only
					put("knight", "5_35");      // Gondorian Knight: a third companion, no text
					put("guard1", "5_83");      // Household Guard: [rohan] Man companion, valiant and nothing else
					put("guard2", "5_83");      // a second Household Guard: with the first, the 2 valiant companions Stronghold of My Fathers asks for

					put("firstlevel", "7_92");  // First Level: [gondor] fortification, support area, cost 1; a maneuver ability only
					put("arrowslits", "5_80");  // Arrow-slits: [rohan] fortification, support area, cost 1; a maneuver ability only
					put("stronghold", "102_48");// Stronghold of My Fathers: [rohan] POSSESSION, fortification, support area, cost 1; to play, spot 2 valiant companions; a response to a wound only
					put("rallypoint", "5_38");  // Rally Point: [gondor] condition, support area, cost 1, and no fortification
				}},
				new HashMap<>()
				{{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "10_117");     // Base of Mindolluin
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BaseofMindolluinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Base of Mindolluin
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 3K
		 * Shadow Number: 0
		 * Direction: Right
		 * Game Text: <b>Sanctuary</b>. <b>Fellowship:</b> If you cannot spot 2 threats, add a threat to play a
		 * fortification from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("Base of Mindolluin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(3, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void AtOneThreatAddingAThreatPlaysAFortificationOfEitherCultureFromTheDrawDeckAndShufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var firstlevel = scn.GetFreepsCard("firstlevel");
		var arrowslits = scn.GetFreepsCard("arrowslits");
		var stronghold = scn.GetFreepsCard("stronghold");
		var rallypoint = scn.GetFreepsCard("rallypoint");
		scn.MoveCompanionsToTable(guard1, guard2);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsDrawDeck(firstlevel, arrowslits, stronghold, rallypoint);
		scn.AddThreats(1);
		// Burdens beside the threat: they are not threats, and do not count toward the 2.
		scn.AddBurdens(2);
		// The fellowship decision the skip ended on was worked out before these cheats.
		scn.FreepsRefreshDecision();
		var site = scn.GetCurrentSite();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetBurdens());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(site));

		scn.FreepsUseCardAction(site);
		// The threat is the cost, paid before the deck is looked through.
		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetBurdens());
		scn.FreepsDismissRevealedCards();

		// A fortification of each culture and of each type may be played (Stronghold is a possession, the others
		// conditions); a [gondor] condition that is no fortification may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(firstlevel, arrowslits, stronghold));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rallypoint));
		// A play from a draw deck that has been looked through may find nothing (F-0042).
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(arrowslits);

		assertInZone(Zone.SUPPORT, arrowslits);
		assertInZone(Zone.DECK, firstlevel, stronghold, rallypoint);
		// The deck looked through is shuffled; the Shadow player's is not.
		assertTrue(scn.GetFreepsDeckShuffleCount() > freepsShuffles);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoFortificationInTheDeckItIsStillUsedAndTheThreatIsAddedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var rallypoint = scn.GetFreepsCard("rallypoint");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsDrawDeck(rallypoint);
		// The fellowship decision the skip ended on was worked out before these cheats.
		scn.FreepsRefreshDecision();
		var site = scn.GetCurrentSite();

		assertEquals(0, scn.GetThreats());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		// Failing to find a card in a draw deck costs nothing (Q-0019), so the action is offered.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(site));

		scn.FreepsUseCardAction(site);
		assertEquals(1, scn.GetThreats());
		// The deck is shown, and nothing in it can be chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rallypoint, false));
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DECK, rallypoint);
		// One shuffle, the action's own, with nothing played out of the deck.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhenTwoThreatsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var firstlevel = scn.GetFreepsCard("firstlevel");
		scn.MoveCompanionsToTable(aragorn, knight);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsDrawDeck(firstlevel);
		// Three companions: a third threat could still be added.
		scn.AddThreats(2);
		// The fellowship decision the skip ended on was worked out before these cheats.
		scn.FreepsRefreshDecision();
		var site = scn.GetCurrentSite();

		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(site));
	}

	@Test
	public void NotOfferedWhenAThreatCannotBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firstlevel = scn.GetFreepsCard("firstlevel");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsDrawDeck(firstlevel);
		// Frodo is the only companion, and there are as many threats as companions.
		scn.AddThreats(1);
		// The fellowship decision the skip ended on was worked out before these cheats.
		scn.FreepsRefreshDecision();
		var site = scn.GetCurrentSite();

		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(site));
	}
}
