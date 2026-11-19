package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_363_Tests
{

	// Palantír Chamber is site 9 of the Towers sites.  Each test skips to site 8, lays the board there and moves on,
	// so that it acts in the Shadow phase at site 9.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, site 5; Damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, site 4; no text
					put("troop2", "1_177");
					put("pinned", "1_192");     // Pinned Down: a Shadow condition
					put("scimitar", "1_180");   // Goblin Scimitar: a Shadow possession
					// Uruk Pursuer: [isengard] Uruk-hai, cost 2; laid in the Shadow player's hand
					put("pursuer", "4_190");
					// Moria Scout: [moria] Orc, cost 2; laid only in the other player's discard pile
					put("scout", "1_191");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PalantirChamberStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Palantír Chamber
		 * Unique: False
		 * Shadow Number: 9
		 * Type: Site
		 * Block: Towers
		 * Direction: Left
		 * Site Number: 9T
		 * Game Text: <b>Shadow:</b> Remove 2 burdens to play a minion from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Palantír Chamber", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void RemovesTwoBurdensToPlayAMinionFromTheShadowPlayersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var pinned = scn.GetShadowCard("pinned");
		var scimitar = scn.GetShadowCard("scimitar");
		var pursuer = scn.GetShadowCard("pursuer");
		var freepsScout = scn.GetFreepsCard("scout");

		scn.StartGame();
		scn.SkipToSite(8);
		// The turns skipped have drawn cards: every pile is laid now. A pile's choice is read by blueprint, so the
		// other player's minion in his own discard pile is of a title that lies in no discard pile of this player.
		scn.MoveCardsToDiscard(savage, troop, pinned, scimitar, freepsScout);
		scn.MoveCardsToHand(pursuer);
		scn.AddBurdens(3);
		scn.SetTwilight(10);
		assertInZone(Zone.DISCARD, savage, troop, pinned, scimitar, freepsScout);
		assertInHand(pursuer);
		scn.FreepsPass();

		var chamber = scn.GetCurrentSite();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals("Palantír Chamber", chamber.getBlueprint().getTitle());
		assertEquals(3, scn.FreepsGetBurdens());
		assertTrue(scn.ShadowActionAvailable(chamber));

		scn.ShadowUseCardAction(chamber);
		// Two burdens, not all of them.
		assertEquals(1, scn.FreepsGetBurdens());

		// Minions of two cultures and races; not a Shadow condition or possession in the same pile, a minion in
		// hand, or the other player's minion in his own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(pinned, scimitar, pursuer, freepsScout));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(troop);

		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.DISCARD, savage, pinned, scimitar, freepsScout);
		assertInHand(pursuer);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOneBurdenAndIsOnceThereAreTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCardsToDiscard(savage);
		scn.AddBurdens(1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var chamber = scn.GetCurrentSite();
		assertEquals("Palantír Chamber", chamber.getBlueprint().getTitle());
		assertEquals(1, scn.FreepsGetBurdens());
		assertInDiscard(savage);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(chamber));

		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.FreepsGetBurdens());
		assertTrue(scn.ShadowActionAvailable(chamber));
	}

	@Test
	public void IsNotOfferedWithNoMinionInTheDiscardPileOrNoneThePoolCanPayFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var pinned = scn.GetShadowCard("pinned");
		var scimitar = scn.GetShadowCard("scimitar");
		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.SkipToSite(8);
		// No minion in the discard pile: a Shadow condition and possession there, a minion in hand.
		scn.MoveCardsToDiscard(pinned, scimitar);
		scn.MoveCardsToHand(pursuer);
		scn.AddBurdens(2);
		scn.FreepsPass();

		var chamber = scn.GetCurrentSite();
		assertEquals("Palantír Chamber", chamber.getBlueprint().getTitle());
		assertEquals(2, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(pursuer));
		assertFalse(scn.ShadowActionAvailable(chamber));

		// The Troop costs (6): with (5) it cannot be played, with (6) it can.
		scn.MoveCardsToDiscard(troop);
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(chamber));

		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(chamber));
		assertEquals(2, scn.FreepsGetBurdens());
	}

	@Test
	public void IsAShadowPhaseActionAndIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCardsToDiscard(savage);
		scn.AddBurdens(2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var chamber = scn.GetCurrentSite();
		assertEquals("Palantír Chamber", chamber.getBlueprint().getTitle());
		assertTrue(scn.ShadowActionAvailable(chamber));
		assertEquals(2, scn.FreepsGetBurdens());
		assertInDiscard(savage);
		// A minion in play, so that there is a maneuver phase.
		scn.MoveMinionsToTable(troop);
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(chamber));
		assertEquals(2, scn.FreepsGetBurdens());
		assertInDiscard(savage);
	}
}
