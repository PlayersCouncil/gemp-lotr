package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_178_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("unferth", "4_178");
					// Rohirrim Traitor: another [isengard] Man, strength 6, vitality 2; +2 only while a companion bears an [isengard] condition
					put("traitor", "6_72");
					put("traitor2", "6_72");
					put("savage", "1_151");     // Uruk Savage: an [isengard] minion who is no Man; damage +1 its whole text
					put("brigand", "4_10");     // Dunlending Brigand: a Man of another culture ([dunland]); no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability used only on purpose
					put("heir", "4_109");       // Aragorn, Heir of Elendil: defender +1 is his whole text
					put("boromir", "1_97");     // Boromir, Son of Denethor: vitality 3; a skirmish ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit, vitality 4; +2 only while he bears a weapon
					put("sam", "1_311");        // Sam, Son of Hamfast: vitality 4; a fellowship ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally who may skirmish by his own maneuver ability

					// The End Comes: "Assignment: Spot 5 [moria] minions to make the Free Peoples player assign the
					// Ring-bearer to a skirmish"; the Troop and four Runners are the five (cheated in, so no Runner's (2))
					put("endcomes", "1_169");
					put("troop", "1_177");      // Goblin Patrol Troop: no text
					put("runner1", "1_178");    // Goblin Runner: its only text answers its own play
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetAssignmentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("unferth", "4_178");
					put("savage", "1_151");     // Uruk Savage: an [isengard] minion who is no Man; Damage +1 its whole text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Glóin: vitality 3; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UnferthStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Unferth, Gríma's Bodyguard
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: Each time the Free Peoples player assigns a companion or ally to an [isengard] Man, that companion or ally must exert.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("unferth");

		assertEquals("Unferth", card.getBlueprint().getTitle());
		assertEquals("Gríma's Bodyguard", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ACompanionTheFreePeoplesPlayerAssignsToAnIsengardManMustExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAssignmentScenario();

		var unferth = scn.GetShadowCard("unferth");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(unferth, savage);
		scn.SkipToAssignments();

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// Aragorn to Unferth, an [isengard] Man; Gimli to the Savage, an [isengard] Uruk-hai.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, unferth}, new PhysicalCardImpl[]{gimli, savage});
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(unferth));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, unferth));
		assertTrue(scn.IsCharAssignedAgainst(gimli, savage));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void EachCompanionAssignedToAnIsengardManExertsAndAnExhaustedOneTakesNoWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unferth = scn.GetShadowCard("unferth");
		var traitor = scn.GetShadowCard("traitor");
		var traitor2 = scn.GetShadowCard("traitor2");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, merry, sam, boromir);

		scn.StartGame();
		// Vitality 4 with 3 wounds: Sam is exhausted.
		scn.AddWoundsToChar(sam, 3);
		scn.MoveMinionsToTable(unferth, traitor, traitor2, savage, brigand);
		assertEquals(3, scn.GetWoundsOn(sam));
		scn.SkipToAssignments();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, unferth},
				new PhysicalCardImpl[]{merry, traitor},
				new PhysicalCardImpl[]{sam, traitor2},
				new PhysicalCardImpl[]{boromir, savage},
				new PhysicalCardImpl[]{frodo, brigand});

		// Unferth himself and another [isengard] Man: each companion assigned to one exerts.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(merry));
		// Sam cannot exert: no wound is placed on him, and he is still in play.
		assertEquals(3, scn.GetWoundsOn(sam));
		assertInZone(Zone.FREE_CHARACTERS, sam);
		// An [isengard] minion who is no Man, and a Man of another culture: no exertion.
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AnAllyTheFreePeoplesPlayerAssignsToUnferthExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unferth = scn.GetShadowCard("unferth");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		// Thrarin's vitality 2 is spent by his own exertion; +1 by hand leaves him one to exert again.
		scn.ApplyAdHocModifier(new VitalityModifier(null, thrarin, null, 1));
		scn.MoveMinionsToTable(unferth);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(thrarin, unferth);
		assertTrue(scn.IsCharAssignedAgainst(thrarin, unferth));
		assertEquals(2, scn.GetWoundsOn(thrarin));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void TheShadowPlayersAssignmentOfACompanionToUnferthMakesNobodyExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unferth = scn.GetShadowCard("unferth");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(unferth, savage);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.FreepsAssignToMinions(sam, savage);
		assertEquals(0, scn.GetWoundsOn(sam));

		// Unferth is left over: the Shadow player assigns him to Aragorn.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		scn.ShadowAssignToMinions(aragorn, unferth);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, unferth));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void MadeByAShadowCardToAssignFrodoTheFreePeoplesPlayerWhoPicksUnferthMakesFrodoExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unferth = scn.GetShadowCard("unferth");
		var endcomes = scn.GetShadowCard("endcomes");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(endcomes);
		assertInHand(endcomes);

		scn.StartGame();
		scn.MoveMinionsToTable(unferth, troop, scn.GetShadowCard("runner1"), scn.GetShadowCard("runner2"),
				scn.GetShadowCard("runner3"), scn.GetShadowCard("runner4"));
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(endcomes));
		scn.ShadowPlayCard(endcomes);

		// The event is the Shadow player's; the Free Peoples player picks the minion, so the assignment is his (Q-0171).
		assertTrue(scn.FreepsHasCardChoiceAvailable(unferth, troop));
		scn.FreepsChooseCard(unferth);

		assertTrue(scn.IsCharAssignedAgainst(frodo, unferth));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ADefenderAssignedToUnferthAndAnotherIsengardManInOneAnswerExertsTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unferth = scn.GetShadowCard("unferth");
		var traitor = scn.GetShadowCard("traitor");
		var heir = scn.GetFreepsCard("heir");
		scn.MoveCompanionsToTable(heir);

		scn.StartGame();
		scn.MoveMinionsToTable(unferth, traitor);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetWoundsOn(heir));
		scn.FreepsAssignToMinions(heir, unferth, traitor);

		assertTrue(scn.IsCharAssignedAgainst(heir, unferth));
		assertTrue(scn.IsCharAssignedAgainst(heir, traitor));
		// Assigned to each [isengard] Man: one exertion for each (Q-0323: B; Q-0330).
		assertEquals(2, scn.GetWoundsOn(heir));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
