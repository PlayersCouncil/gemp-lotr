package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_045_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hurry", "15_45");
					// Sméagol, Poor Creature: [gollum] companion, Ring-bound, no race, strength 3; to play, add a burden
					// (cheated in); his skirmish ability is not used
					put("smeagol", "6_45");
					// Smeagol, Once Like Us: the [shire] Sméagol, Ring-bound, no race, strength 3 (cheated in); an
					// assignment ability only
					put("shiresmeagol", "103_116");
					// Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer, strength 3
					put("sam", "1_311");
					// Bilbo, Melancholy Hobbit: a Ring-bound Hobbit companion; his start-of-skirmish offer is not reached
					put("bilbo", "12_119");
					// Bilbo, Retired Adventurer: a Hobbit ally, and so not Ring-bound
					put("bilboally", "1_284");
					// Merry, From O'er the Brandywine: an unbound Hobbit, strength 3 while he bears no weapon
					put("merry", "1_303");
					// Faramir, Son of Denethor: a Ring-bound Man, strength 7; his bar is on the Shadow player
					put("faramir", "4_117");
					// Goblin Runner: strength 5, vitality 1 (its (2) is not met: cheated in)
					put("runner", "1_178");
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HurryHobbitsesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Hurry Hobbitses
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make Sméagol strength +1 for each Ring- bound Hobbit you can spot.
		*/

		var scn = GetScenario();

		var hurry = scn.GetFreepsCard("hurry");

		assertEquals("Hurry Hobbitses", hurry.getBlueprint().getTitle());
		assertNull(hurry.getBlueprint().getSubtitle());
		assertFalse(hurry.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, hurry.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, hurry.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, hurry.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(hurry, Timeword.SKIRMISH));
		assertEquals(1, hurry.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesSmeagolStrengthPlus1ForEachRingBoundHobbitAndCountsNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hurry = scn.GetFreepsCard("hurry");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var bilboally = scn.GetFreepsCard("bilboally");
		var merry = scn.GetFreepsCard("merry");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(smeagol, sam, merry, faramir);
		scn.MoveCardsToSupportArea(bilboally);
		scn.MoveCardsToHand(hurry);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, bilboally);
		assertInHand(hurry);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, runner);
		scn.FreepsResolveSkirmish(smeagol);
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(3, scn.GetStrength(sam));

		scn.FreepsPlayCard(hurry);
		// Frodo and Sam: 3 + 2. Merry is unbound, Faramir no Hobbit, Bilbo an ally (only companions are
		// Ring-bound), and Sméagol, Ring-bound, has no race.
		assertEquals(5, scn.GetStrength(smeagol));
		assertEquals(3, scn.GetStrength(sam));
		assertInDiscard(hurry);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ASmeagolInNoSkirmishIsMadeStrongerTooAndAThirdRingBoundHobbitIsCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hurry = scn.GetFreepsCard("hurry");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(smeagol, sam, bilbo, merry);
		scn.MoveCardsToHand(hurry);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInHand(hurry);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner);
		scn.FreepsResolveSkirmish(merry);
		assertFalse(scn.IsCharSkirmishing(smeagol));
		assertEquals(3, scn.GetStrength(smeagol));

		scn.FreepsPlayCard(hurry);
		// Frodo, Sam and Bilbo: 3 + 3.
		assertEquals(6, scn.GetStrength(smeagol));
		assertEquals(3, scn.GetStrength(merry));
		assertInDiscard(hurry);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheShireSmeagolIsSmeagolToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hurry = scn.GetFreepsCard("hurry");
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(hurry);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInHand(hurry);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, runner);
		scn.FreepsResolveSkirmish(smeagol);
		assertEquals(3, scn.GetStrength(smeagol));

		scn.FreepsPlayCard(hurry);
		// Frodo alone: 3 + 1.
		assertEquals(4, scn.GetStrength(smeagol));
		assertInDiscard(hurry);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoSmeagolInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hurry = scn.GetFreepsCard("hurry");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveCardsToHand(hurry);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInHand(hurry);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner);
		scn.FreepsResolveSkirmish(merry);

		assertTrue(scn.FreepsPlayAvailable(hurry));
		scn.FreepsPlayCard(hurry);
		assertInDiscard(hurry);
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hurry = scn.GetFreepsCard("hurry");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(smeagol, sam);
		scn.MoveCardsToHand(hurry);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInHand(hurry);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(hurry));
	}

	@Test
	public void TheBonusLastsTheSkirmishItIsPlayedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hurry = scn.GetFreepsCard("hurry");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(smeagol, sam);
		scn.MoveCardsToHand(hurry);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		assertInHand(hurry);
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{smeagol, runner}, new PhysicalCardImpl[]{sam, runner2});
		scn.FreepsResolveSkirmish(smeagol);
		scn.FreepsPlayCard(hurry);
		// Frodo and Sam: 3 + 2.
		assertEquals(5, scn.GetStrength(smeagol));
		// Still in force with the skirmish about to end.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(smeagol));

		// Sméagol's 5 against the Runner's 5: a tie goes to the Shadow side, and Sméagol takes a wound.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(smeagol));
		scn.FreepsResolveSkirmish(sam);
		assertEquals(3, scn.GetStrength(smeagol));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
