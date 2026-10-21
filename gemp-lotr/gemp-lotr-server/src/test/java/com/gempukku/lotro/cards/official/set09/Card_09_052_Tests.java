package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_052_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tom", "9_52");
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, strength 3, vitality 4; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit, vitality 4; to play, add a burden (cheated in);
					// an assignment ability used only on purpose
					put("smeagol", "103_116");
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli, Bearer of Grudges as the Ring-bearer, a Dwarf, so that Sam can be the only [shire] companion.
	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tom", "9_52");
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("goldberry", "9_51");   // Goldberry, River-daughter: a [shire] ally; her texts need a river (Ettenmoors is a plains)
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFellowshipBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tom", "9_52");
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion; a fellowship ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Fellowship
		);
	}

	protected VirtualTableScenario GetMultipathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tom", "9_52");
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion; a fellowship ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath
		);
	}

	@Test
	public void TomBombadilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Tom Bombadil, The Master
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 5
		 * Type: Ally
		 * Subtype:
		 * Strength: 14
		 * Vitality: 9
		 * Site Number: 2
		 * Game Text: To play, remove 2 burdens or 2 threats.<br><b>Skirmish:</b> Spot 2 [shire] companions and exert Tom Bombadil X times, where X is the fellowship's site number, to cancel a skirmish involving a [shire] companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tom");

		assertEquals("Tom Bombadil", card.getBlueprint().getTitle());
		assertEquals("The Master", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(9, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 2)));
	}

	@Test
	public void WithBothTwoBurdensAndTwoThreatsThePlayerChoosesWhichToRemoveToPlayTom() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tom = scn.GetFreepsCard("tom");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(tom);

		scn.StartGame();
		// Two companions: room for two threats.
		scn.AddBurdens(2);
		scn.AddThreats(2);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(tom));
		scn.FreepsPlayCard(tom);

		assertEquals(List.of("Remove 2 burdens", "Remove 2 threats"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Remove 2 threats");

		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, tom);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTwoBurdensAndNoThreatTheBurdensAreRemovedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tom = scn.GetFreepsCard("tom");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(tom);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		scn.FreepsPlayCard(tom);

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SUPPORT, tom);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTwoThreatsAndOneBurdenTheThreatsAreRemovedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tom = scn.GetFreepsCard("tom");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(tom);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.AddThreats(2);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetThreats());
		scn.FreepsPlayCard(tom);

		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, tom);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOneBurdenAndOneThreatUntilASecondBurdenIsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tom = scn.GetFreepsCard("tom");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(tom);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		assertInHand(tom);
		assertFalse(scn.FreepsPlayAvailable(tom));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.AddBurdens(1);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(tom));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSite2TomExertsTwiceToCancelTheSkirmishOfAShireCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tom = scn.GetFreepsCard("tom");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(tom);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Not in the assignment phase, the phase before.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(tom));
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignToMinions(sam, runner);
		scn.FreepsResolveSkirmish(sam);

		// Frodo and Sam are the two [shire] companions spotted.
		assertTrue(scn.IsCharSkirmishingAgainst(sam, runner));
		assertEquals(0, scn.GetWoundsOn(tom));
		assertTrue(scn.FreepsActionAvailable(tom));
		scn.FreepsUseCardAction(tom);

		// X is 2, the site number.
		assertEquals(2, scn.GetWoundsOn(tom));
		// Cancelled: the Runner (5) would have beaten Sam (3).
		assertFalse(scn.IsCharSkirmishing(sam));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtSite3TomExertsThreeTimesToCancelTheSkirmishOfAShireCompanionWhoIsNoHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tom = scn.GetFreepsCard("tom");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveAlliesToTable(tom);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();   // at Rivendell Valley, site 3
		scn.FreepsAssignToMinions(smeagol, runner);
		scn.FreepsResolveSkirmish(smeagol);

		// Frodo and Smeagol, a [shire] companion who is no Hobbit, are spotted.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, runner));
		assertEquals(0, scn.GetWoundsOn(tom));
		assertTrue(scn.FreepsActionAvailable(tom));
		scn.FreepsUseCardAction(tom);

		assertEquals(3, scn.GetWoundsOn(tom));
		assertFalse(scn.IsCharSkirmishing(smeagol));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InASkirmishWithNoShireCompanionTomPaysAndTheSkirmishGoesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tom = scn.GetFreepsCard("tom");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(tom);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		// Frodo and Sam are spotted; the skirmish is Aragorn's.  What stands after the "to" is effect.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertEquals(0, scn.GetWoundsOn(tom));
		assertTrue(scn.FreepsActionAvailable(tom));
		scn.FreepsUseCardAction(tom);

		assertEquals(2, scn.GetWoundsOn(tom));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyOneShireCompanionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var tom = scn.GetFreepsCard("tom");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var goldberry = scn.GetFreepsCard("goldberry");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(tom, goldberry);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner);
		scn.FreepsResolveSkirmish(sam);

		// Sam is the one [shire] companion: Goldberry is a [shire] ally, Aragorn a companion of another culture,
		// and the Ring-bearer a Dwarf.
		assertTrue(scn.IsCharSkirmishingAgainst(sam, runner));
		assertEquals(0, scn.GetWoundsOn(tom));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(tom));
	}

	@Test
	public void NotOfferedWhileTomCannotExertAsManyTimesAsTheSiteNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tom = scn.GetFreepsCard("tom");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(tom);
		// Vitality 9 with 7 wounds: he can exert once more, and two exertions are wanted at site 2.
		scn.AddWoundsToChar(tom, 7);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertEquals(7, scn.GetWoundsOn(tom));
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner);
		scn.FreepsResolveSkirmish(sam);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.IsCharSkirmishingAgainst(sam, runner));
		assertEquals(7, scn.GetWoundsOn(tom));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(tom));
	}

	@Test
	public void InTheFellowshipBlockFormatTomCancelsTheRingBearersSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var tom = scn.GetFreepsCard("tom");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(tom);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, runner);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, runner));
		assertEquals(0, scn.GetWoundsOn(tom));
		assertTrue(scn.FreepsActionAvailable(tom));
		scn.FreepsUseCardAction(tom);

		assertEquals(2, scn.GetWoundsOn(tom));
		// Cancelled: the Runner (5) would have beaten Frodo (3, +1 from the Ring).
		assertFalse(scn.IsCharSkirmishing(frodo));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InMultipathTomPaysAndTheRingBearersSkirmishGoesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMultipathScenario();

		var tom = scn.GetFreepsCard("tom");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(tom);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, runner);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, runner));
		assertEquals(0, scn.GetWoundsOn(tom));
		// The exertions can be paid, so the ability is used; the skirmish is the Ring-bearer's and goes on.
		assertTrue(scn.FreepsActionAvailable(tom));
		scn.FreepsUseCardAction(tom);

		assertEquals(2, scn.GetWoundsOn(tom));
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
