package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_082_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("unhindered", "8_82");
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl, strength 9, vitality 2; not fierce; her text reads [wraith] conditions on companions
					put("nertea", "1_234");     // Úlairë Nertëa, Messenger of Dol Guldur: [wraith] Nazgûl, strength 9, vitality 2; not fierce; a play trigger only
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl, strength 12; fierce; his text needs 3 or 5 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, no Nazgûl, strength 5; its text needs 2 wounds on the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability used only on purpose
					// Goblin Patrol Troop: no text.  Four in the Free Peoples hand give him the initiative.
					put("card1", "1_177");
					put("card2", "1_177");
					put("card3", "1_177");
					put("card4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UnhinderedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Unhindered
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a Nazgûl strength +2. If that Nazgûl wins this skirmish, the Shadow has initiative until the end of the turn, regardless of the Free Peoples player's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("unhindered");

		assertEquals("Unhindered", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesANazgulStrengthPlus2AndWhenHeWinsTheShadowHasInitiativeWithAFullFreePeoplesHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unhindered = scn.GetShadowCard("unhindered");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(unhindered);
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"));

		scn.StartGame();
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(lemenya, khamul, hound);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		// Khamûl and the Hound are left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(12, scn.GetStrength(khamul));
		assertTrue(scn.ShadowPlayAvailable(unhindered));
		scn.ShadowPlayCard(unhindered);

		// A Nazgûl of each culture, the one fighting and one in no skirmish; not the [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lemenya);
		assertEquals(11, scn.GetStrength(lemenya));
		assertEquals(12, scn.GetStrength(khamul));
		assertInDiscard(unhindered);
		assertTrue(scn.FreepsHasInitiative());

		// 11 against Aragorn's 8: she wins, and the Shadow has the initiative though the Free Peoples hand is 4.
		scn.FreepsPass();
		scn.ShadowPass();
		// The win's trigger and the loser's wound come at one moment, and the Free Peoples player orders them.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertFalse(scn.FreepsHasInitiative());
		// Khamûl is fierce and unassigned: the fierce assignment phase follows.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
	}

	@Test
	public void TheInitiativeLastsThroughASecondMoveAndEndsWithTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unhindered = scn.GetShadowCard("unhindered");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(unhindered);
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"));

		scn.StartGame();
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(lemenya);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.FreepsHasInitiative());

		// Lemenya is the only Nazgûl and is chosen unasked.
		scn.ShadowPlayCard(unhindered);
		assertEquals(11, scn.GetStrength(lemenya));
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The regroup phase, the Free Peoples hand still 4.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());

		// After a second move, in the Shadow phase at site 3, it is still so.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertFalse(scn.FreepsHasInitiative());

		// The next turn is player 2's: his fellowship, with a full hand, has the initiative again.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertTrue(scn.GetShadowHandCount() >= 4);
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
	}

	@Test
	public void NothingChangesWhenThatNazgulLosesTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unhindered = scn.GetShadowCard("unhindered");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(unhindered);
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"));

		scn.StartGame();
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(lemenya);
		// Aragorn at 13, to beat Lemenya's 9 + 2 without being twice her strength.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		scn.ShadowPlayCard(unhindered);
		assertEquals(11, scn.GetStrength(lemenya));
		assertEquals(13, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(lemenya));

		scn.FreepsPass();
		scn.ShadowPass();
		// Lemenya lost, took the wound for it and lives (vitality 2); the Free Peoples player keeps the initiative.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PlayedOnANazgulWhoIsNotFightingItLastsThatSkirmishAndNeitherWinGivesTheInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unhindered = scn.GetShadowCard("unhindered");
		var lemenya = scn.GetShadowCard("lemenya");
		var nertea = scn.GetShadowCard("nertea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(unhindered);
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"));

		scn.StartGame();
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(lemenya, nertea);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{boromir, nertea});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Played on Nertëa, whose skirmish with Boromir is still to come, while Lemenya fights Aragorn.
		scn.ShadowPlayCard(unhindered);
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, nertea));
		scn.ShadowChooseCard(nertea);
		assertEquals(11, scn.GetStrength(nertea));
		assertEquals(9, scn.GetStrength(lemenya));

		// Lemenya, 9 against 8, wins: she is not that Nazgûl, and the Free Peoples player keeps the initiative.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		// Nertëa's own skirmish is not the one the event was played in: the bonus is gone, and her win, 9 against
		// Boromir's 7, gives the Shadow nothing.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(9, scn.GetStrength(nertea));
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhaseAndCanInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unhindered = scn.GetShadowCard("unhindered");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(unhindered);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(lemenya);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(unhindered));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(unhindered));
		scn.ShadowPlayCard(unhindered);
		assertEquals(11, scn.GetStrength(lemenya));
		assertInDiscard(unhindered);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoNazgulInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unhindered = scn.GetShadowCard("unhindered");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(unhindered);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(hound);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, hound);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.ShadowPlayAvailable(unhindered));
		scn.ShadowPlayCard(unhindered);
		// The [wraith] Orc is no Nazgûl: nothing is made stronger.
		assertEquals(5, scn.GetStrength(hound));
		assertInDiscard(unhindered);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
