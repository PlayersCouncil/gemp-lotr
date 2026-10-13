package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_118_Tests
{

	// The Fellowship sites, with The Great River as site 7 in place of Anduin Confluence.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "17_17");     // Gandalf, Returned: when played, may play a [gandalf] possession on him from the draw deck or discard pile
					put("pipe", "1_74");         // Gandalf's Pipe: [gandalf] possession, cost 0, bearer must be Gandalf; its ability is used only on purpose
					put("helpus", "5_117");      // You Must Help Us: Fellowship event, "Play Smeagol from your discard pile to heal each Ring-bound Hobbit twice"
					put("smeagol", "5_29");      // Smeagol, Slinker: [gollum] companion, to play add a burden; a skirmish ability only

					put("captured", "7_53");     // Captured by the Ring: Shadow event, "Play Gollum from your draw deck or discard pile to add a threat"
					put("gollum", "5_25");       // Gollum, Stinker: [gollum] minion, cost 2; strength +1 per burden, an optional trigger on winning
					put("host", "1_187");        // Host of Thousands: Shadow event, "Play a [moria] Orc from your discard pile"
					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc, cost 6, no text
					put("troop2", "1_177");
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site7", "3_118");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheGreatRiverStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: The Great River
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 7
		 * Shadow Number: 6
		 * Direction: Right
		 * Game Text: <b>River</b>. While the fellowship is at The Great River, cards may not be played from draw decks or discard piles.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(7);

		assertEquals("The Great River", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(7, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void AtTheGreatRiverTheFreePeoplesPlayerPlaysNothingFromHisDrawDeckOrDiscardPileButPlaysFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var pipe = scn.GetFreepsCard("pipe");
		var helpus = scn.GetFreepsCard("helpus");
		var smeagol = scn.GetFreepsCard("smeagol");

		scn.StartGame();
		scn.SkipToSite(7);
		assertEquals("The Great River", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SetFreepsHand(gandalf, helpus);
		scn.MoveCardsToDiscard(smeagol);
		scn.SetFreepsDrawDeck(pipe);
		scn.FreepsRefreshDecision();

		// Smeagol could be played from the discard pile anywhere else (the last test), and You Must Help Us is the
		// play of him: not offered here.
		assertInZone(Zone.DISCARD, smeagol);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(helpus));

		// A card from hand is played as usual; the pipe in the draw deck cannot follow him in.
		assertTrue(scn.FreepsPlayAvailable(gandalf));
		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		assertInZone(Zone.DECK, pipe);
		assertInZone(Zone.DISCARD, smeagol);
		assertInZone(Zone.HAND, helpus);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheGreatRiverTheShadowPlayerPlaysNothingFromHisDrawDeckOrDiscardPileButPlaysFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captured = scn.GetShadowCard("captured");
		var gollum = scn.GetShadowCard("gollum");
		var host = scn.GetShadowCard("host");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SetShadowHand(captured, host, troop2);
		scn.MoveCardsToDiscard(troop);
		scn.SetShadowDrawDeck(gollum);
		scn.SetTwilight(2);
		// The fellowship moves from site 6 to The Great River.
		scn.FreepsPass();

		assertEquals("The Great River", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// 2 + 6 for The Great River + 1 companion: enough for Gollum (2) and for the Troop (6).
		assertEquals(9, scn.GetTwilight());
		// Gollum in the draw deck, and a [moria] Orc in the discard pile: neither event is offered.
		assertInZone(Zone.DECK, gollum);
		assertInZone(Zone.DISCARD, troop);
		assertFalse(scn.ShadowPlayAvailable(captured));
		assertFalse(scn.ShadowPlayAvailable(host));

		// A minion from hand is played as usual.
		assertTrue(scn.ShadowPlayAvailable(troop2));
		scn.ShadowPlayCard(troop2);
		assertInZone(Zone.SHADOW_CHARACTERS, troop2);
		assertInZone(Zone.DECK, gollum);
		assertInZone(Zone.DISCARD, troop);
		assertInZone(Zone.HAND, captured, host);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtTheNextSiteWithTheGreatRiverBehindTheFreePeoplesPlayerPlaysFromHisDrawDeckAndDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var pipe = scn.GetFreepsCard("pipe");
		var helpus = scn.GetFreepsCard("helpus");
		var smeagol = scn.GetFreepsCard("smeagol");

		scn.StartGame();
		scn.SkipToSite(8);
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals("The Great River", scn.GetSite(7).getBlueprint().getTitle());
		scn.SetFreepsHand(gandalf, helpus);
		scn.MoveCardsToDiscard(smeagol);
		scn.SetFreepsDrawDeck(pipe);
		scn.FreepsRefreshDecision();

		// Smeagol from the discard pile.
		assertTrue(scn.FreepsPlayAvailable(helpus));
		scn.FreepsPlayCard(helpus);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);

		// The pipe from the draw deck, onto Gandalf as he is played.
		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(pipe);
		assertAttachedTo(pipe, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheNextSiteWithTheGreatRiverBehindTheShadowPlayerPlaysFromHisDrawDeckAndDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captured = scn.GetShadowCard("captured");
		var gollum = scn.GetShadowCard("gollum");
		var host = scn.GetShadowCard("host");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.SetShadowHand(captured, host);
		scn.MoveCardsToDiscard(troop);
		scn.SetShadowDrawDeck(gollum);
		scn.SetTwilight(2);
		// The fellowship moves on from The Great River to site 8.
		scn.FreepsPass();

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals("The Great River", scn.GetSite(7).getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Gollum from the draw deck.
		assertTrue(scn.ShadowPlayAvailable(captured));
		scn.ShadowPlayCard(captured);
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);

		// A [moria] Orc from the discard pile.
		assertTrue(scn.ShadowPlayAvailable(host));
		scn.ShadowPlayCard(host);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
