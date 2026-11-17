package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_071_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("company", "5_71");
					put("company2", "5_71");

					put("nomad", "7_132");      // Desert Nomad: a [raider] Southron Man; damage +1 only at 2 threats
					// Bladetusk Rearguard: [raider] artifact that its maneuver ability makes a Southron minion who is no Man
					put("rearguard", "103_43");
					put("buccaneer", "8_53");   // Corsair Buccaneer: a [raider] Man who is no Southron; a when-played trigger only
					put("savage", "15_92");     // Savage Southron: a [men] Man, a Southron by title only; a maneuver ability only

					// Durin's Secret: Fellowship event, spot Gandalf, a Dwarf and an Elf to make the move limit for this
					// turn +1.  It lifts the limit to 3, so that a -1 is not hidden by the floor of 1.
					put("secret", "1_71");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: a fellowship ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: the Dwarf; a skirmish ability used only on purpose
					put("arwen", "3_7");        // Arwen, Elven Rider: the Elf; a response that discards 3 cards from hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CompanyofHaradrimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Company of Haradrim
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>.<br><b>Regroup:</b> Remove (9) and spot another Southron to make the move limit -1 for this turn (to a minimum of 1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("company");

		assertEquals("Company of Haradrim", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupActionRemoves9ToMakeTheMoveLimitMinus1ForTheRestOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var secret = scn.GetFreepsCard("secret");
		scn.MoveCompanionsToTable("gandalf", "gimli", "arwen");
		scn.MoveCardsToHand(secret);

		var company = scn.GetShadowCard("company");
		var nomad = scn.GetShadowCard("nomad");

		scn.StartGame();
		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsPlayCard(secret);
		assertEquals(3, scn.GetMoveLimit()); // 2 + 1

		// The Company and another Southron, who stand unassigned until the regroup phase.
		scn.MoveMinionsToTable(company, nomad);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(1, scn.GetMoveCount());
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(company));
		scn.ShadowUseCardAction(company);
		assertEquals(1, scn.GetTwilight()); // 10 - 9
		assertEquals(2, scn.GetMoveLimit()); // 2 + 1 - 1

		// The fellowship may still move a second time, and does.
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();

		// The Shadow phase after the second move: the -1 has outlasted the regroup phase it was used in.
		assertEquals(2, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The other player's turn: his limit is 2, and nothing of the -1 is left.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void ASecondCompanyIsAnotherSouthron() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var company = scn.GetShadowCard("company");
		var company2 = scn.GetShadowCard("company2");

		scn.StartGame();
		scn.MoveMinionsToTable(company, company2);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(9);
		scn.FreepsPass();

		assertEquals(9, scn.GetTwilight());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.ShadowActionAvailable(company));
		scn.ShadowUseCardAction(company);
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASouthronWhoIsNoManIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var company = scn.GetShadowCard("company");
		var rearguard = scn.GetShadowCard("rearguard");
		scn.MoveCardsToSupportArea(rearguard);
		assertInZone(Zone.SUPPORT, rearguard);

		scn.StartGame();
		scn.MoveMinionsToTable(company);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();
		assertFalse(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		scn.ShadowUseCardAction(rearguard);
		// A Southron minion now, and still no Man.
		assertTrue(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertFalse(scn.IsRace(rearguard, Race.MAN));

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(9);
		scn.FreepsPass();

		assertEquals(9, scn.GetTwilight());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.ShadowActionAvailable(company));
		scn.ShadowUseCardAction(company);
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoOtherSouthronBesideCardsThatAreNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var company = scn.GetShadowCard("company");
		var buccaneer = scn.GetShadowCard("buccaneer");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		// The Company itself, a [raider] Man who is no Southron, and a Southron by title only.
		scn.MoveMinionsToTable(company, buccaneer, savage);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(9);
		scn.FreepsPass();

		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(company));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void IsNotOfferedWithOnly8ToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var company = scn.GetShadowCard("company");
		var nomad = scn.GetShadowCard("nomad");

		scn.StartGame();
		scn.MoveMinionsToTable(company, nomad);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(8);
		scn.FreepsPass();

		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(company));

		scn.SetTwilight(9);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(company));
		assertEquals(2, scn.GetMoveLimit());
	}
}
