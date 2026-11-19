package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_100_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stronghold", "7_100");
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, vitality 3; a skirmish ability only
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally, vitality 1; a maneuver ability only
					put("narsil", "9_34");      // Narsil: vitality +1 on a [gondor] Man, so that the Merchant can exert
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit; a skirmish ability used only on purpose
					put("household", "5_83");   // Household Guard: a [rohan] Man companion; valiant is its whole text

					// Fortifications of two cultures and two card types, with abilities used only on purpose
					put("first", "7_92");       // First Level: a [gondor] fortification condition
					put("arrowslits", "5_80");  // Arrow-slits: a [rohan] fortification condition
					put("fathers", "102_48");   // Stronghold of My Fathers: a [rohan] fortification POSSESSION
					// Rally Point: a [gondor] condition that is no fortification; a regroup ability that needs a knight
					put("rallypoint", "5_38");

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is an archery phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatestStrongholdStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Greatest Stronghold
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Exert a [gondor] Man and spot 3 fortifications to make the fellowship archery total +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stronghold");

		assertEquals("Greatest Stronghold", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAGondorManWithThreeFortificationsSpottedToMakeTheFellowshipArcheryTotalPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stronghold = scn.GetFreepsCard("stronghold");
		var boromir = scn.GetFreepsCard("boromir");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var pippin = scn.GetFreepsCard("pippin");
		var household = scn.GetFreepsCard("household");
		var first = scn.GetFreepsCard("first");
		var arrowslits = scn.GetFreepsCard("arrowslits");
		var fathers = scn.GetFreepsCard("fathers");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(boromir, pippin, household);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		// Exactly three fortifications: a [gondor] condition, a [rohan] condition and a [rohan] possession.
		scn.MoveCardsToSupportArea(first, arrowslits, fathers);
		scn.MoveCardsToHand(stronghold);
		assertInZone(Zone.SUPPORT, first, arrowslits, fathers);
		assertAttachedTo(narsil, merchant);
		assertInHand(stronghold);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// No archers on either side.
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		// With Narsil on him the Merchant is not exhausted: he can exert.
		assertFalse(scn.IsExhausted(merchant));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsPlayAvailable(stronghold));

		scn.FreepsPlayCard(stronghold);
		// [gondor] Men, a companion and an ally; not a [gondor] Hobbit, a [rohan] Man nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, household, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merchant);

		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertInDiscard(stronghold);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		// Spotted, not spent.
		assertInZone(Zone.SUPPORT, first, arrowslits, fathers);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void CannotBePlayedWithTwoFortificationsAndAConditionThatIsNoFortification() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stronghold = scn.GetFreepsCard("stronghold");
		var boromir = scn.GetFreepsCard("boromir");
		var first = scn.GetFreepsCard("first");
		var arrowslits = scn.GetFreepsCard("arrowslits");
		var rallypoint = scn.GetFreepsCard("rallypoint");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(first, arrowslits, rallypoint);
		scn.MoveCardsToHand(stronghold);
		assertInZone(Zone.SUPPORT, first, arrowslits, rallypoint);
		assertInHand(stronghold);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(stronghold));
	}

	@Test
	public void CannotBePlayedWhileTheOnlyGondorManIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stronghold = scn.GetFreepsCard("stronghold");
		var boromir = scn.GetFreepsCard("boromir");
		var household = scn.GetFreepsCard("household");
		var first = scn.GetFreepsCard("first");
		var arrowslits = scn.GetFreepsCard("arrowslits");
		var fathers = scn.GetFreepsCard("fathers");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(boromir, household);
		scn.MoveCardsToSupportArea(first, arrowslits, fathers);
		scn.MoveCardsToHand(stronghold);
		// Vitality 3 with 2 wounds: Boromir cannot exert. The Household Guard could, but is a [rohan] Man.
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertInZone(Zone.SUPPORT, first, arrowslits, fathers);
		assertInHand(stronghold);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.IsExhausted(boromir));
		assertFalse(scn.IsExhausted(household));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(stronghold));
	}

	@Test
	public void TheArcheryTotalBonusLastsThroughTheArcheryPhaseAndNoLonger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stronghold = scn.GetFreepsCard("stronghold");
		var boromir = scn.GetFreepsCard("boromir");
		var first = scn.GetFreepsCard("first");
		var arrowslits = scn.GetFreepsCard("arrowslits");
		var fathers = scn.GetFreepsCard("fathers");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(first, arrowslits, fathers);
		scn.MoveCardsToHand(stronghold);
		assertInZone(Zone.SUPPORT, first, arrowslits, fathers);
		assertInHand(stronghold);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetFreepsArcheryTotal());
		// Boromir is the only [gondor] Man and exerts unasked.
		scn.FreepsPlayCard(stronghold);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetFreepsArcheryTotal());

		scn.ShadowPass();
		// The last decision of the archery phase: still in force.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(2, scn.GetFreepsArcheryTotal());

		scn.FreepsPass();

		// The first decision after it, in the assignment phase: gone.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(0, scn.GetFreepsArcheryTotal());
	}
}
