package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_034_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("faramir", "8_34");
					put("gknight", "5_35");     // Gondorian Knight: [gondor] Man companion, knight; Knight is its whole text
					// Pippin, Trollslayer: a [gondor] Hobbit companion, a knight who is no Man; to play, spot a [gondor]
					// companion (Faramir); his skirmish ability is never used
					put("pippin", "103_31");
					// Knight of Gondor: [gondor] Man companion, knight; a fellowship ability used only on purpose
					put("kog", "5_36");
					// Ranger of Osgiliath: [gondor] Man companion who is no knight; a regroup ability used only on purpose
					put("ranger", "7_116");

					// Goblin Patrol Troop: a minion with no text, to fill the draw deck
					put("troop1", "1_177");
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("troop4", "1_177");
					put("troop5", "1_177");
					put("troop6", "1_177");
					put("troop7", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FaramirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Faramir, Defender of Osgiliath
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: <b>Knight</b>.<br>When you play Faramir (except in your starting fellowship), you may reveal the top 10 cards of your draw deck. You may play a knight revealed. Shuffle your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("faramir");

		assertEquals("Faramir", card.getBlueprint().getTitle());
		assertEquals("Defender of Osgiliath", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.KNIGHT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void PlayedFromHandHeMayRevealTheTopTenCardsAndPlayAKnightRevealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var gknight = scn.GetFreepsCard("gknight");
		var pippin = scn.GetFreepsCard("pippin");
		var kog = scn.GetFreepsCard("kog");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCardsToHand(faramir);

		scn.StartGame();
		// A knight Man third, a [gondor] Man who is no knight fourth, a knight Hobbit tenth, and a knight eleventh.
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"), gknight, ranger,
				scn.GetFreepsCard("troop3"), scn.GetFreepsCard("troop4"), scn.GetFreepsCard("troop5"),
				scn.GetFreepsCard("troop6"), scn.GetFreepsCard("troop7"), pippin, kog);
		assertEquals(gknight, scn.GetFromTopOfFreepsDeck(3));
		assertEquals(pippin, scn.GetFromTopOfFreepsDeck(10));
		assertEquals(kog, scn.GetFromTopOfFreepsDeck(11));
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(faramir);
		assertInZone(Zone.FREE_CHARACTERS, faramir);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(faramir));
		scn.FreepsAcceptOptionalTrigger();

		// The top 10 are shown to both players, and the eleventh is not.
		assertEquals(10, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(gknight, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(ranger, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(pippin, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(kog, false));
		assertEquals(10, scn.FreepsGetTotalRevealedCardCount());
		scn.DismissRevealedCards();

		// Either knight revealed, a Man and a Hobbit; not the [gondor] Man who is no knight, nor the knight below
		// the tenth card.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gknight, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ranger, kog));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertInZone(Zone.DECK, gknight, ranger, kog);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ChoosingNoKnightPlaysNoneAndTheDeckIsStillShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var gknight = scn.GetFreepsCard("gknight");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCardsToHand(faramir);

		scn.StartGame();
		scn.SetFreepsDrawDeck(gknight, pippin, scn.GetFreepsCard("troop1"));
		assertEquals(gknight, scn.GetFreepsTopOfDeck());
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(faramir);
		scn.FreepsAcceptOptionalTrigger();
		scn.DismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(gknight, pippin));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertInZone(Zone.DECK, gknight, pippin);
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningTheOfferRevealsNothingAndTheDeckIsNotShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var gknight = scn.GetFreepsCard("gknight");
		var troop1 = scn.GetFreepsCard("troop1");
		scn.MoveCardsToHand(faramir);

		scn.StartGame();
		scn.SetFreepsDrawDeck(gknight, troop1);
		assertEquals(gknight, scn.GetFreepsTopOfDeck());
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(faramir);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(faramir));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertInZone(Zone.DECK, gknight);
		assertEquals(gknight, scn.GetFreepsTopOfDeck());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenHeIsPlayedInTheStartingFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var gknight = scn.GetFreepsCard("gknight");
		var pippin = scn.GetFreepsCard("pippin");

		// Every card is in the draw deck, the knights among them; no companion there costs 1 or less.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertInZone(Zone.DECK, faramir, gknight, pippin);
		int shuffles = scn.GetFreepsDeckShuffleCount();
		scn.FreepsChooseCard(faramir);

		// His 3 leaves 1 of the 4, which no companion left fits, so the choice is over; nothing is offered him,
		// and the other player is choosing now.
		assertInZone(Zone.FREE_CHARACTERS, faramir);
		assertInZone(Zone.DECK, gknight, pippin);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
	}

	@Test
	public void AnotherKnightPlayedWhileFaramirIsInPlayBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var gknight = scn.GetFreepsCard("gknight");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(faramir);
		scn.MoveCardsToHand(gknight);

		scn.StartGame();
		scn.SetFreepsDrawDeck(pippin, scn.GetFreepsCard("troop1"));
		assertEquals(pippin, scn.GetFreepsTopOfDeck());
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(gknight);
		assertInZone(Zone.FREE_CHARACTERS, gknight);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertInZone(Zone.DECK, pippin);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
