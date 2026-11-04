package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_145_Tests
{

	// The Fellowship path: site 2 Ettenmoors, where the skirmishes are, is a plains and no dwelling.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("letgo", "13_145");
					put("letgo2", "13_145");

					put("bilbo", "12_119");     // Bilbo, Melancholy Hobbit: Hobbit companion, strength 3, vitality 4, resistance 8; a start-of-skirmish offer that wants a [shire] skirmish event in the discard pile (none)
					put("merry", "1_303");      // Merry, From O'er the Brandywine: Hobbit companion, strength 3, resistance 6; +2 with a weapon (none)
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; enduring, a skirmish ability never used
					put("bounder", "1_286");    // Bounder: Hobbit ALLY; a skirmish ability never used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion; a maneuver ability used only on purpose

					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its text answers only its own play
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					// Ted Sandyman, Chief's Men's Ally: the opponent's Hobbit, a [men] minion; a maneuver and a regroup
					// ability never used here
					put("ted", "18_75");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A Shadows path: the fellowship moves from Fangorn Glade 11_239 to Buckland Homestead 11_230 (site6 of the
	// map), a dwelling whose Shadow ability is never used.  Uruk Pursuer is fierce and left unassigned in the first
	// round, so that its skirmish is the fierce one.
	protected VirtualTableScenario GetDwellingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("letgo", "13_145");
					put("letgo2", "13_145");

					put("bilbo", "12_119");     // Bilbo, Melancholy Hobbit: Hobbit companion, strength 3, resistance 8; a start-of-skirmish offer that wants a [shire] skirmish event in the discard pile (none)
					put("merry", "1_303");      // Merry, From O'er the Brandywine: Hobbit companion, strength 3, resistance 6; +2 with a weapon (none)

					put("pursuer", "4_190");    // Uruk Pursuer: fierce, strength 5, vitality 3; its response answers an unbound Hobbit killed
				}},
				VirtualTableScenario.ShadowsSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	// A Shadows path: the fellowship moves from Fangorn Glade 11_239 to Buckland Homestead 11_230, a dwelling with no
	// text, where the skirmishes are.  Allies have no home sites on a Shadows path (CR 4.2 "ally"), so Rosie
	// Cotton is at no site.  No Hobbit ally has a home that is a dwelling.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("letgo", "13_145");
					put("letgo2", "13_145");
					put("rosie", "1_309");      // Rosie Cotton, Hobbiton Lass: Hobbit ally, home 1F, strength 1; "Sam is strength +1", and a fellowship ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.ShadowsSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void DontLetGoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Don't Let Go
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Skirmish:</b> Discard this condition to make a Hobbit strength +1 for each of the following that is true: he or she is participating in a fierce skirmish; he or she has resistance 7 or more; he or she is at a dwelling site; you can spot an unwounded Hobbit.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("letgo");

		assertEquals("Don't Let Go", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SkirmishAbilityDiscardsItselfToMakeAHobbitStrongerForResistanceSevenOrMoreForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var letgo = scn.GetFreepsCard("letgo");
		var letgo2 = scn.GetFreepsCard("letgo2");
		var frodo = scn.GetRingBearer();
		var bilbo = scn.GetFreepsCard("bilbo");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(bilbo, merry, pippin, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(letgo, letgo2);
		// Every Hobbit is wounded; Aragorn, no Hobbit, is not.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(bilbo, 1);
		scn.AddWoundsToChar(merry, 1);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(bounder, 1);

		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, letgo, letgo2);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(bounder));
		scn.MoveMinionsToTable(runner, troop, ted);
		scn.AddWoundsToChar(ted, 1);
		scn.SkipToAssignments();
		assertEquals(1, scn.GetWoundsOn(ted));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bilbo, runner}, new PhysicalCardImpl[]{aragorn, troop});
		// Ted Sandyman is left out of the skirmishes.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(bilbo);
		// Bilbo's own start-of-skirmish offer (an exertion paid for nothing, the discard pile empty) is declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetBurdens());
		assertEquals(8, scn.GetResistance(bilbo));
		assertEquals(3, scn.GetStrength(bilbo));

		assertTrue(scn.FreepsActionAvailable(letgo));
		scn.FreepsUseCardAction(letgo);
		// Hobbits: companions of two cultures, in this skirmish and out of it, an ally, and the opponent's minion;
		// not a Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, frodo, merry, pippin, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(bilbo);

		// No fierce skirmish, no dwelling, no unwounded Hobbit; resistance 8: +1
		assertEquals(4, scn.GetStrength(bilbo));
		assertInDiscard(letgo);
		assertInZone(Zone.SUPPORT, letgo2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The bonus lasts the skirmish phase: in the next skirmish Bilbo is himself again.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(bilbo));
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(3, scn.GetStrength(bilbo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtResistanceSevenWithAnUnwoundedHobbitCompanionSpottedTheHobbitIsStrengthPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var letgo = scn.GetFreepsCard("letgo");
		var frodo = scn.GetRingBearer();
		var bilbo = scn.GetFreepsCard("bilbo");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(bilbo, merry);
		scn.MoveCardsToSupportArea(letgo);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(bilbo, 1);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);
		// Bilbo's own start-of-skirmish offer (an exertion paid for nothing, the discard pile empty) is declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsDeclineOptionalTrigger();
		// 8 - 1 burden
		assertEquals(7, scn.GetResistance(bilbo));
		assertEquals(3, scn.GetStrength(bilbo));

		scn.FreepsUseCardAction(letgo);
		scn.FreepsChooseCard(bilbo);

		// Resistance 7 +1; Merry, an unwounded Hobbit, +1
		assertEquals(5, scn.GetStrength(bilbo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtResistanceSixAHobbitAllySpottedUnwoundedAddsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var letgo = scn.GetFreepsCard("letgo");
		var frodo = scn.GetRingBearer();
		var bilbo = scn.GetFreepsCard("bilbo");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(bilbo);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(letgo);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(bilbo, 1);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);
		// Bilbo's own start-of-skirmish offer (an exertion paid for nothing, the discard pile empty) is declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsDeclineOptionalTrigger();
		// 8 - 2 burdens
		assertEquals(6, scn.GetResistance(bilbo));
		assertEquals(3, scn.GetStrength(bilbo));

		scn.FreepsUseCardAction(letgo);
		scn.FreepsChooseCard(bilbo);

		// Resistance 6: nothing; Bounder, a Hobbit ally, unwounded: +1
		assertEquals(4, scn.GetStrength(bilbo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InAFierceSkirmishAtADwellingAllFourAreTrueAndAHobbitInNoSkirmishGetsNothingForTheFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDwellingScenario();

		var letgo = scn.GetFreepsCard("letgo");
		var letgo2 = scn.GetFreepsCard("letgo2");
		var bilbo = scn.GetFreepsCard("bilbo");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(bilbo, merry);
		scn.MoveCardsToSupportArea(letgo, letgo2);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertInZone(Zone.SUPPORT, letgo, letgo2);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site6"));
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.DWELLING));
		// Nobody assigned in the first round: the Pursuer's skirmish is the fierce one.
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(bilbo, pursuer);
		scn.FreepsResolveSkirmish(bilbo);
		// Bilbo's own start-of-skirmish offer (an exertion paid for nothing, the discard pile empty) is declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(8, scn.GetResistance(bilbo));
		assertEquals(3, scn.GetStrength(bilbo));
		assertEquals(3, scn.GetStrength(merry));

		scn.FreepsUseCardAction(letgo);
		scn.FreepsChooseCard(bilbo);
		// Fierce skirmish +1, resistance 8 +1, at a dwelling +1, an unwounded Hobbit +1
		assertEquals(7, scn.GetStrength(bilbo));

		// Merry is in no skirmish, though a fierce one is going on: at the dwelling +1, an unwounded Hobbit +1;
		// resistance 6, nothing.
		scn.ShadowPass();
		scn.FreepsUseCardAction(letgo2);
		scn.FreepsChooseCard(merry);
		assertEquals(5, scn.GetStrength(merry));
		assertInDiscard(letgo, letgo2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var letgo = scn.GetFreepsCard("letgo");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCompanionsToTable(bilbo);
		scn.MoveCardsToSupportArea(letgo);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, letgo);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(letgo));
		assertInZone(Zone.SUPPORT, letgo);
	}

	@Test
	public void AnAllyIsAtNoDwellingWithTheFellowshipAtOneOnAShadowsPath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var letgo = scn.GetFreepsCard("letgo");
		var letgo2 = scn.GetFreepsCard("letgo2");
		var frodo = scn.GetRingBearer();
		var rosie = scn.GetFreepsCard("rosie");
		scn.MoveAlliesToTable(rosie);
		scn.MoveCardsToSupportArea(letgo, letgo2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site6"));
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("Buckland Homestead", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.DWELLING));
		scn.FreepsAssignToMinions(frodo, troop);
		scn.FreepsResolveSkirmish(frodo);
		int frodoStrength = scn.GetStrength(frodo);
		assertEquals(1, scn.GetStrength(rosie));

		// Frodo: at the dwelling, with the fellowship; resistance 7 or more; an unwounded Hobbit spotted: +3.
		scn.FreepsUseCardAction(letgo);
		scn.FreepsChooseCard(frodo);
		assertEquals(frodoStrength + 3, scn.GetStrength(frodo));

		// Rosie is at no site: of the four, an unwounded Hobbit spotted alone: +1.
		scn.ShadowPass();
		scn.FreepsUseCardAction(letgo2);
		scn.FreepsChooseCard(rosie);
		assertEquals(2, scn.GetStrength(rosie));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
