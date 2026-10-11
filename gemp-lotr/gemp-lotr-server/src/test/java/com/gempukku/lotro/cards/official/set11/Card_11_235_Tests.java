package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_235_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					// Don't Look at Them: [gollum] Free Peoples condition played to the support area, cost 1
					put("dontlook", "6_39");
					// Sméagol, Always Helps: [gollum] Free Peoples companion, cost 0.  "To play, add a burden."
					put("smeagol", "7_71");
					// Fishing Boat: [gollum] Free Peoples possession, cost 2.  "Bearer must be Sméagol."  It may
					// reinforce a [gollum] token when played, and there is none.
					put("boat", "13_48");
					// Still Far Ahead: [gollum] Free Peoples event, Fellowship or Regroup, cost 0.  "Spot Sméagol
					// to play the fellowship's next site ..."
					put("ahead", "8_29");
					put("slippery", "6_43");    // Not Listening: [gollum] Free Peoples Response event
					put("clever", "7_54");      // Clever Hobbits: [gollum] Free Peoples Skirmish event
					// Evil-smelling Fens: a [gollum] SHADOW condition played to the support area, cost 1
					put("fens", "5_22");
					// Sam, Son of Hamfast: a [shire] Free Peoples companion, cost 2, nothing needed to play him
					put("sam", "1_311");
					// Smeagol, Once Like Us: the [shire] Smeagol, cost 1.  "To play, add a burden."
					put("shireSmeagol", "103_116");
				}},
				// Dammed Gate-stream as the site the game starts at, on a Shadows-block path
				new HashMap<>() {{
					putAll(VirtualTableScenario.ShadowsSites);
					put("site1", "11_235");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void DammedGatestreamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Dammed Gate-stream
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 3
		 * Direction: Left
		 * Game Text: <b>Marsh</b>. At the start of your fellowship phase, you may play a [gollum] Free Peoples card from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("Dammed Gate-stream");

		assertEquals("Dammed Gate-stream", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MARSH));
	}

	@Test
	public void AtTheStartOfTheFellowshipPhaseAGollumFreePeoplesCardMayBePlayedFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dontlook = scn.GetFreepsCard("dontlook");
		var smeagol = scn.GetFreepsCard("smeagol");
		var boat = scn.GetFreepsCard("boat");
		var ahead = scn.GetFreepsCard("ahead");
		var slippery = scn.GetFreepsCard("slippery");
		var clever = scn.GetFreepsCard("clever");
		var fens = scn.GetFreepsCard("fens");
		var sam = scn.GetFreepsCard("sam");
		var shireSmeagol = scn.GetFreepsCard("shireSmeagol");
		var site1 = scn.GetFreepsSite("Dammed Gate-stream");

		scn.StartGame(site1);

		assertSame(site1, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DECK, dontlook, smeagol, boat, ahead, slippery, clever, fens, sam, shireSmeagol);

		// Enough in the pool that the Shadow condition could be paid for.
		scn.SetTwilight(3);
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();

		// A companion and a condition may be played.  The Boat and Still Far Ahead need a Sméagol in play, a
		// Response event and a Skirmish event are not played in a fellowship phase, the Fens are a Shadow
		// card, and Sam and the other Smeagol are [shire].
		assertTrue(scn.FreepsHasCardChoiceAvailable(dontlook, smeagol));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boat, ahead, slippery, clever, fens, sam, shireSmeagol));
		assertEquals(2, scn.FreepsGetSelectableCount());
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(dontlook);

		assertInZone(Zone.SUPPORT, dontlook);
		assertInZone(Zone.DECK, smeagol);
		// 3 + 1 for the condition
		assertEquals(4, scn.GetTwilight());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.GetFreepsDeckShuffleCount() > shuffles);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithSmeagolInPlayHisPossessionAndAFellowshipEventThatSpotsHimMayBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var dontlook = scn.GetFreepsCard("dontlook");
		var boat = scn.GetFreepsCard("boat");
		var ahead = scn.GetFreepsCard("ahead");
		var slippery = scn.GetFreepsCard("slippery");
		var clever = scn.GetFreepsCard("clever");
		var site1 = scn.GetFreepsSite("Dammed Gate-stream");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame(site1);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();

		assertTrue(scn.FreepsHasCardChoiceAvailable(dontlook, boat, ahead));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(slippery, clever));

		scn.FreepsChooseCardBPFromSelection(boat);

		assertAttachedTo(boat, smeagol);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.GetFreepsDeckShuffleCount() > shuffles);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningPlaysNothingAndTheDrawDeckIsNotLookedThrough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dontlook = scn.GetFreepsCard("dontlook");
		var smeagol = scn.GetFreepsCard("smeagol");
		var site1 = scn.GetFreepsSite("Dammed Gate-stream");

		scn.StartGame(site1);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DECK, dontlook, smeagol);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ThePlayerMayLookAndPlayNothingAndTheDeckIsStillReshuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dontlook = scn.GetFreepsCard("dontlook");
		var smeagol = scn.GetFreepsCard("smeagol");
		var site1 = scn.GetFreepsSite("Dammed Gate-stream");

		scn.StartGame(site1);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(dontlook, smeagol));
		scn.FreepsDeclineChoosing();

		assertInZone(Zone.DECK, dontlook, smeagol);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoGollumFreePeoplesCardInTheDrawDeckTheOfferIsStillMadeAndTheLookFindsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var site1 = scn.GetFreepsSite("Dammed Gate-stream");
		// The Fens, Sam and the [shire] Smeagol are left in the draw deck.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("dontlook"), scn.GetFreepsCard("smeagol"), scn.GetFreepsCard("boat"),
				scn.GetFreepsCard("ahead"), scn.GetFreepsCard("slippery"), scn.GetFreepsCard("clever"));

		scn.StartGame(site1);

		assertEquals(3, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		scn.SetTwilight(3);

		scn.FreepsAcceptOptionalTrigger();
		// The deck is shown, and nothing in it may be chosen.
		scn.FreepsDismissRevealedCards();

		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OfferedToThePlayerWhoseFellowshipIsAtTheSiteAndNotOnceItHasMovedOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var site1 = scn.GetFreepsSite("Dammed Gate-stream");
		var ruins = scn.GetShadowSite("site5");     // Sirannon Ruins: a river, and no other text
		var shadowSmeagol = scn.GetShadowCard("smeagol");

		scn.StartGame(site1);

		// The first player's turn: he is offered, declines, and moves on to site 2.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);
		assertEquals(2, scn.FreepsGetSiteNumber());

		// The second player's turn begins at site 1, which is the first player's card: the offer is the
		// second player's, to play from his own draw deck.
		scn.SkipToOtherPlayersTurn();
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertSame(site1, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.MoveCardsToTopOfDeck(shadowSmeagol);
		assertEquals(0, scn.ShadowGetBurdens());
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(shadowSmeagol);
		// "To play, add a burden": his own Sméagol, at his own cost.
		assertInZone(Zone.FREE_CHARACTERS, shadowSmeagol);
		assertEquals(1, scn.ShadowGetBurdens());
		assertEquals(0, scn.FreepsGetBurdens());
		assertNotInZone(Zone.FREE_CHARACTERS, scn.GetFreepsCard("smeagol"));

		// The first player's second turn begins at site 2.  Dammed Gate-stream lies behind him, and nothing
		// is offered.
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertSame(site1, scn.GetSite(1));
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
