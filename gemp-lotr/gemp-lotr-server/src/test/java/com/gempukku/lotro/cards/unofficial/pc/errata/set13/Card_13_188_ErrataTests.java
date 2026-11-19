package com.gempukku.lotro.cards.unofficial.pc.errata.set13;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_188_ErrataTests
{
	// The card's own block: the Shadow player lays each next site, Courtyard Parapet among them.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Cards to hold in a hand, of three kinds. Each player's whole draw deck is these three.
					put("runner", "1_178");     // Goblin Runner: a Shadow minion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Free Peoples companion
					put("armor", "1_8");        // Dwarven Armor: a Free Peoples possession
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");     // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to; there are none
					put("site2", "13_185");     // Abandoned Mine Shaft: adds a burden when left in the regroup phase outside region 1; here it is in region 1
					put("site3", "11_234");     // Crags of Emyn Muil: wounds each companion when left in the regroup phase in region 2; here it is in region 1
					put("parapet", "63_188");
					put("site5", "18_138");     // Sirannon Ruins: River and no text
					// Never laid in these tests.
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "11_237");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	// The turn-skippers walk a Fellowship-block path, which they cannot do on a Shadows-block one. Courtyard
	// Parapet stands in each adventure deck in place of site 9, which no test reaches, and a test puts the
	// Shadow player's copy on the path as the next site the fellowship moves to.
	protected VirtualTableScenario GetFellowshipPathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("runner", "1_178");     // Goblin Runner: a Shadow minion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Free Peoples companion
					put("armor", "1_8");        // Dwarven Armor: a Free Peoples possession
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("parapet", "63_188");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CourtyardParapetStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Courtyard Parapet
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Dwelling.</b>When the fellowship moves to this site in region 2, the
		 * 	first Shadow player may discard 2 cards from hand to add a burden.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("parapet");

		assertEquals("Courtyard Parapet", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.DWELLING));
	}

	@Test
	public void AtSiteFourTheShadowPlayerMayDiscardTwoCardsFromHandToAddABurdenAndIsNotAskedAgainWhenTheFellowshipMovesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetShadowCard("aragorn");
		var armor = scn.GetShadowCard("armor");

		// The first turn takes the fellowship to site 3 in two moves, with no minion to fight on the way.
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site3"));
		// The rest of that turn, and the other player's turn.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		// Three cards in each hand.
		scn.MoveCardsToHand(runner, aragorn, armor);
		scn.MoveCardsToHand(scn.GetFreepsCard("runner"), scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("armor"));

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetBurdens());

		// Courtyard Parapet is laid as site 4, the first site of region 2.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("parapet"));

		assertEquals("Courtyard Parapet", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(4, scn.GetCurrentSiteNumber());
		// The Shadow player is asked, and nobody else.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("discard 2 cards from hand to add a burden"));
		scn.ShadowChooseYes();

		// Any 2 of his cards, whatever they are; the other player's hand is not his to discard from.
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, aragorn, armor));
		scn.ShadowChooseCards(runner, armor);

		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertInZone(Zone.DISCARD, runner, armor);
		assertInZone(Zone.HAND, aragorn);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The fellowship moves on from Courtyard Parapet in the regroup phase. The Shadow player again has
		// three cards in hand, and leaving the site asks him nothing.
		scn.SkipToMovementDecision();
		scn.MoveCardsToHand(runner, armor);
		assertEquals(3, scn.GetShadowHandCount());

		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site5"));

		assertEquals("Sirannon Ruins", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertFalse(scn.ShadowDecisionAvailable("discard 2 cards from hand to add a burden"));
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AsSiteThreeInRegionOneNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));
		scn.SkipToMovementDecision();

		// Three cards in each hand.
		scn.MoveCardsToHand(scn.GetShadowCard("runner"), scn.GetShadowCard("aragorn"), scn.GetShadowCard("armor"));
		scn.MoveCardsToHand(scn.GetFreepsCard("runner"), scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("armor"));

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());

		// Courtyard Parapet is laid as site 3, the last site of region 1.
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("parapet"));

		assertEquals("Courtyard Parapet", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertFalse(scn.ShadowDecisionAvailable("discard 2 cards from hand to add a burden"));
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSiteSixAShadowPlayerWithExactlyTwoCardsDiscardsBothToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipPathScenario();

		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetShadowCard("aragorn");
		var armor = scn.GetShadowCard("armor");

		scn.StartGame();
		scn.SkipToSite(5);
		// Courtyard Parapet becomes site 6, the last site of region 2.
		scn.MoveCardToAdventurePath(scn.GetShadowSite("parapet"));
		// Two cards in the Shadow player's hand, and three in the other.
		scn.MoveCardsToHand(aragorn, armor);
		scn.MoveCardsToBottomOfDeck(runner);
		scn.MoveCardsToHand(scn.GetFreepsCard("runner"), scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("armor"));

		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPass();

		assertEquals("Courtyard Parapet", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("discard 2 cards from hand to add a burden"));
		scn.ShadowChooseYes();

		// Two cards are all he has, so there is nothing to choose.
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertInZone(Zone.DISCARD, aragorn, armor);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AsSiteSevenInRegionThreeNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipPathScenario();

		scn.StartGame();
		scn.SkipToSite(6);
		// Courtyard Parapet becomes site 7, the first site of region 3.
		scn.MoveCardToAdventurePath(scn.GetShadowSite("parapet"));
		// Three cards in each hand.
		scn.MoveCardsToHand(scn.GetShadowCard("runner"), scn.GetShadowCard("aragorn"), scn.GetShadowCard("armor"));
		scn.MoveCardsToHand(scn.GetFreepsCard("runner"), scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("armor"));

		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPass();

		assertEquals("Courtyard Parapet", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertFalse(scn.ShadowDecisionAvailable("discard 2 cards from hand to add a burden"));
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AShadowPlayerWithOneCardInHandIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipPathScenario();

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(3);
		// Courtyard Parapet becomes site 4.
		scn.MoveCardToAdventurePath(scn.GetShadowSite("parapet"));
		// One card in the Shadow player's hand. The other player has three, which are not the Shadow player's
		// to pay with.
		scn.MoveCardsToHand(runner);
		scn.MoveCardsToBottomOfDeck(scn.GetShadowCard("aragorn"), scn.GetShadowCard("armor"));
		scn.MoveCardsToHand(scn.GetFreepsCard("runner"), scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("armor"));

		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPass();

		assertEquals("Courtyard Parapet", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertFalse(scn.ShadowDecisionAvailable("discard 2 cards from hand to add a burden"));
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, runner);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSiteFiveTheShadowPlayerDeclinesAndHisHandAndTheBurdensAreAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipPathScenario();

		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetShadowCard("aragorn");
		var armor = scn.GetShadowCard("armor");

		scn.StartGame();
		scn.SkipToSite(4);
		// Courtyard Parapet becomes site 5, the middle site of region 2.
		scn.MoveCardToAdventurePath(scn.GetShadowSite("parapet"));
		scn.MoveCardsToHand(runner, aragorn, armor);

		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPass();

		assertEquals("Courtyard Parapet", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("discard 2 cards from hand to add a burden"));
		scn.ShadowChooseNo();

		assertEquals(3, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, runner, aragorn, armor);
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnTheSecondPlayersTurnTheFirstPlayerIsTheShadowPlayerAndIsTheOneAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipPathScenario();

		// Player 1's cards: on player 2's turn he is the Shadow player.
		var runner = scn.GetFreepsCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");

		scn.StartGame();
		scn.SkipToSite(3);
		// Courtyard Parapet becomes site 4: player 2's copy, as the Shadow player lays the next site.
		var parapet = scn.GetShadowSite("parapet");
		scn.MoveCardToAdventurePath(parapet);
		// Player 1's fellowship moves there first. Player 2 has no cards in hand and is asked nothing.
		scn.MoveCardsToBottomOfDeck(scn.GetShadowCard("runner"), scn.GetShadowCard("aragorn"), scn.GetShadowCard("armor"));
		assertEquals(0, scn.GetShadowHandCount());
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Player 2's turn: his fellowship is at site 3, and the site ahead of it is his own Courtyard Parapet.
		scn.SkipToOtherPlayersTurn();
		// Three cards in each hand.
		scn.MoveCardsToHand(runner, aragorn, armor);
		scn.MoveCardsToHand(scn.GetShadowCard("runner"), scn.GetShadowCard("aragorn"), scn.GetShadowCard("armor"));

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		// Player 2's burdens: it is his turn.
		assertEquals(0, scn.GetBurdens());

		// Player 2 passes his fellowship phase and his fellowship moves.
		scn.ShadowPass();

		assertSame(parapet, scn.GetCurrentSite());
		assertEquals(4, scn.GetCurrentSiteNumber());
		// Player 1 is asked, and player 2, whose site and whose fellowship it is, is not.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("discard 2 cards from hand to add a burden"));
		scn.FreepsChooseYes();

		assertEquals(2, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, aragorn, armor));
		scn.FreepsChooseCards(aragorn, armor);

		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertInZone(Zone.DISCARD, aragorn, armor);
		assertInZone(Zone.HAND, runner);
		assertEquals(3, scn.GetShadowHandCount());
		// The burden is on the Ring-bearer of the fellowship that moved.
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
