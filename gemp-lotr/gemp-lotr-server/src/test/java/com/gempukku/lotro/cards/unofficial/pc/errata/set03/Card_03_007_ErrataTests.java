package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_007_ErrataTests
{
	// The One Ring, Answer To All Riddles has no response of its own, so the Ring-bearer's wounds are answered by
	// Arwen alone (The Ruling Ring would offer its own response to every wound in a skirmish).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "53_7");
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound Hobbit companion, not the Ring-bearer
					put("marksman1", "1_176");  // Goblin Marksman: archer minion, nothing else
					put("marksman2", "1_176");
					put("marksman3", "1_176");
					put("savage", "1_151");     // Uruk Savage: strength 5, damage +1, nothing else
					put("troop1", "1_177");     // Goblin Patrol Troop: no text; cards for the hand
					put("troop2", "1_177");
					put("troop3", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.ATARRing
		);
	}

	// Galadriel, Bearer of Wisdom as the Ring-bearer: a Ring-bearer who is not Frodo, with no Frodo beside her.
	protected VirtualTableScenario GetGaladrielScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "53_7");
					put("marksman1", "1_176");  // Goblin Marksman: archer minion, nothing else
					put("marksman2", "1_176");
					put("troop1", "1_177");     // Goblin Patrol Troop: no text; cards for the hand
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.ATARRing
		);
	}

	@Test
	public void ArwenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Arwen, Elven Rider
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: <b>Ranger</b>.
		 * <b>Response:</b> If the Ring-bearer is about to take a wound, discard X cards from hand to prevent that
		 * wound, where X is the current region number.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arwen");

		assertEquals("Arwen", card.getBlueprint().getTitle());
		assertEquals("Elven Rider", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void IsPlayedFromHandForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCardsToHand(arwen);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(arwen));

		scn.FreepsPlayCard(arwen);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, arwen);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSite3OneCardFromHandPreventsAWoundToTheRingBearerAndNotToOtherCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var arwen = scn.GetFreepsCard("arwen");
		var sam = scn.GetFreepsCard("sam");
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		var marksman3 = scn.GetShadowCard("marksman3");
		var troop1 = scn.GetFreepsCard("troop1");
		var troop2 = scn.GetFreepsCard("troop2");
		var troop3 = scn.GetFreepsCard("troop3");
		scn.MoveCompanionsToTable(arwen, sam);

		scn.StartGame();
		scn.SkipToSite(2);
		// The turns on the way drew the rest of the deck: put back all but the three cards for the hand.
		scn.MoveCardsToTopOfDeck(scn.GetFreepsCard("marksman1"), scn.GetFreepsCard("marksman2"),
				scn.GetFreepsCard("marksman3"), scn.GetFreepsCard("savage"));
		scn.MoveCardsToHand(troop1, troop2, troop3);
		scn.MoveMinionsToTable(marksman1, marksman2, marksman3);
		// The fellowship moves to site 3: region 1.
		scn.SkipToArcheryWounds();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetShadowArcheryTotal());

		// Sam, Ring-bound and not the Ring-bearer, and Arwen herself take theirs unanswered.
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.FreepsChooseCard(sam);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(arwen));
		assertEquals(1, scn.GetWoundsOn(sam));

		assertEquals(0, scn.GetWoundsOn(arwen));
		scn.FreepsChooseCard(arwen);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(arwen));
		assertEquals(1, scn.GetWoundsOn(arwen));

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.FreepsChooseCard(frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(arwen));
		scn.FreepsChooseOptionalTrigger(arwen);

		// X is 1: one card of the three.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, troop2, troop3));
		scn.FreepsChooseCard(troop2);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertInDiscard(troop2);
		assertInHand(troop1, troop3);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AtSite4TwoCardsFromHandPreventAWoundAndWithOneCardLeftItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var arwen = scn.GetFreepsCard("arwen");
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		var troop1 = scn.GetFreepsCard("troop1");
		var troop2 = scn.GetFreepsCard("troop2");
		var troop3 = scn.GetFreepsCard("troop3");
		scn.MoveCompanionsToTable(arwen);

		scn.StartGame();
		scn.SkipToSite(3);
		// The turns on the way drew the rest of the deck: put back all but the three cards for the hand.
		scn.MoveCardsToTopOfDeck(scn.GetFreepsCard("sam"), scn.GetFreepsCard("marksman1"),
				scn.GetFreepsCard("marksman2"), scn.GetFreepsCard("marksman3"), scn.GetFreepsCard("savage"));
		scn.MoveCardsToHand(troop1, troop2, troop3);
		scn.MoveMinionsToTable(marksman1, marksman2);
		// The fellowship moves to site 4: region 2.
		scn.SkipToArcheryWounds();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetShadowArcheryTotal());

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.FreepsChooseCard(frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(arwen));
		scn.FreepsChooseOptionalTrigger(arwen);

		// X is 2: two cards of the three.
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, troop2, troop3));
		scn.FreepsChooseCards(troop1, troop3);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertInDiscard(troop1, troop3);
		assertInHand(troop2);

		// One card in hand is one short of X: the second wound is taken.
		scn.FreepsChooseCard(frodo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(arwen));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void EachResponsePreventsOneWoundOfADamagePlusOneLoss() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var arwen = scn.GetFreepsCard("arwen");
		var savage = scn.GetShadowCard("savage");
		var troop1 = scn.GetFreepsCard("troop1");
		var troop2 = scn.GetFreepsCard("troop2");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(troop1, troop2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.FreepsResolveSkirmish(frodo);
		// Uruk Savage, strength 5, beats Frodo, strength 3, without overwhelming him: two wounds with damage +1.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetFreepsHandCount());
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(arwen));
		scn.FreepsChooseOptionalTrigger(arwen);
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop1);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetFreepsHandCount());

		// The other wound is answered again, and declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(arwen));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetFreepsHandCount());
		assertInHand(troop2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ARingBearerWhoIsNotFrodoIsProtectedAndTheCompanionBesideHerIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielScenario();

		var galadriel = scn.GetRingBearer();
		var arwen = scn.GetFreepsCard("arwen");
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		var troop1 = scn.GetFreepsCard("troop1");
		var troop2 = scn.GetFreepsCard("troop2");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(troop1, troop2);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman1, marksman2);
		scn.SkipToArcheryWounds();
		assertEquals(2, scn.GetShadowArcheryTotal());

		assertEquals(0, scn.GetWoundsOn(arwen));
		scn.FreepsChooseCard(arwen);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(arwen));
		assertEquals(1, scn.GetWoundsOn(arwen));

		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(2, scn.GetFreepsHandCount());
		scn.FreepsChooseCard(galadriel);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(arwen));
		scn.FreepsChooseOptionalTrigger(arwen);
		scn.FreepsChooseCard(troop1);

		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(1, scn.GetFreepsHandCount());
		assertInDiscard(troop1);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
