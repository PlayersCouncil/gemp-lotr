package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_030_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pass", "2_30");
					// Gandalf, Friend of the Shirefolk: strength +1 for each race of companion; no ability
					put("gandalf", "1_72");
					// Gandalf's Staff: a staff that is an artifact; "Bearer must be Gandalf. The twilight cost of each
					// [gandalf] spell is -1"; its skirmish ability is used only on purpose
					put("gstaff", "2_22");
					// Wizard Staff: a staff that is a possession; "Bearer must be a Wizard"; a skirmish ability used
					// only on purpose
					put("wstaff", "2_29");
					// Glamdring: a hand weapon, no staff; "Bearer must be Gandalf"; its abilities are used only on purpose
					put("glamdring", "1_75");
					// Radagast, The Brown: a [gandalf] Wizard companion who is no Gandalf, to bear a staff; his texts
					// touch only the move limit and regroup moves
					put("radagast", "9_26");

					// Úlairë Cantëa: a fierce [wraith] Nazgûl; his skirmish ability is used only on purpose
					put("cantea", "1_230");
					// Uruk Pursuer: a fierce [isengard] Uruk-hai, strength 5; his response answers a Hobbit killed
					put("pursuer", "4_190");
					// Goblin Patrol Troop: a [moria] Orc that is not fierce, and no text
					put("troop", "1_177");
					// Uruk Fighter: an [isengard] Uruk-hai that is not fierce; his own maneuver ability needs 5 companions
					put("fighter", "1_146");
					// Hunt Them Down!: "Maneuver: Make an Uruk-hai fierce until the regroup phase."
					put("hunt1", "1_126");
					put("hunt2", "1_126");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void YouCannotPassStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: You Cannot Pass!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Spell</b>.<br><b>Maneuver:</b> Spot Gandalf bearing a staff to prevent a minion from being <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pass");

		assertEquals("You Cannot Pass!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
        assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWhileGandalfBearsNoStaff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pass = scn.GetFreepsCard("pass");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var radagast = scn.GetFreepsCard("radagast");
		var wstaff = scn.GetFreepsCard("wstaff");
		var cantea = scn.GetShadowCard("cantea");
		scn.MoveCompanionsToTable(gandalf, radagast);
		// Gandalf bears a weapon and no staff; a staff is in play, borne by a Wizard who is not Gandalf.
		scn.AttachCardsTo(gandalf, glamdring);
		scn.AttachCardsTo(radagast, wstaff);
		scn.MoveCardsToHand(pass);

		scn.StartGame();
		assertAttachedTo(glamdring, gandalf);
		assertAttachedTo(wstaff, radagast);
		scn.MoveMinionsToTable(cantea);
		assertTrue(scn.HasKeyword(cantea, Keyword.FIERCE));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(pass));
		assertInHand(pass);
	}

	@Test
	public void WithGandalfBearingGandalfsStaffAMinionIsNotFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pass = scn.GetFreepsCard("pass");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gstaff = scn.GetFreepsCard("gstaff");
		var cantea = scn.GetShadowCard("cantea");
		var pursuer = scn.GetShadowCard("pursuer");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, gstaff);
		scn.MoveCardsToHand(pass);

		scn.StartGame();
		assertAttachedTo(gstaff, gandalf);
		scn.MoveMinionsToTable(cantea, pursuer, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.HasKeyword(cantea, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.FreepsPlayAvailable(pass));

		scn.FreepsPlayCard(pass);
		// "A minion": either fierce minion, of two cultures and races, and a minion that is not fierce as well; no
		// companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(cantea, pursuer, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(cantea);

		assertInDiscard(pass);
		assertFalse(scn.HasKeyword(cantea, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));

		// Nobody is assigned in the first round: the Pursuer alone is fierce, and the fierce round follows.
		scn.PassManeuverActions();
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		// Just before the regroup phase Cantëa is still not fierce, and is no fierce minion to defend against.
		assertFalse(scn.HasKeyword(cantea, Keyword.FIERCE));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(cantea));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(pursuer));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// Just after: fierce again.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.HasKeyword(cantea, Keyword.FIERCE));
	}

	@Test
	public void WithGandalfBearingAWizardStaffTheMinionCannotGainFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pass = scn.GetFreepsCard("pass");
		var gandalf = scn.GetFreepsCard("gandalf");
		var wstaff = scn.GetFreepsCard("wstaff");
		var pursuer = scn.GetShadowCard("pursuer");
		var fighter = scn.GetShadowCard("fighter");
		var hunt1 = scn.GetShadowCard("hunt1");
		var hunt2 = scn.GetShadowCard("hunt2");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, wstaff);
		scn.MoveCardsToHand(pass);
		scn.MoveCardsToHand(hunt1, hunt2);

		scn.StartGame();
		assertAttachedTo(wstaff, gandalf);
		scn.MoveMinionsToTable(pursuer, fighter);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(fighter, Keyword.FIERCE));
		assertTrue(scn.FreepsPlayAvailable(pass));

		scn.FreepsPlayCard(pass);
		assertTrue(scn.FreepsHasCardChoiceAvailable(pursuer, fighter));
		scn.FreepsChooseCard(pursuer);
		assertInDiscard(pass);
		assertFalse(scn.HasKeyword(pursuer, Keyword.FIERCE));

		// Hunt Them Down! on the Pursuer does not make him fierce while he is prevented from being so.
		assertTrue(scn.ShadowPlayAvailable(hunt1));
		scn.ShadowPlayCard(hunt1);
		scn.ShadowChooseCard(pursuer);
		assertInDiscard(hunt1);
		assertFalse(scn.HasKeyword(pursuer, Keyword.FIERCE));

		// The control: the same event on the other Uruk-hai makes him fierce.
		scn.FreepsPass();
		scn.ShadowPlayCard(hunt2);
		scn.ShadowChooseCard(fighter);
		assertTrue(scn.HasKeyword(fighter, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
