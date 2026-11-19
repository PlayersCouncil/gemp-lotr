package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_006_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. Turn 1 begins at Bag
	// End (site 1); its maneuver phase is at The Troll Hoard (site 2), whose own offer at the start of a fellowship
	// phase is answered where a test reaches one there.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ponies", "33_6");

					// The three [dwarven] followers it names, and one it does not; each plays to the support area
					put("bifur", "30_5");           // Bifur, Inarticulate
					put("bofur", "30_6");           // Bofur, Encouraging Fellow
					put("bombur", "30_7");          // Bombur, Chief Cook
					put("dori", "30_8");            // Dori, Really a decent fellow

					// Cards of each kind for the hand
					put("balin", "30_2");           // Balin, Brother of Dwalin: a companion
					put("axe", "31_1");             // Dwarven Axe: a possession
					put("fury", "30_3");            // Battle Fury: an event
					put("troop", "1_177");          // Goblin Patrol Troop: a Shadow card (a minion with no text)
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void DwarvenPoniesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Dwarven Ponies
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: Each time you play Bifur, Bofur or Bombur, you may draw 2 cards.
		 * 	<b>Fellowship:</b> Add (2) to place a card from hand beneath your draw deck (limit once per turn).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ponies");

		assertEquals("Dwarven Ponies", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachTimeBifurOrBofurIsPlayedTwoCardsMayBeDrawnUntilTheRuleOfFourIsSpent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ponies = scn.GetFreepsCard("ponies");
		var bifur = scn.GetFreepsCard("bifur");
		var bofur = scn.GetFreepsCard("bofur");
		var bombur = scn.GetFreepsCard("bombur");
		scn.MoveCardsToSupportArea(ponies);
		scn.MoveCardsToHand(bifur, bofur, bombur);

		scn.StartGame();

		assertEquals(3, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(bifur);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ponies));
		scn.FreepsAcceptOptionalTrigger();
		// Bofur and Bombur, and two cards drawn.
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());

		scn.FreepsPlayCard(bofur);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ponies));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(deck - 4, scn.GetFreepsDeckCount());

		// Four cards drawn this fellowship phase: the Rule of 4 leaves the draw nothing to do, and it is not offered.
		scn.FreepsPlayCard(bombur);
		assertInZone(Zone.SUPPORT, bifur, bofur, bombur);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ponies));
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deck - 4, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayingBomburOffersTheDrawAndItMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ponies = scn.GetFreepsCard("ponies");
		var bombur = scn.GetFreepsCard("bombur");
		scn.MoveCardsToSupportArea(ponies);
		scn.MoveCardsToHand(bombur);

		scn.StartGame();

		assertEquals(1, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(bombur);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ponies));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, bombur);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayingThePoniesThemselvesOrAnotherDwarvenFollowerOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ponies = scn.GetFreepsCard("ponies");
		var dori = scn.GetFreepsCard("dori");
		scn.MoveCardsToHand(ponies, dori);

		scn.StartGame();

		assertEquals(2, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		// The Ponies' own play is a [dwarven] card played that is none of the three.
		scn.FreepsPlayCard(ponies);
		assertInZone(Zone.SUPPORT, ponies);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ponies));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(dori);

		assertInZone(Zone.SUPPORT, dori);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ponies));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AddsTwoToPlaceACardOfAnyKindFromHandBeneathTheDrawDeckOnceATurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ponies = scn.GetFreepsCard("ponies");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var fury = scn.GetFreepsCard("fury");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCardsToSupportArea(ponies);
		scn.MoveCardsToHand(balin, axe, fury, troop);

		scn.StartGame();

		assertInZone(Zone.HAND, balin, axe, fury, troop);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(ponies));

		scn.FreepsUseCardAction(ponies);
		assertEquals(2, scn.GetTwilight());
		// A companion, a possession, an event, and a Shadow card.
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, axe, fury, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(axe);

		assertEquals(axe, scn.GetFreepsBottomOfDeck());
		assertInZone(Zone.HAND, balin, fury, troop);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetTwilight());
		// Once a turn: cards in hand and the (2) can always be added.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ponies));

		// Player 1's next turn (turn 3), at The Troll Hoard.
		scn.SkipToSite(2);
		// The Troll Hoard makes no offer of its own: the Dwarven Axe is the one hand weapon, and no [dwarven]
		// character is in play to bear it (F-0800).
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(scn.GetCurrentSite()));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.GetFreepsHandCount() > 0);
		assertTrue(scn.FreepsActionAvailable(ponies));
	}

	@Test
	public void IsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ponies = scn.GetFreepsCard("ponies");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCardsToSupportArea(ponies);
		scn.MoveCardsToHand(axe);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		assertInZone(Zone.HAND, axe);

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ponies));
		assertInZone(Zone.HAND, axe);
	}

	@Test
	public void WithAnEmptyHandTheTwoAreAddedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ponies = scn.GetFreepsCard("ponies");
		scn.MoveCardsToSupportArea(ponies);

		scn.StartGame();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetTwilight());
		var bottom = scn.GetFreepsBottomOfDeck();
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsActionAvailable(ponies));

		scn.FreepsUseCardAction(ponies);

		assertEquals(2, scn.GetTwilight());
		assertEquals(bottom, scn.GetFreepsBottomOfDeck());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
