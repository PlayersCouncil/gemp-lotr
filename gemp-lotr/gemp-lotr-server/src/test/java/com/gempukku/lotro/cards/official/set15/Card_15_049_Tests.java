package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_049_Tests
{

	// The Fellowship path; the skirmishes are fought at site 2, Ettenmoors 1_331, whose skirmish ability is used
	// only on purpose.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "15_49");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer, resistance 5, vitality 4
					// Bilbo, Melancholy Hobbit: a Ring-bound Hobbit companion, vitality 4; his start-of-skirmish
					// trigger answers only a skirmish he is in, and he fights none
					put("bilbo", "12_119");
					put("merry", "1_302");      // Merry, Friend to Sam: an unbound Hobbit, resistance 6; a skirmish ability used only on purpose
					put("boromir", "0_65");     // Boromir, Steward's Heir: a Ring-bound Man, resistance 6; keywords only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound Man, strength 8, resistance 6; a maneuver ability used only on purpose

					put("brigand", "4_10");     // Dunlending Brigand: strength 8, vitality 1, no text: double a Sméagol of 4, short of triple
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; cheated in, so its text never comes up
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SmeagolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Sméagol, Wretched and Hungry
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype:
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 5
		 * Game Text: <b>Ring-bound</b>. To play, add a burden.<br>Each Ring-bound companion is resistance +1.<br><b>Skirmish:</b> Exert 2 Ring-bound Hobbits to make Sméagol strength +1 and prevent him from being overwhelmed unless his strength is tripled.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("smeagol");

		assertEquals("Sméagol", card.getBlueprint().getTitle());
		assertEquals("Wretched and Hungry", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
	}

	@Test
	public void PlayingHimAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(smeagol);

		scn.StartGame();
		assertInHand(smeagol);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(smeagol));
		scn.FreepsPlayCard(smeagol);

		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachRingBoundCompanionHimselfIncludedIsResistancePlusOneAndNoUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, boromir, merry, aragorn);

		scn.StartGame();
		assertEquals(0, scn.GetBurdens());
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(6, scn.GetResistance(boromir));
		assertEquals(6, scn.GetResistance(merry));
		assertEquals(6, scn.GetResistance(aragorn));

		scn.MoveCompanionsToTable(smeagol);

		// Ring-bound: the Ring-bearer, a Hobbit who is not, a Man, and Sméagol himself (5 + 1).
		assertEquals(11, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(sam));
		assertEquals(7, scn.GetResistance(boromir));
		assertEquals(6, scn.GetResistance(smeagol));
		// Unbound: a Hobbit and a Man.
		assertEquals(6, scn.GetResistance(merry));
		assertEquals(6, scn.GetResistance(aragorn));
	}

	@Test
	public void SkirmishAbilityExertsTwoRingBoundHobbitsToMakeHimStrengthPlusOneAndSpareHimAnOverwhelmShortOfTriple() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(smeagol, sam, bilbo, merry, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, brigand);
		scn.FreepsResolveSkirmish(smeagol);

		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(2, scn.GetOverwhelmMultiplier(smeagol));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);

		// Exactly 2, among the Ring-bound Hobbits; not an unbound Hobbit, a Ring-bound Man, or Sméagol.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, bilbo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, boromir, smeagol));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(sam, bilbo);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(4, scn.GetStrength(smeagol));
		assertEquals(3, scn.GetOverwhelmMultiplier(smeagol));

		// 8 against 4 is double and short of triple: he loses, takes the one wound, and lives.
		scn.PassSkirmishActions();
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOnlyOneRingBoundHobbitAbleToExertItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(smeagol, sam, merry, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		// Sam, vitality 4, exhausted; Frodo, Merry and Boromir could exert.
		scn.AddWoundsToChar(sam, 3);
		assertEquals(3, scn.GetWoundsOn(sam));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, brigand);
		scn.FreepsResolveSkirmish(smeagol);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(smeagol));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetStrength(smeagol));
	}

	@Test
	public void UsedUnassignedInAnotherSkirmishHeIsStrengthPlusOneThereAndNotOnceThePhaseIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(smeagol, sam, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.IsCharAssigned(smeagol));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(2, scn.GetOverwhelmMultiplier(smeagol));
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);
		// Frodo and Sam are the only two to exert.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(4, scn.GetStrength(smeagol));
		assertEquals(3, scn.GetOverwhelmMultiplier(smeagol));

		// Aragorn wins; the skirmish phase is over and with it the bonus.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(2, scn.GetOverwhelmMultiplier(smeagol));
	}

	@Test
	public void UsedWhileHeWaitsForHisOwnSkirmishItIsGoneWhenThatSkirmishComes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(smeagol, sam, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{smeagol, brigand});
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.FreepsUseCardAction(smeagol);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(4, scn.GetStrength(smeagol));
		assertEquals(3, scn.GetOverwhelmMultiplier(smeagol));

		// His own skirmish is a phase of its own: 3 against 8, and overwhelmed.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(smeagol);
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, brigand));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(2, scn.GetOverwhelmMultiplier(smeagol));
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, smeagol);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
