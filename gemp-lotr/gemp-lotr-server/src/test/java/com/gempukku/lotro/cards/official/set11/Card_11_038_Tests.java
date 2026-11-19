package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_038_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("awakened", "11_38");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion; a strength bonus by the races in the fellowship
					// Saruman, The White: a [gandalf] Wizard who is an ally (a Hobbit-set card, laid under Q-0312 as the
					// one card that tells "a [gandalf] Wizard" from a [gandalf] Wizard companion).  His abilities are never used.
					put("sarumanally", "32_24");
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Wizard; strength +2 only beside 3 Ents
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability never used

					put("marksmanship", "11_17"); // Elven Marksmanship: [elven] support-area condition; an archery ability never used
					put("saga", "1_114");       // The Saga of Elendil: [gondor] condition on a [gondor] companion; a skirmish ability never used
					put("sword", "1_299");      // Hobbit Sword: [shire] possession, hand weapon on a Hobbit; no other text
					put("wreathed", "1_238");   // Wreathed in Shadow: [wraith] support-area condition; an archery ability never used
					put("weary", "4_212");      // Weary: [isengard] condition on a companion; answers an assignment to a tracker only
					put("ithilstone", "9_47");  // Ithil Stone: [sauron] support-area artifact; a Shadow-phase draw and ability never reached
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NewawakenedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: New-awakened
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 6
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spell. Toil 2. (For each [gandalf] character you exert when playing this, its twilight cost is -2.)
		 * Spot a [gandalf] Wizard to discard 2 conditions from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("awakened");

		assertEquals("New-awakened", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.TOIL));
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(6, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsTwoConditionsOfEitherSideBorneOrNotAndExertingGandalfForToilTakesTwoOffItsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var awakened = scn.GetFreepsCard("awakened");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var marksmanship = scn.GetFreepsCard("marksmanship");
		var saga = scn.GetFreepsCard("saga");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(marksmanship);
		scn.AttachCardsTo(aragorn, saga);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToHand(awakened);

		// Shadow cards left in play from an earlier turn.
		var wreathed = scn.GetShadowCard("wreathed");
		var weary = scn.GetShadowCard("weary");
		var ithilstone = scn.GetShadowCard("ithilstone");
		scn.MoveCardsToSupportArea(wreathed, ithilstone);
		scn.AttachCardsTo(gandalf, weary);

		assertInZone(Zone.HAND, awakened);
		assertInZone(Zone.SUPPORT, marksmanship, wreathed, ithilstone);
		assertInZone(Zone.ATTACHED, saga, sword, weary);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(awakened));
		scn.FreepsPlayCard(awakened);

		// Toil: only a [gandalf] character may be exerted, and none need be.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(gandalf);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		// 6 less 2 for the one exertion.
		assertEquals(4, scn.GetTwilight());

		// Conditions of either side, in a support area or borne; no possession or artifact.
		assertTrue(scn.FreepsHasCardChoiceAvailable(marksmanship, saga, wreathed, weary));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword, ithilstone));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(saga, wreathed);

		assertInDiscard(saga, wreathed);
		assertInZone(Zone.SUPPORT, marksmanship, ithilstone);
		assertInZone(Zone.ATTACHED, sword, weary);
		assertInDiscard(awakened);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertingTwoGandalfCharactersForToilTakesFourOffItsCostAndWithNoConditionNothingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var awakened = scn.GetFreepsCard("awakened");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var frodo = scn.GetRingBearer();
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToHand(awakened);
		var ithilstone = scn.GetShadowCard("ithilstone");
		scn.MoveCardsToSupportArea(ithilstone);

		assertInZone(Zone.HAND, awakened);
		assertInZone(Zone.ATTACHED, sword);
		assertInZone(Zone.SUPPORT, ithilstone);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(awakened);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, avenger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(gandalf, avenger);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(avenger));
		// 6 less 2 for each of two exertions.
		assertEquals(2, scn.GetTwilight());

		// With no condition in play the event is played for nothing.
		assertInZone(Zone.ATTACHED, sword);
		assertInZone(Zone.SUPPORT, ithilstone);
		assertInDiscard(awakened);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAGandalfWizard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var awakened = scn.GetFreepsCard("awakened");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(awakened);
		var wreathed = scn.GetShadowCard("wreathed");
		var weary = scn.GetShadowCard("weary");
		scn.MoveCardsToSupportArea(wreathed);
		scn.AttachCardsTo(avenger, weary);
		assertInZone(Zone.HAND, awakened);

		scn.StartGame();

		// A [gandalf] companion who is no Wizard is not enough.
		assertFalse(scn.FreepsPlayAvailable(awakened));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGandalfWizardWhoIsAnAllyMayBeSpottedAndALoneConditionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var awakened = scn.GetFreepsCard("awakened");
		var sarumanally = scn.GetFreepsCard("sarumanally");
		scn.MoveAlliesToTable(sarumanally);
		scn.MoveCardsToHand(awakened);
		var wreathed = scn.GetShadowCard("wreathed");
		scn.MoveCardsToSupportArea(wreathed);
		assertInZone(Zone.HAND, awakened);
		assertInZone(Zone.SUPPORT, wreathed);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(awakened));
		scn.FreepsPlayCard(awakened);
		// He is a [gandalf] character too, and may be exerted for toil; he is not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sarumanally));
		scn.FreepsDeclineChoosing();

		// One condition in play: as much as can be done is done.
		assertInDiscard(wreathed);
		assertInDiscard(awakened);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
