package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_087_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eomer", "8_87");
					put("rshield", "5_91");     // Rohirrim Shield: [rohan] possession, shield, cost 1, on a [rohan] Man; a regroup ability only
					// Firefoot, Mearas of The Mark: [rohan] possession, mount, cost 2, on a [rohan] Man; its play trigger
					// reinforces a [rohan] token and is withheld with none in play; its maneuver trigger needs 2 tokens
					put("firefoot", "13_126");
					put("ridersmount", "4_287"); // Rider's Mount: [rohan] possession, mount, on a [rohan] Man; a start-of-skirmish exertion only
					// The Red Arrow: a [rohan] artifact (support area, cost 0), the item that is no possession; an
					// assignment ability only
					put("arrow", "9_46");
					put("gshield", "1_107");    // Great Shield: a [gondor] possession, shield, on a Man; the minion archery total -1 only
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, cost 2; valiant is its whole text

					// Goblin Patrol Troop: a minion with no text, to fill the draw deck
					put("troop1", "1_177");
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("troop4", "1_177");
					put("troop5", "1_177");
					put("troop6", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EomerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Éomer, Keeper of Oaths
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: <b>Valiant</b>.<br>When you play Éomer (except in your starting fellowship), you may reveal the top 10 cards of your draw deck. You may play each [rohan] possession revealed. Shuffle your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eomer");

		assertEquals("Éomer", card.getBlueprint().getTitle());
		assertEquals("Keeper of Oaths", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void PlayedFromHandHeMayRevealTheTopTenCardsAndPlayEachRohanPossessionRevealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var rshield = scn.GetFreepsCard("rshield");
		var firefoot = scn.GetFreepsCard("firefoot");
		var ridersmount = scn.GetFreepsCard("ridersmount");
		var arrow = scn.GetFreepsCard("arrow");
		var gshield = scn.GetFreepsCard("gshield");
		scn.MoveCardsToHand(eomer);

		scn.StartGame();
		// A [rohan] shield third, a [gondor] shield fourth, a [rohan] artifact fifth, a [rohan] mount tenth, and a
		// [rohan] mount eleventh.
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"), rshield, gshield, arrow,
				scn.GetFreepsCard("troop3"), scn.GetFreepsCard("troop4"), scn.GetFreepsCard("troop5"),
				scn.GetFreepsCard("troop6"), firefoot, ridersmount);
		assertInZone(Zone.DECK, rshield, gshield, arrow, firefoot, ridersmount);
		assertEquals(rshield, scn.GetFromTopOfFreepsDeck(3));
		assertEquals(firefoot, scn.GetFromTopOfFreepsDeck(10));
		assertEquals(ridersmount, scn.GetFromTopOfFreepsDeck(11));
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(eomer);
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eomer));
		scn.FreepsAcceptOptionalTrigger();

		// The top 10 are shown to both players, and the eleventh is not.
		assertEquals(10, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(rshield, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(gshield, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(arrow, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(firefoot, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(ridersmount, false));
		assertEquals(10, scn.FreepsGetTotalRevealedCardCount());
		scn.DismissRevealedCards();

		// Each [rohan] possession revealed, a shield and a mount; not the [rohan] artifact, the [gondor] possession
		// he could bear, nor the [rohan] possession below the tenth card.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rshield, firefoot));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arrow, gshield, ridersmount));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(rshield);
		assertAttachedTo(rshield, eomer);

		// "Each": the other is offered in its turn, and goes on him too.
		assertTrue(scn.FreepsHasCardChoiceAvailable(firefoot));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arrow, gshield, ridersmount));
		scn.FreepsChooseCard(firefoot);

		assertAttachedTo(rshield, eomer);
		assertAttachedTo(firefoot, eomer);
		assertInZone(Zone.DECK, gshield, arrow, ridersmount);
		// One shuffle as each possession leaves the deck for its bearer, and the text's own after.
		assertEquals(shuffles + 3, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ChoosingNoPossessionPlaysNoneAndTheDeckIsStillShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var rshield = scn.GetFreepsCard("rshield");
		var firefoot = scn.GetFreepsCard("firefoot");
		var gshield = scn.GetFreepsCard("gshield");
		var arrow = scn.GetFreepsCard("arrow");
		scn.MoveCardsToHand(eomer);

		scn.StartGame();
		// A [gondor] possession and a [rohan] artifact are revealed too, and are not counted among the [rohan] possessions.
		scn.SetFreepsDrawDeck(rshield, firefoot, gshield, arrow, scn.GetFreepsCard("troop1"));
		assertInZone(Zone.DECK, rshield, firefoot, gshield, arrow);
		assertEquals(rshield, scn.GetFreepsTopOfDeck());
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(eomer);
		scn.FreepsAcceptOptionalTrigger();
		scn.DismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(rshield, firefoot));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();
		// Each possession is a "may" of its own: with one passed over, the choice is put again, once for each.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rshield, firefoot));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertInZone(Zone.DECK, rshield, firefoot, gshield, arrow);
		assertEquals(5, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningTheOfferRevealsNothingAndTheDeckIsNotShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var rshield = scn.GetFreepsCard("rshield");
		var troop1 = scn.GetFreepsCard("troop1");
		scn.MoveCardsToHand(eomer);

		scn.StartGame();
		scn.SetFreepsDrawDeck(rshield, troop1);
		assertInZone(Zone.DECK, rshield);
		assertEquals(rshield, scn.GetFreepsTopOfDeck());
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(eomer);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eomer));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertInZone(Zone.DECK, rshield);
		assertEquals(rshield, scn.GetFreepsTopOfDeck());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenHeIsPlayedInTheStartingFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var rshield = scn.GetFreepsCard("rshield");
		var firefoot = scn.GetFreepsCard("firefoot");

		// Every card is in the draw deck, the [rohan] possessions among them; no companion there costs 1 or less.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertInZone(Zone.DECK, eomer, rshield, firefoot);
		int shuffles = scn.GetFreepsDeckShuffleCount();
		scn.FreepsChooseCard(eomer);

		// His 3 leaves 1 of the 4, which no companion left fits, so the choice is over; nothing is offered him,
		// and the other player is choosing now.
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		assertInZone(Zone.DECK, rshield, firefoot);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
	}

	@Test
	public void AnotherRohanCompanionPlayedWhileEomerIsInPlayBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var rshield = scn.GetFreepsCard("rshield");
		scn.MoveCompanionsToTable(eomer);
		scn.MoveCardsToHand(guard);

		scn.StartGame();
		scn.SetFreepsDrawDeck(rshield, scn.GetFreepsCard("troop1"));
		assertInZone(Zone.DECK, rshield);
		assertEquals(rshield, scn.GetFreepsTopOfDeck());
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(guard);
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertInZone(Zone.DECK, rshield);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
