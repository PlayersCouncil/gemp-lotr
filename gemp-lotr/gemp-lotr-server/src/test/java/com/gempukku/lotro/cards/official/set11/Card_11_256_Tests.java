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

public class Card_11_256_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Ranger of Osgiliath: a [gondor] Man companion, a ranger, not unique, cost 2.  His regroup
					// ability is not used.
					put("osgiliath", "7_116");
					// Arwen, Daughter of Elrond: an [elven] Elf companion, a ranger, unique, cost 2.  Strength +3
					// against a Nazgul, and none is here.
					put("arwen", "1_30");
					// Gondorian Knight: a [gondor] Man companion of cost 2 who is a knight and no ranger; no other text
					put("knight", "5_35");
				}},
				new HashMap<>() {{
					put("pony", "11_256");
					put("site2", "13_185");
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void ThePrancingPonyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: The Prancing Pony
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: <b>Dwelling</b>. At the start of your fellowship phase, you may add a burden to play a ranger from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pony");

		assertEquals("The Prancing Pony", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.DWELLING));
	}

	@Test
	public void ABurdenIsAddedToPlayARangerFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var osgiliath = scn.GetFreepsCard("osgiliath");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		var pony = scn.GetFreepsCard("pony");

		scn.StartGame(pony);

		assertSame(pony, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertInZone(Zone.DECK, osgiliath, arwen, knight);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertEquals(0, scn.GetBurdens());

		scn.FreepsAcceptOptionalTrigger();

		// The burden is paid before the deck is looked at.
		assertEquals(1, scn.GetBurdens());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());

		scn.FreepsDismissRevealedCards();

		// A [gondor] Man ranger and an [elven] Elf ranger may be played; a [gondor] Man who is no ranger may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(osgiliath, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(osgiliath);

		assertInZone(Zone.FREE_CHARACTERS, osgiliath);
		assertInZone(Zone.DECK, arwen, knight);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetBurdens());
		// Shuffled as the ranger leaves the deck that was looked through, and again by the card's own shuffle.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningAddsNoBurdenAndTheOtherPlayerIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var osgiliath = scn.GetFreepsCard("osgiliath");
		scn.MoveCardsToTopOfDeck(osgiliath);
		var pony = scn.GetFreepsCard("pony");

		scn.StartGame(pony);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(0, scn.GetBurdens());

		scn.FreepsDeclineOptionalTrigger();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.DECK, osgiliath);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheBurdenIsAddedAndThePlayerMayStillPlayNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var osgiliath = scn.GetFreepsCard("osgiliath");
		var pony = scn.GetFreepsCard("pony");

		scn.StartGame(pony);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertEquals(0, scn.GetBurdens());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(osgiliath));
		scn.FreepsDeclineChoosing();

		assertInZone(Zone.DECK, osgiliath);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoRangerInTheDrawDeckTheOfferIsStillMadeAndTheBurdenBuysALookThatFindsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		// Both rangers are taken out of the deck; the Gondorian Knight is left in it.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("osgiliath"), scn.GetFreepsCard("arwen"));
		var pony = scn.GetFreepsCard("pony");

		scn.StartGame(pony);

		assertInZone(Zone.DECK, knight);
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(0, scn.GetBurdens());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		// The deck is shown, and nothing in it may be chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, false));
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DECK, knight);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OfferedToThePlayerWhoseFellowshipIsAtThePonyAndNotOnceItHasMovedOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var osgiliath = scn.GetFreepsCard("osgiliath");
		var shadowOsgiliath = scn.GetShadowCard("osgiliath");

		var pony = scn.GetFreepsCard("pony");
		var ruins = scn.GetShadowSite("site5");     // Sirannon Ruins: a river, and no other text

		scn.StartGame(pony);

		// The first player's turn: he is offered and declines.  The second player is offered nothing on a
		// turn that is not his.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// He moves on to site 2.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);
		assertEquals(2, scn.FreepsGetSiteNumber());

		// The second player's turn begins at site 1, which is the first player's card: the offer is the
		// second player's.  The burden goes on his own Ring-bearer and the ranger comes from his own deck.
		scn.SkipToOtherPlayersTurn();
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertSame(pony, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// The reconcile at the end of the first player's turn drew most of his deck.
		scn.MoveCardsToTopOfDeck(shadowOsgiliath);
		int shadowBurdens = scn.ShadowGetBurdens();
		int freepsBurdens = scn.FreepsGetBurdens();
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(shadowBurdens + 1, scn.ShadowGetBurdens());
		assertEquals(freepsBurdens, scn.FreepsGetBurdens());
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(shadowOsgiliath);
		assertInZone(Zone.FREE_CHARACTERS, shadowOsgiliath);
		assertNotInZone(Zone.FREE_CHARACTERS, osgiliath);

		// The first player's second turn begins at site 2, with the Pony on the path behind him: nothing
		// is offered.
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertSame(pony, scn.GetSite(1));
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
