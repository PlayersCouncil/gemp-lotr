package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_171_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thrice", "7_171");
					// Southron Explorer: a [raider] Southron Man; a regroup ability used only on purpose
					put("explorer", "4_250");
					// Easterling Infantry: a [raider] Easterling Man; fierce and damage +1 only at 3 burdens
					put("infantry", "4_227");
					// Desert Soldier: a [raider] Southron Man; an optional trigger at the start of its own skirmish only
					put("desertsoldier", "4_220");
					// Southron Sentry: a [raider] Southron Man; a maneuver ability used only on purpose
					put("sentry", "4_253");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is a Nazgul and no Man; fierce; his other
					// text needs 3 burdens
					put("khamul", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a Man who is not [raider]; no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: vitality 4, laid exhausted; abilities used only on purpose
					// Albert Dreary, Entertainer from Bree: a Man ally; a maneuver ability used only on purpose
					put("albert", "1_69");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThriceOutnumberedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Thrice Outnumbered
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Spot a threat and X [raider] Men to make the Free Peoples player exert X companions (limit 3).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("thrice");

		assertEquals("Thrice Outnumbered", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAThreatAndTwoRaiderMenTheFreePeoplesPlayerExertsTwoCompanionsOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var albert = scn.GetFreepsCard("albert");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, sam);
		scn.MoveAlliesToTable(albert);

		var thrice = scn.GetShadowCard("thrice");
		var explorer = scn.GetShadowCard("explorer");
		var infantry = scn.GetShadowCard("infantry");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(thrice);
		assertInHand(thrice);

		scn.StartGame();
		// Sam, vitality 4, is exhausted.
		scn.AddWoundsToChar(sam, 3);
		assertEquals(3, scn.GetWoundsOn(sam));
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());

		// Two [raider] Men, a Southron and an Easterling, beside a [raider] minion who is no Man and a Man who is not
		// [raider]; all left unassigned so that they stand in the regroup phase.
		scn.MoveMinionsToTable(explorer, infantry, khamul, brigand);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		// Khamul is fierce: a fierce assignment phase, with nobody assigned again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(thrice));
		scn.ShadowPlayCard(thrice);

		// The Shadow player says how many [raider] Men he spots: up to the two, filled in with both.
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("2");

		// The Free Peoples player chooses the two companions; not the exhausted Sam, nor the ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, albert));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, gimli);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(albert));
		assertInDiscard(thrice);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerMaySpotFewerRaiderMenThanThereAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var thrice = scn.GetShadowCard("thrice");
		var explorer = scn.GetShadowCard("explorer");
		var infantry = scn.GetShadowCard("infantry");
		scn.MoveCardsToHand(thrice);
		assertInHand(thrice);

		scn.StartGame();
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.MoveMinionsToTable(explorer, infantry);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(3);
		scn.FreepsPass();

		scn.ShadowPlayCard(thrice);
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInDiscard(thrice);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFourRaiderMenNoMoreThanThreeCompanionsExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);

		var thrice = scn.GetShadowCard("thrice");
		var explorer = scn.GetShadowCard("explorer");
		var infantry = scn.GetShadowCard("infantry");
		var desertsoldier = scn.GetShadowCard("desertsoldier");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCardsToHand(thrice);
		assertInHand(thrice);

		scn.StartGame();
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.MoveMinionsToTable(explorer, infantry, desertsoldier, sentry);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(3);
		scn.FreepsPass();

		scn.ShadowPlayCard(thrice);
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("4");

		// Four companions able to exert, and the limit of 3.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli, boromir));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, gimli, boromir);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(thrice);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAThreatIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		var thrice = scn.GetShadowCard("thrice");
		var explorer = scn.GetShadowCard("explorer");
		var infantry = scn.GetShadowCard("infantry");
		scn.MoveCardsToHand(thrice);
		assertInHand(thrice);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, infantry);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(3);
		scn.FreepsPass();

		// Two [raider] Men and the twilight to pay, but no threat.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(thrice));

		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(thrice));
	}

	@Test
	public void TheNumberOfRaiderMenSpottedRunsFrom1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var thrice = scn.GetShadowCard("thrice");
		var explorer = scn.GetShadowCard("explorer");
		var infantry = scn.GetShadowCard("infantry");
		scn.MoveCardsToHand(thrice);
		assertInHand(thrice);

		scn.StartGame();
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.MoveMinionsToTable(explorer, infantry);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(3);
		scn.FreepsPass();

		scn.ShadowPlayCard(thrice);
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("1");

		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
