package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_033_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "4_33");
					// Saruman's Staff, Wizard's Device: plays on Saruman (strength +2, fierce, damage +1).  "Maneuver:
					// Make the first sentence of Saruman's game text not apply until the regroup phase."
					put("staff", "4_174");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, strength 8, vitality 1, no text
					put("brigand2", "4_10");    // A second Brigand, left out of every skirmish
					put("savage", "4_17");      // Dunlending Savage: a [dunland] Man, strength 11, vitality 1, no text
					// Easterling Axeman: a [raider] Man, strength 6, vitality 2; his text needs 2 burdens
					put("axeman", "4_224");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally, no companion; an ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 3 is Rivendell Waterfall: "While you can spot a ranger at Rivendell Waterfall, the move limit is +1 for
	// this turn."  Aragorn is a ranger, so a fellowship that reaches it on a regroup move may move once more.
	protected VirtualTableScenario GetThreeMoveScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "4_33");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, strength 8, vitality 1, no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a ranger, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: vitality 3; a skirmish ability used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The White Council beside Gandalf and two [elven] allies, whose first sentence makes Saruman's first sentence
	// not apply for as long as they are spotted, the regroup phase included.
	protected VirtualTableScenario GetWhiteCouncilScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "4_33");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, strength 8, vitality 1, no text
					// The White Council: [gandalf] condition in the support area.  "While you can spot Gandalf and 2
					// [elven] allies, the first sentence of Saruman's game text does not apply."  Its Regroup ability is
					// used only on purpose.
					put("council", "101_18");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a standing strength bonus only
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: [elven] ally; a Skirmish ability only
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: [elven] ally; a Fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Saruman, Rabble-rouser
		 * Unique: True
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Wizard
		 * Strength: 8
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: Saruman may not be assigned to a skirmish.
		 * 	Each time the fellowship moves, the Free Peoples player must exert a companion for each [dunland] Man you can spot.
		 * 	<b>Skirmish:</b> Exert Saruman to make a [dunland] Man strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("saruman");

		assertEquals("Saruman", card.getBlueprint().getTitle());
		assertEquals("Rabble-rouser", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SarumanCannotBeAssignedToASkirmishByEitherPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		// Laid after the move, so that his move trigger has nothing to answer.
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, brigand);

		scn.SkipToAssignments();
		// Neither player may assign him; the Brigand beside him may be assigned by either.
		assertFalse(scn.FreepsCanAssign(saruman));
		assertFalse(scn.ShadowCanAssign(saruman));
		assertTrue(scn.FreepsCanAssign(brigand));
		assertTrue(scn.ShadowCanAssign(brigand));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(brigand));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(saruman));

		scn.FreepsAssignToMinions(aragorn, brigand);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, brigand));
		assertFalse(scn.IsCharAssigned(saruman));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void MovingWithOneDunlandManMakesTheFreePeoplesPlayerExertOneCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(grimir);

		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, brigand, axeman);
		scn.SkipToMovementDecision();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));

		// The second move of the turn, with the minions of the first still on the table.  One [dunland] Man: Saruman
		// is a [dunland] Wizard and the Axeman a [raider] Man, and neither is counted.
		scn.FreepsChooseToMove();

		// The Free Peoples player chooses the companion: any companion, the Ring-bearer included; not an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir, saruman, brigand, axeman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(grimir));
		// No second exertion is asked.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingWithTwoDunlandMenMakesTwoExertionsAndOneCompanionMayTakeBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(grimir);

		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, brigand, savage, axeman);
		scn.SkipToMovementDecision();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsChooseToMove();

		// Two [dunland] Men are spotted, with no question: two exertions, each a choice of one companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// "For each": the same companion may take the second exertion too.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		scn.FreepsChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingWithNoDunlandManExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.FreepsPass();
		// Saruman is [dunland] and no Man, the Axeman a Man and not [dunland].
		scn.MoveMinionsToTable(saruman, axeman);
		scn.SkipToMovementDecision();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsChooseToMove();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingAtTheRegroupDecisionExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, brigand);
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The fellowship stays where it is: the turn ends with nobody exerted, and the other player's begins.
		scn.FreepsChooseToStay();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void EachMoveOfATurnMakesTheFreePeoplesPlayerExertAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreeMoveScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, brigand);
		scn.SkipToMovementDecision();
		assertEquals(0, scn.GetWoundsOn(gimli));

		// The move from site 2 to Rivendell Waterfall.
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// With a ranger at the Waterfall the fellowship may move a third time, and Saruman and the Brigand are there.
		assertEquals(3, scn.GetMoveLimit());
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		scn.FreepsChooseCard(gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsSarumanToMakeADunlandManStrengthPlus2UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");
		var brigand2 = scn.GetShadowCard("brigand2");
		var savage = scn.GetShadowCard("savage");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, brigand, brigand2, savage, axeman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, brigand }, new PhysicalCardImpl[] { gimli, savage });
		// The second Brigand and the Axeman are left out of every skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(8, scn.GetStrength(brigand2));
		assertEquals(11, scn.GetStrength(savage));
		assertTrue(scn.ShadowActionAvailable(saruman));
		scn.ShadowUseCardAction(saruman);

		// A [dunland] Man, in this skirmish, in another or in none; not a Man of another culture, not Saruman, not a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(brigand, savage, brigand2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axeman, saruman, aragorn, gimli));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(brigand);

		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(10, scn.GetStrength(brigand));
		assertEquals(8, scn.GetStrength(brigand2));
		assertEquals(11, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(axeman));

		// Still in force at the next decision of the same skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(brigand));
		scn.PassSkirmishActions();

		// 10 against 8: the Brigand wins and lives.  In the next skirmish the bonus is gone.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(brigand));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhenSarumanIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, brigand);
		// Vitality 4 with 3 wounds.
		scn.AddWoundsToChar(saruman, 3);
		assertEquals(3, scn.GetWoundsOn(saruman));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brigand);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.IsExhausted(saruman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(saruman));
		assertEquals(8, scn.GetStrength(brigand));
	}

	@Test
	public void WithHisFirstSentenceMadeNotToApplyBySarumansStaffHeIsAssignedAndHisSkirmishAbilityStillWorks() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, brigand);
		scn.AttachCardsTo(saruman, staff);
		assertAttachedTo(staff, saruman);
		scn.SkipToPhase(Phase.MANEUVER);

		// Until the Staff's ability is used the sentence is in force.
		assertFalse(scn.FreepsCanAssign(saruman));
		assertFalse(scn.ShadowCanAssign(saruman));
		scn.FreepsPass();
		scn.ShadowUseCardAction(staff);
		assertTrue(scn.FreepsCanAssign(saruman));
		assertTrue(scn.ShadowCanAssign(saruman));

		// The Free Peoples player is offered him beside the Brigand, and takes the Brigand; the Shadow player then
		// assigns Saruman.
		scn.SkipToAssignments();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(saruman));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(brigand));
		scn.FreepsAssignToMinions(aragorn, brigand);
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(saruman));
		scn.ShadowAssignToMinions(gimli, saruman);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, brigand));
		assertTrue(scn.IsCharAssignedAgainst(gimli, saruman));

		// His third sentence still applies: in the Brigand's skirmish he exerts to make the Brigand strength +2.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(8, scn.GetStrength(brigand));
		assertTrue(scn.ShadowActionAvailable(saruman));
		scn.ShadowUseCardAction(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(10, scn.GetStrength(brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithHisFirstSentenceMadeNotToApplyByTheWhiteCouncilHisMoveTriggerStillExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWhiteCouncilScenario();

		var frodo = scn.GetRingBearer();
		var council = scn.GetFreepsCard("council");
		var gandalf = scn.GetFreepsCard("gandalf");
		var golradir = scn.GetFreepsCard("golradir");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(golradir, celeborn);
		scn.MoveCardsToSupportArea(council);

		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, council);
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, brigand);

		// Gandalf and two [elven] allies are spotted: his first sentence does not apply, and he may be assigned.
		assertTrue(scn.FreepsCanAssign(saruman));

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsCanAssign(saruman));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// His second sentence still applies: the move makes the Free Peoples player exert a companion.
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(golradir, celeborn));
		scn.FreepsChooseCard(gandalf);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
