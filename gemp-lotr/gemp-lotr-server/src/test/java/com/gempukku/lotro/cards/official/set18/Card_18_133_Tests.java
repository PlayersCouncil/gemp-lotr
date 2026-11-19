package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_133_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pull", "18_133");

					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl; strength -2 only for
					                            // characters bearing a [wraith] condition
					put("steed", "1_208");      // Black Steed: a [wraith] possession on a Nazgûl, the [wraith] card that is no minion
					put("whelp", "7_202");      // Morgul Whelp: a [wraith] Orc, strength 3, vitality 1, not fierce;
					                            // +6 while the Ring-bearer is wounded
					put("rider", "12_161");     // Black Rider: a [wraith] Nazgûl, fierce; strength 8, +1 for each companion
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl, fierce; Easterling bonuses
					                            // at 3 and 5 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, strength 5, vitality 2; strength +4 and fierce while
					                            // 2 wounds can be spotted on the Ring-bearer

					// Six burdens take a companion of resistance 5 or 6 to 0; the burden removed puts the 6 back at 1.
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit, resistance 5; a fellowship ability only
					put("boromir", "11_57");    // Boromir, Hero of Osgiliath: [gondor] Man, resistance 5
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: resistance 6, strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					// Thrarin, Dwarven Smith: an ally (resistance 0 by rule) who can be made to take part in skirmishes
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PullOfTheRingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Pull of the Ring
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: Remove a burden to choose one: assign a [wraith] minion to a companion who has resistance 0; or
		 * 	have a [wraith] minion lose <b>fierce</b> and gain <b>hunter 2</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var pull = scn.GetFreepsCard("pull");

		assertEquals("Pull of the Ring", pull.getBlueprint().getTitle());
		assertNull(pull.getBlueprint().getSubtitle());
		assertFalse(pull.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, pull.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, pull.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, pull.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(pull, Timeword.ASSIGNMENT));
		assertEquals(0, pull.getBlueprint().getTwilightCost());
	}

	@Test
	public void RemovesABurdenAndAssignsAWraithMinionToACompanionWhoHasResistanceZeroOnceTheBurdenIsGone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(sam, boromir, aragorn);
		scn.MoveAlliesToTable(thrarin);

		var pull = scn.GetShadowCard("pull");
		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");
		var whelp = scn.GetShadowCard("whelp");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(pull);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, whelp, khamul);
		scn.AttachCardsTo(lemenya, steed);
		scn.AddBurdens(6);
		assertInHand(pull);
		assertInZone(Zone.ATTACHED, steed);
		assertEquals(6, scn.GetBurdens());

		// Thrarin takes part in skirmishes from now to the regroup phase, so that he could be assigned.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(6, scn.GetBurdens());
		assertEquals(0, scn.GetResistance(sam));
		assertEquals(0, scn.GetResistance(boromir));
		assertEquals(0, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(frodo));
		assertEquals(0, scn.GetResistance(thrarin));

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(pull));
		scn.ShadowPlayCard(pull);

		// The burden is paid first, and resistance is read after it.
		assertEquals(5, scn.GetBurdens());
		assertEquals(0, scn.GetResistance(sam));
		assertEquals(0, scn.GetResistance(boromir));
		assertEquals(1, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(frodo));

		// Both branches can be done: the choice is asked.
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChoose("Assign");

		// A [shire] Hobbit and a [gondor] Man at resistance 0; not Aragorn, at 0 before the burden came off and 1
		// now, nor Frodo, nor the ally, who has resistance 0 and can be assigned.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, boromir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, frodo, thrarin));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(boromir);

		// A [wraith] Nazgûl and a [wraith] Orc; not the [raider] Nazgûl, nor the [wraith] possession.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, whelp));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, steed));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(whelp);

		assertTrue(scn.IsCharAssignedAgainst(boromir, whelp));
		assertFalse(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(lemenya));
		assertFalse(scn.IsCharAssigned(khamul));
		assertInZone(Zone.DISCARD, pull);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoCompanionAtResistanceZeroAWraithMinionLosesFierceAndGainsHunter2WithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var pull = scn.GetShadowCard("pull");
		var rider = scn.GetShadowCard("rider");
		var steed = scn.GetShadowCard("steed");
		var whelp = scn.GetShadowCard("whelp");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(pull);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, whelp, khamul);
		scn.AttachCardsTo(rider, steed);
		scn.AddBurdens(1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertInHand(pull);
		assertInZone(Zone.ATTACHED, steed);
		assertEquals(1, scn.GetBurdens());
		assertEquals(5, scn.GetResistance(aragorn));
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(rider, Keyword.HUNTER));
		assertTrue(scn.HasKeyword(khamul, Keyword.FIERCE));

		scn.FreepsPass();
		scn.ShadowPlayCard(pull);
		assertEquals(0, scn.GetBurdens());

		// No companion has resistance 0, so only the second branch can be done, and it is carried out unasked:
		// the choice is of the minion. A fierce [wraith] Nazgûl and a [wraith] Orc who is not fierce; not the
		// fierce [raider] Nazgûl, nor the [wraith] possession.
		assertTrue(scn.ShadowHasCardChoiceAvailable(rider, whelp));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, steed));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(rider);

		assertFalse(scn.HasKeyword(rider, Keyword.FIERCE));
		assertEquals(2, scn.GetKeywordCount(rider, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(whelp, Keyword.HUNTER));
		assertTrue(scn.HasKeyword(khamul, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(khamul, Keyword.HUNTER));
		assertFalse(scn.IsCharAssigned(rider));
		assertInZone(Zone.DISCARD, pull);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithBothBranchesPossibleTheLossOfFierceCanBeChosenOverTheAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);

		var pull = scn.GetShadowCard("pull");
		var rider = scn.GetShadowCard("rider");
		scn.MoveCardsToHand(pull);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		scn.AddBurdens(6);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertInHand(pull);
		assertEquals(6, scn.GetBurdens());
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(rider, Keyword.HUNTER));

		scn.FreepsPass();
		scn.ShadowPlayCard(pull);
		assertEquals(5, scn.GetBurdens());
		// Sam is at resistance 0 even with the burden gone: both branches can be done, and both are offered.
		assertEquals(0, scn.GetResistance(sam));
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChoose("lose fierce");

		assertFalse(scn.HasKeyword(rider, Keyword.FIERCE));
		assertEquals(2, scn.GetKeywordCount(rider, Keyword.HUNTER));
		assertFalse(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(rider));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheLossOfFierceAndHunter2LastUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var pull = scn.GetShadowCard("pull");
		var rider = scn.GetShadowCard("rider");
		var whelp = scn.GetShadowCard("whelp");
		scn.MoveCardsToHand(pull);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, whelp);
		scn.AddBurdens(1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertInHand(pull);
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(rider, Keyword.HUNTER));

		scn.FreepsPass();
		scn.ShadowPlayCard(pull);
		// No companion has resistance 0: the second branch, unasked.
		scn.ShadowChooseCard(rider);
		assertFalse(scn.HasKeyword(rider, Keyword.FIERCE));
		assertEquals(2, scn.GetKeywordCount(rider, Keyword.HUNTER));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, whelp}, new PhysicalCardImpl[]{aragorn, rider});
		// Gimli's 6 against the Whelp's 3 kills it; Aragorn's skirmish is fought second.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, whelp);
		scn.FreepsResolveSkirmish(aragorn);

		// Still in force in a skirmish that follows one already resolved.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.HasKeyword(rider, Keyword.FIERCE));
		assertEquals(2, scn.GetKeywordCount(rider, Keyword.HUNTER));

		scn.PassSkirmishActions();
		assertInZone(Zone.SHADOW_CHARACTERS, rider);
		// The Rider is not fierce as the skirmishes end, so there is no fierce round: the regroup phase comes next,
		// and there the Rider is fierce again and no hunter.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(rider, Keyword.HUNTER));
	}

	@Test
	public void AMinionThatWouldGainFierceFromItsOwnTextAfterLosingItIsNotFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var pull = scn.GetShadowCard("pull");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(pull);

		scn.StartGame();
		scn.MoveMinionsToTable(hound);
		scn.AddBurdens(1);
		// One wound on the Ring-bearer: the Hound is not yet fierce, and will be once he takes a second.
		scn.AddWoundsToChar(frodo, 1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertInHand(pull);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.HasKeyword(hound, Keyword.FIERCE));
		assertEquals(5, scn.GetStrength(hound));

		scn.FreepsPass();
		scn.ShadowPlayCard(pull);
		// The minion is chosen unasked: the Hound is the only [wraith] minion, and no companion has resistance 0.
		assertEquals(2, scn.GetKeywordCount(hound, Keyword.HUNTER));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(frodo, hound);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();
		// 5 and hunter 2 against Frodo's 4: he loses, and the Ring's response is declined.
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(frodo));

		// Two wounds on the Ring-bearer: the Hound's own text makes it strength +4 and fierce, but the fierce it has
		// lost is not given back before the regroup phase. No fierce round; in the regroup phase it is fierce.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(9, scn.GetStrength(hound));
		assertTrue(scn.HasKeyword(hound, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(hound, Keyword.HUNTER));
	}

	@Test
	public void WithNoWraithMinionTheBurdenIsRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var pull = scn.GetShadowCard("pull");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(pull);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		scn.AddBurdens(1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertInHand(pull);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.HasKeyword(khamul, Keyword.FIERCE));

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(pull));
		scn.ShadowPlayCard(pull);

		// Neither branch has a [wraith] minion to act on: nothing is asked, and only the burden is gone.
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.DISCARD, pull);
		assertTrue(scn.HasKeyword(khamul, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(khamul, Keyword.HUNTER));
		assertFalse(scn.IsCharAssigned(khamul));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoBurdenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pull = scn.GetShadowCard("pull");
		var rider = scn.GetShadowCard("rider");
		scn.MoveCardsToHand(pull);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertInHand(pull);
		assertEquals(0, scn.GetBurdens());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(pull));

		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(pull));
	}
}
