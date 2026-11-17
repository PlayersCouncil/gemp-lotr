package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_117_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("counter", "7_117");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man knight, vitality 3; Knight is its whole text
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] knight who is a Hobbit, no Man; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, no knight, vitality 4; a maneuver ability used only on purpose
					put("ranger", "7_116");     // Ranger of Osgiliath: [gondor] Man, no knight, vitality 3; a regroup ability used only on purpose
					put("guard", "5_83");       // Household Guard: a [rohan] Man, valiant, no other text
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally, vitality 1; a maneuver ability used only on purpose
					put("narsil", "9_34");      // Narsil: vitality +1, so the Merchant can exert; a response to its bearer's wound only

					// Cards for the Free Peoples hand, played by nobody: a companion, a possession, an event and a
					// Shadow minion (his own copy).
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] companion
					put("bow", "1_41");         // Elven Bow: [elven] possession
					put("defiance", "1_37");    // Defiance: [elven] skirmish event
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RecklessCounterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Reckless Counter
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Spot 2 knights or exert 2 [gondor] Men to liberate a site or discard any number of cards from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("counter");

		assertEquals("Reckless Counter", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertingTwoGondorMenLiberatesTheHighestSiteAnOpponentControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var counter = scn.GetFreepsCard("counter");
		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(knight, pippin, aragorn, guard);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		// The Shadow player holds sites 1 and 2; the Free Peoples player holds site 3, the highest of them.
		scn.ShadowTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		scn.FreepsTakeControlOfSite(site3);
		scn.MoveCardsToHand(counter, bow);
		assertInZone(Zone.HAND, counter, bow);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.FreepsControls(site3));
		scn.SkipToPhase(Phase.REGROUP);

		assertAttachedTo(narsil, merchant);
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(counter));

		scn.FreepsPlayCard(counter);
		// Both halves of the cost can be paid (two knights, and [gondor] Men to exert), so the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("knights"));
		assertTrue(scn.FreepsChoiceAvailable("Exert 2"));
		scn.FreepsChooseOption("Exert 2");

		// Who exerts: [gondor] Men, companions and the ally; not Pippin ([gondor], no Man), the Household Guard
		// (a Man, [rohan]) or Frodo.  Exactly two.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, aragorn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, merchant);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(guard));

		// Both alternatives can be carried out, so the player is asked which.
		assertTrue(scn.FreepsChoiceAvailable("Liberate a site"));
		assertTrue(scn.FreepsChoiceAvailable("Discard any number of cards from hand"));
		scn.FreepsChoose("Liberate a site");

		// CR 4.2 "liberate": the highest site an opponent controls, passing over the player's own.
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertFalse(scn.ShadowControls(site2));
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site3));
		assertInZone(Zone.HAND, bow);
		assertInZone(Zone.DISCARD, counter);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void SpottingTwoKnightsDiscardsTheCardsChosenFromHandWithNobodyExerting() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var counter = scn.GetFreepsCard("counter");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var boromir = scn.GetFreepsCard("boromir");
		var bow = scn.GetFreepsCard("bow");
		var defiance = scn.GetFreepsCard("defiance");
		var handTroop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(knight, pippin, aragorn, guard);

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		// Both [gondor] Men exhausted: the exertion cannot be paid, and the two knights (the Knight, a Man, and
		// Pippin, a Hobbit) are spotted.
		scn.AddWoundsToChar(knight, 2);
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveCardsToHand(counter, boromir, bow, defiance, handTroop);
		assertInZone(Zone.HAND, counter, boromir, bow, defiance, handTroop);
		assertTrue(scn.ShadowControls(site1));
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.FreepsPlayAvailable(counter));
		// The turns played have left cards drawn when reconciling in the hand as well.
		int handBefore = scn.GetFreepsHandCount();

		scn.FreepsPlayCard(counter);
		assertTrue(scn.FreepsChoiceAvailable("Liberate a site"));
		assertTrue(scn.FreepsChoiceAvailable("Discard any number of cards from hand"));
		scn.FreepsChoose("Discard any number of cards from hand");

		// Any number of the cards in hand, of any type or side, from none to all of them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, bow, defiance, handTroop));
		assertEquals(0, scn.FreepsGetChoiceMin());
		// Every card left in hand, the event having left it.
		assertEquals(handBefore - 1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(bow, handTroop);
		assertEquals(handBefore - 3, scn.GetFreepsHandCount());

		assertInZone(Zone.DISCARD, bow, handTroop, counter);
		assertInZone(Zone.HAND, boromir, defiance);
		assertTrue(scn.ShadowControls(site1));
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteAnOpponentControlsTheDiscardIsWhatIsDone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var counter = scn.GetFreepsCard("counter");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var bow = scn.GetFreepsCard("bow");
		var defiance = scn.GetFreepsCard("defiance");
		scn.MoveCompanionsToTable(knight, pippin);
		scn.MoveCardsToHand(counter, bow, defiance);
		assertInZone(Zone.HAND, counter, bow, defiance);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// The first turn: nobody controls a site, so there is nothing to liberate.
		assertFalse(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.FreepsPlayAvailable(counter));

		// One [gondor] Man only: the two knights are spotted, unasked.  The liberation cannot be carried out, so
		// the discard is chosen for him, and the cards to discard are asked at once.
		scn.FreepsPlayCard(counter);
		assertFalse(scn.FreepsChoiceAvailable("Liberate a site"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(bow, defiance));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(defiance);

		assertInZone(Zone.DISCARD, defiance, counter);
		assertInZone(Zone.HAND, bow);
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOneKnightAndOneGondorManAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var counter = scn.GetFreepsCard("counter");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ranger = scn.GetFreepsCard("ranger");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(pippin, aragorn, ranger, guard);
		scn.MoveCardsToHand(counter);
		assertInZone(Zone.HAND, counter);

		scn.StartGame();
		// Vitality 3 with 2 wounds: the Ranger, a [gondor] Man who is no knight, cannot exert.
		scn.AddWoundsToChar(ranger, 2);
		scn.SkipToPhase(Phase.REGROUP);

		// Pippin is the one knight; Aragorn the one [gondor] Man who can exert; the Household Guard can exert, but
		// is a [rohan] Man.
		assertEquals(2, scn.GetWoundsOn(ranger));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(counter));
	}
}
