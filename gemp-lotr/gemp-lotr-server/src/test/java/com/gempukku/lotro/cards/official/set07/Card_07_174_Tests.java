package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_174_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("called", "7_174");

					put("lemenya", "1_232");    // Ulaire Lemenya: a [wraith] Nazgul; her text reaches only bearers of [wraith] conditions
					// Ulaire Attea, Khamul of the East: a Nazgul of [raider]; his text needs 3 burdens
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] minion who is no Nazgul (an Orc); its text needs 2 wounds on the Ring-bearer
					put("hound", "7_192");

					// Companions beside Frodo, so that the fellowship can hold 5 threats
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] Dwarf; a skirmish ability only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: his own strength only (no archer, so no archery wound to place)
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability only
					// Elrond, Lord of Rivendell: an ally whose home is site 3, where these tests assign; he heals
					// allies of site 3 at the start of the turn and has a fellowship ability, neither used here
					put("elrond", "1_40");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CalledStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Called
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Assignment:</b> Remove 5 threats to assign a Nazgûl to a companion (except the Ring-bearer). The Free Peoples player may discard that companion.
		*/

		var scn = GetScenario();

		var called = scn.GetFreepsCard("called");

		assertEquals("Called", called.getBlueprint().getTitle());
		assertNull(called.getBlueprint().getSubtitle());
		assertFalse(called.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, called.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, called.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, called.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(called, Keyword.SUPPORT_AREA));
		assertEquals(1, called.getBlueprint().getTwilightCost());
	}

	@Test
	public void AssignmentAbilityRemovesFiveThreatsToAssignTheChosenNazgulToTheChosenCompanionWhoMayBeKept() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var called = scn.GetShadowCard("called");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var boromir = scn.GetFreepsCard("boromir");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCardsToSupportArea(called);
		scn.MoveCompanionsToTable(aragorn, gimli, gandalf, boromir);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(lemenya, khamul, hound);
		scn.AddThreats(5);
		assertEquals(5, scn.GetThreats());

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// At site 3, Elrond's home: an ally who could be assigned there, but no companion.
		assertTrue(scn.FreepsCanAssign(elrond));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(called));

		scn.ShadowUseCardAction(called);
		assertEquals(0, scn.GetThreats());

		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, elrond, lemenya));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lemenya);

		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard"));
		scn.FreepsChooseNo();

		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.IsCharAssignedAgainst(gimli, lemenya));
		assertFalse(scn.IsCharAssigned(khamul));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayDiscardThatCompanionAndTheNazgulIsLeftUnassigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var called = scn.GetShadowCard("called");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(called);
		scn.MoveCompanionsToTable(aragorn, gimli, scn.GetFreepsCard("gandalf"), scn.GetFreepsCard("boromir"));

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul);
		scn.AddThreats(5);
		assertEquals(5, scn.GetThreats());

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		scn.ShadowUseCardAction(called);
		assertEquals(0, scn.GetThreats());
		scn.ShadowChooseCard(aragorn);
		scn.ShadowChooseCard(khamul);

		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard"));
		scn.FreepsChooseYes();

		// Aragorn goes to the discard pile (not killed), and Khamul is left with nobody to skirmish.
		assertInDiscard(aragorn);
		assertFalse(scn.IsCharAssigned(khamul));
		// The other companion is untouched.
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertFalse(scn.IsCharAssigned(gimli));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignmentAbilityIsNotOfferedWithFourThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var called = scn.GetShadowCard("called");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToSupportArea(called);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("gandalf"), scn.GetFreepsCard("boromir"));

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.AddThreats(4);
		assertEquals(4, scn.GetThreats());

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(called));

		// A fifth threat, and the ability is offered.
		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(5, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(called));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void WithNoNazgulTheThreatsArePaidForNothingAndNoDiscardIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var called = scn.GetShadowCard("called");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(called);
		scn.MoveCompanionsToTable(aragorn, scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("gandalf"), scn.GetFreepsCard("boromir"));

		scn.StartGame();
		// A [wraith] minion who is no Nazgul: nothing for the ability to assign.
		scn.MoveMinionsToTable(hound);
		scn.AddThreats(5);
		assertEquals(5, scn.GetThreats());

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(called));

		scn.ShadowUseCardAction(called);

		// The cost is paid whether or not anything can be assigned, and no companion is assigned, so none can be discarded.
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.IsCharAssigned(hound));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.FreepsDecisionAvailable("Would you like to discard"));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoCompanionButTheRingBearerTheThreatsArePaidForNothingAndNoDiscardIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var called = scn.GetShadowCard("called");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToSupportArea(called);
		scn.MoveCompanionsToTable(aragorn, gimli, gandalf, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.AddThreats(5);
		// The four companions then leave play, as they can in a game: the threats stay (CR 4.2 "threats").
		scn.MoveCardsToDiscard(aragorn, gimli, gandalf, boromir);
		assertEquals(5, scn.GetThreats());
		assertInDiscard(aragorn, gimli, gandalf, boromir);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(called));

		scn.ShadowUseCardAction(called);

		// The Ring-bearer is the only companion left and is excepted: the cost is paid, nobody is assigned, nobody can be discarded.
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.IsCharAssigned(lemenya));
		assertFalse(scn.IsCharAssigned(scn.GetRingBearer()));
		assertFalse(scn.FreepsDecisionAvailable("Would you like to discard"));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
