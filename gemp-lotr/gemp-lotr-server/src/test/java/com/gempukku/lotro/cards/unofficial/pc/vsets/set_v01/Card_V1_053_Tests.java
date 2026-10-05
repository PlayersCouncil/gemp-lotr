package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_053_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "101_53");
					// Merry, Friend to Sam: [shire] Hobbit, Frodo signet, strength 3, vitality 4.  His skirmish ability
					// needs him unassigned and is never used here.
					put("merry", "1_302");
					// Sam, Son of Hamfast: [shire] Hobbit, strength 3, vitality 4, with the Aragorn signet
					put("sam", "1_311");
					// Gandalf, Friend of the Shirefolk: [gandalf] Wizard with the Frodo signet
					put("gandalf", "1_72");
					put("guard", "1_7");            // Dwarf Guard: [dwarven] Dwarf, strength 4, vitality 2, no signet
					// Bounder: [shire] Hobbit ally, strength 2, vitality 2, whose home is site 2.  His own skirmish
					// ability is never used here.
					put("bounder", "1_286");

					put("brigand1", "4_10");        // Dunlending Brigand: strength 8, vitality 1, no text
					put("brigand2", "4_10");
					put("savage", "4_17");          // Dunlending Savage: strength 11, vitality 1, no text
					put("party", "1_158");          // Uruk-hai Raiding Party: strength 9, vitality 3, damage +1
					put("runner", "1_178");         // Goblin Runner: strength 5, vitality 1
					// Shelob, Last Child of Ungoliant: strength 8, vitality 8, fierce; damage +1 in a fierce skirmish
					put("shelob", "8_26");
					// Ulaire Lemenya, Lieutenant of Morgul: strength 9; its text is about bearers of [wraith] conditions
					put("lemenya", "1_232");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: Frodo signet, strength 3
				VirtualTableScenario.RulingRing     // strength +1 to its bearer
		);
	}

	protected VirtualTableScenario GetFrodoWithTheAragornSignetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "101_53");
					// Merry, Friend to Sam: [shire] Hobbit, Frodo signet, strength 3, vitality 4.  His skirmish ability
					// needs him unassigned and is never used here.
					put("merry", "1_302");
					// Sam, Son of Hamfast: [shire] Hobbit, strength 3, vitality 4, with the Aragorn signet
					put("sam", "1_311");
					// Gandalf, Friend of the Shirefolk: [gandalf] Wizard with the Frodo signet
					put("gandalf", "1_72");
					put("guard", "1_7");            // Dwarf Guard: [dwarven] Dwarf, strength 4, vitality 2, no signet
					// Bounder: [shire] Hobbit ally, strength 2, vitality 2, whose home is site 2.  His own skirmish
					// ability is never used here.
					put("bounder", "1_286");

					put("brigand1", "4_10");        // Dunlending Brigand: strength 8, vitality 1, no text
					put("brigand2", "4_10");
					put("savage", "4_17");          // Dunlending Savage: strength 11, vitality 1, no text
					put("party", "1_158");          // Uruk-hai Raiding Party: strength 9, vitality 3, damage +1
					put("runner", "1_178");         // Goblin Runner: strength 5, vitality 1
					// Shelob, Last Child of Ungoliant: strength 8, vitality 8, fierce; damage +1 in a fierce skirmish
					put("shelob", "8_26");
					// Ulaire Lemenya, Lieutenant of Morgul: strength 9; its text is about bearers of [wraith] conditions
					put("lemenya", "1_232");
				}},
				VirtualTableScenario.FellowshipSites,
				// Frodo, Reluctant Adventurer: a Frodo with the Aragorn signet; his text is about what is played on him
				"2_102",
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PippinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Pippin, Of Tuckborough
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: Skirmish: Exert Pippin to prevent a skirmishing [shire] companion from being overwhelmed
		 * 	unless their strength is tripled (and make them strength +1 if you can spot 3 companions with the
		 * 	Frodo signet).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pippin");

		assertEquals("Pippin", card.getBlueprint().getTitle());
		assertEquals("Of Tuckborough", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void PlaysFromHandAsACompanionForOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCardsToHand(pippin);

		scn.StartGame();

		assertInHand(pippin);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(pippin));

		scn.FreepsPlayCard(pippin);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsPippinSoThatAShireCompanionFacingDoubleHisStrengthIsWoundedAndNotOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrodoWithTheAragornSignetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");
		scn.MoveCompanionsToTable(pippin, merry, sam, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand1, brigand2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, brigand1}, new PhysicalCardImpl[]{sam, brigand2});

		scn.FreepsResolveSkirmish(merry);
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(brigand1));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.FreepsActionAvailable(pippin));

		scn.FreepsUseCardAction(pippin);
		assertEquals(1, scn.GetWoundsOn(pippin));
		// Only Pippin and Merry have the Frodo signet.  This Frodo and Sam have Aragorn's and the Dwarf Guard
		// has none, so nobody is made stronger.
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassCurrentPhaseActions();
		// 8 is more than double 3 and less than triple: Merry loses and takes his wound.
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(1, scn.GetWoundsOn(merry));

		// Sam at the same strength against the same minion, with Pippin doing nothing, is overwhelmed.
		scn.FreepsResolveSkirmish(sam);
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(brigand2));
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DEAD, sam);
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionFacingTripleHisStrengthIsOverwhelmedHoweverOftenPippinExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrodoWithTheAragornSignetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(pippin, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(merry, party);

		scn.FreepsResolveSkirmish(merry);
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(9, scn.GetStrength(party));

		scn.FreepsUseCardAction(pippin);
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(3, scn.GetStrength(merry));

		// A second use asks for no more than the first did: tripled is still what it takes.
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsUseCardAction(pippin);
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(3, scn.GetStrength(merry));

		scn.PassCurrentPhaseActions();
		// 9 is exactly triple 3.
		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithThreeCompanionsWhoHaveTheFrodoSignetTheProtectedCompanionIsAlsoStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrodoWithTheAragornSignetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		var savage = scn.GetShadowCard("savage");
		// Exactly three with the Frodo signet: Pippin, Merry and Gandalf.
		scn.MoveCompanionsToTable(pippin, merry, gandalf, sam, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(merry, savage);

		scn.FreepsResolveSkirmish(merry);
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(11, scn.GetStrength(savage));
		var gandalfStrength = scn.GetStrength(gandalf);

		scn.FreepsUseCardAction(pippin);
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(4, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(gandalfStrength, scn.GetStrength(gandalf));

		scn.PassCurrentPhaseActions();
		// 11 is triple 3 and not triple 4: without the +1 Merry would have been overwhelmed.
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		// The skirmish is over, and so is the +1.
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenPippinIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrodoWithTheAragornSignetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var brigand1 = scn.GetShadowCard("brigand1");
		scn.MoveCompanionsToTable(pippin, merry);
		// Vitality 4 with 3 wounds: he cannot exert.
		scn.AddWoundsToChar(pippin, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand1);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(merry, brigand1);

		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishing(merry));
		assertEquals(3, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pippin));
	}

	@Test
	public void ProtectionAndStrengthAreGoneInTheFierceSkirmishThatFollows() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var shelob = scn.GetShadowCard("shelob");
		// Three with the Frodo signet: Frodo, Pippin and Merry.
		scn.MoveCompanionsToTable(pippin, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(merry, shelob);

		scn.FreepsResolveSkirmish(merry);
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(shelob));
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));

		scn.FreepsUseCardAction(pippin);
		assertEquals(4, scn.GetStrength(merry));
		assertEquals(3, scn.GetOverwhelmMultiplier(merry));
		// The Ring-bearer has the signet that is counted and gets nothing himself: 3 and 1 for the Ring.
		assertEquals(4, scn.GetStrength(frodo));

		scn.PassCurrentPhaseActions();
		// 8 is double 4: the protection is still there when the skirmish resolves.
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(1, scn.GetWoundsOn(merry));

		// Shelob is fierce.  Merry meets her again with nothing done for him this time.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(merry, shelob);

		scn.FreepsResolveSkirmish(merry);
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(shelob));
		assertEquals(1, scn.GetWoundsOn(pippin));

		scn.PassCurrentPhaseActions();
		// 8 is more than double 3, and he is overwhelmed.
		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DoesNothingForAHobbitOfAnotherCultureAShireAllyOrAShireCompanionWhoseSkirmishHasNotBegun() throws DecisionResultInvalidException, CardNotFoundException {
		// Frodo, Son of Drogo is the Ring-bearer here: with Pippin and Merry, three have the Frodo signet, so a
		// strength +1 is there to be given if anybody is protected.
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var bounder = scn.GetFreepsCard("bounder");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(pippin, merry, guard);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		// The Dwarf Guard becomes a Hobbit who is still a [dwarven] card.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, guard, null, Race.HOBBIT));
		scn.MoveMinionsToTable(brigand1, brigand2, runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// The fellowship has moved to site 2, the Bounder's home, where he may be assigned.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, brigand1},
				new PhysicalCardImpl[]{bounder, runner},
				new PhysicalCardImpl[]{merry, brigand2});

		// The Guard's skirmish: 4 against 8.  Merry and the Bounder are assigned and not yet skirmishing.
		scn.FreepsResolveSkirmish(guard);
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));
		assertTrue(scn.FreepsActionAvailable(pippin));
		scn.FreepsUseCardAction(pippin);
		assertEquals(1, scn.GetWoundsOn(pippin));
		// Nobody is there to be chosen, and the Shadow player has the next action.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		// Nobody is protected, so nobody is made stronger either; and Merry, who is only waiting, is not protected.
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DEAD, guard);

		// The Bounder's skirmish: 2 against 5, doubled and not tripled.
		scn.FreepsResolveSkirmish(bounder);
		assertEquals(2, scn.GetStrength(bounder));
		assertEquals(5, scn.GetStrength(runner));
		scn.FreepsUseCardAction(pippin);
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(2, scn.GetStrength(bounder));
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DEAD, bounder);

		// Merry's own skirmish, 3 against 8: neither earlier use was for him.
		scn.FreepsResolveSkirmish(merry);
		assertEquals(3, scn.GetStrength(merry));
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ProtectsPippinHimselfAndACompanionOfAnotherRaceWhoIsAShireCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrodoWithTheAragornSignetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");
		scn.MoveCompanionsToTable(pippin, guard);

		scn.StartGame();
		// The Dwarf Guard becomes a [shire] card who is still a Dwarf.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, guard, null, Culture.SHIRE));
		scn.MoveMinionsToTable(brigand1, brigand2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, brigand1}, new PhysicalCardImpl[]{pippin, brigand2});

		// 4 against 8.
		scn.FreepsResolveSkirmish(guard);
		assertEquals(4, scn.GetStrength(guard));
		scn.FreepsUseCardAction(pippin);
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(4, scn.GetStrength(guard));
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertEquals(1, scn.GetWoundsOn(guard));

		// 3 against 8, and Pippin is the one skirmishing.
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.FreepsActionAvailable(pippin));
		scn.FreepsUseCardAction(pippin);
		assertEquals(2, scn.GetWoundsOn(pippin));
		scn.PassCurrentPhaseActions();
		// A wound for losing on top of his two exertions.
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertEquals(3, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ProtectsTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(pippin);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(frodo, lemenya);

		scn.FreepsResolveSkirmish(frodo);
		// 3 and 1 for the Ring, against 9.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(2, scn.GetOverwhelmMultiplier(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.FreepsActionAvailable(pippin));

		scn.FreepsUseCardAction(pippin);
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(3, scn.GetOverwhelmMultiplier(frodo));
		// Frodo and Pippin are the only two with the Frodo signet.
		assertEquals(4, scn.GetStrength(frodo));

		scn.PassCurrentPhaseActions();
		// Frodo has lost and is about to take a wound, which the Ring offers to turn into a burden.
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInZone(Zone.FREE_CHARACTERS, frodo);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
