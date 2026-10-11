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

public class Card_04_141_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mountains", "4_141");
					put("mountains2", "4_141");
					put("mountains3", "4_141");

					// [isengard] minions of two races, vitality 3 each.
					put("raiders", "1_158");    // Uruk-hai Raiding Party: Uruk-hai, strength 9; Damage +1 is its whole text
					put("raiders2", "1_158");
					put("servant", "3_58");     // Isengard Servant: Orc, strength 7; a regroup ability that is never used
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, vitality 3, no text
					// Fierce in Despair: [raider] Regroup event, cost 7, "Spot 2 [raider] Men to discard a companion
					// (except the Ring-bearer)".  The Axemen are the Men; their own text needs 2 burdens.
					put("despair", "7_148");
					put("axeman", "4_224");
					put("axeman2", "4_224");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor], strength 8, vitality 4
					put("defender", "17_93");   // Aragorn, Defender of Rohan: the [rohan] Aragorn, strength 8, vitality 4
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: strength 7, vitality 4; a fellowship ability
					put("theoden", "4_292");    // Theoden, Son of Thengel: [rohan], strength 6, vitality 2; a regroup ability
					// Companions of the same three cultures who are none of the three, strength 6 each.
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man, vitality 3
					put("avenger", "6_27");     // Ent Avenger: [gandalf] Ent, vitality 4; stronger only beside 3 Ents
					put("guard", "5_83");       // Household Guard: [rohan] Man, vitality 3
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, strength 2, vitality 2, home site 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BeyondDarkMountainsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Beyond Dark Mountains
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: 
		 * Game Text: <b>Response:</b> If a companion or ally is killed, exert an [isengard] minion to add a burden (or 2 burdens if Aragorn, Gandalf, or Théoden is killed).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mountains");

		assertEquals("Beyond Dark Mountains", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ACompanionKilledLetsTheShadowPlayerExertAnIsengardMinionToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		var mountains = scn.GetShadowCard("mountains");
		var raiders = scn.GetShadowCard("raiders");
		var servant = scn.GetShadowCard("servant");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(mountains);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, servant, troop);
		// 2 wounds of his 3 vitality: the next wound kills him.
		scn.AddWoundsToChar(knight, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, raiders);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		// 2 companions + 2 for Ettenmoors
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.FREE_CHARACTERS, knight);

		// 9 against 6: the Knight loses, and the wound kills him.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, knight);
		assertTrue(scn.ShadowDecisionAvailable("Optional responses"));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mountains));

		scn.ShadowPlayCard(mountains);
		// "An [isengard] minion": the Uruk-hai and the Orc of that culture, and not the [moria] Orc.
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiders, servant));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(servant));

		scn.ShadowChooseCard(servant);
		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(troop));
		// A [gondor] Man who is not Aragorn: one burden.
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.DISCARD, mountains);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyKilledAtHisHomeSiteIsAnsweredToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(bounder);

		var mountains = scn.GetShadowCard("mountains");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(mountains);

		scn.StartGame();
		scn.MoveMinionsToTable(servant);

		// Site 2 is the Bounder's home, where he may be assigned.
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(bounder, servant);
		scn.FreepsResolveSkirmish(bounder);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(servant));

		// 7 against 2: the Bounder is overwhelmed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, bounder);
		assertTrue(scn.ShadowDecisionAvailable("Optional responses"));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mountains));

		// The Servant is the only [isengard] minion: nobody is asked who exerts.
		scn.ShadowPlayCard(mountains);
		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.DISCARD, mountains);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionOfGandalfsCultureOrOfRohanWhoIsNoneOfTheThreeAddsOneBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var avenger = scn.GetFreepsCard("avenger");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(avenger, guard);

		var mountains = scn.GetShadowCard("mountains");
		var mountains2 = scn.GetShadowCard("mountains2");
		var raiders = scn.GetShadowCard("raiders");
		var raiders2 = scn.GetShadowCard("raiders2");
		scn.MoveCardsToHand(mountains, mountains2);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, raiders2);
		// Each is one wound from death.
		scn.AddWoundsToChar(avenger, 3);
		scn.AddWoundsToChar(guard, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{avenger, raiders},
				new PhysicalCardImpl[]{guard, raiders2});

		// 9 against 6: the Ent Avenger, a [gandalf] companion who is not Gandalf, is killed.
		scn.FreepsResolveSkirmish(avenger);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, avenger);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mountains));
		scn.ShadowPlayCard(mountains);
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiders, raiders2));
		scn.ShadowChooseCard(raiders);
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertEquals(1, scn.GetBurdens());

		// The copy still in hand may answer the same death (the rules let a player respond more than once); it
		// is kept for the next.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mountains2));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetBurdens());

		// 9 against 6: the Household Guard, a [rohan] Man who is not Theoden, is killed.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mountains2));
		scn.ShadowPlayCard(mountains2);
		scn.ShadowChooseCard(raiders2);
		assertEquals(1, scn.GetWoundsOn(raiders2));
		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.DISCARD, mountains, mountains2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AragornGandalfOrTheodenKilledAddsTwoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var theoden = scn.GetFreepsCard("theoden");
		scn.MoveCompanionsToTable(aragorn, gandalf, theoden);

		var mountains = scn.GetShadowCard("mountains");
		var mountains2 = scn.GetShadowCard("mountains2");
		var mountains3 = scn.GetShadowCard("mountains3");
		var raiders = scn.GetShadowCard("raiders");
		var raiders2 = scn.GetShadowCard("raiders2");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(mountains, mountains2, mountains3);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, raiders2, servant);
		// Each is one wound from death.
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(gandalf, 3);
		scn.AddWoundsToChar(theoden, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, raiders},
				new PhysicalCardImpl[]{gandalf, raiders2},
				new PhysicalCardImpl[]{theoden, servant});
		assertEquals(0, scn.GetBurdens());

		// 9 against 8: Aragorn is killed.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mountains));
		scn.ShadowPlayCard(mountains);
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiders, raiders2, servant));
		scn.ShadowChooseCard(raiders);
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertEquals(2, scn.GetBurdens());
		// The copies still in hand are offered for the same death, and kept.
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(2, scn.GetBurdens());

		// 9 against 7 (Gandalf's printed strength): Gandalf is killed.
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, gandalf);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mountains2));
		scn.ShadowPlayCard(mountains2);
		scn.ShadowChooseCard(raiders2);
		assertEquals(1, scn.GetWoundsOn(raiders2));
		assertEquals(4, scn.GetBurdens());
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(4, scn.GetBurdens());

		// 7 against 6: Theoden is killed.
		scn.FreepsResolveSkirmish(theoden);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, theoden);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mountains3));
		scn.ShadowPlayCard(mountains3);
		scn.ShadowChooseCard(servant);
		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(6, scn.GetBurdens());
		assertInZone(Zone.DISCARD, mountains, mountains2, mountains3);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AragornOfRohanKilledAddsTwoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defender = scn.GetFreepsCard("defender");
		scn.MoveCompanionsToTable(defender);

		var mountains = scn.GetShadowCard("mountains");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToHand(mountains);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		// One wound from death.
		scn.AddWoundsToChar(defender, 3);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(defender, raiders);
		scn.FreepsResolveSkirmish(defender);
		assertEquals(0, scn.GetBurdens());

		// 9 against 8: Aragorn, Defender of Rohan, is killed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, defender);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mountains));

		scn.ShadowPlayCard(mountains);
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheResponseAddsNoBurdenAndExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);

		var mountains = scn.GetShadowCard("mountains");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToHand(mountains);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(raiders);
		// One wound from death.
		scn.AddWoundsToChar(gandalf, 3);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, raiders);
		scn.FreepsResolveSkirmish(gandalf);

		// 9 against 7: Gandalf is killed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, gandalf);
		assertTrue(scn.ShadowDecisionAvailable("Optional responses"));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mountains));

		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertInZone(Zone.HAND, mountains);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheOnlyIsengardMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		var mountains = scn.GetShadowCard("mountains");
		var raiders = scn.GetShadowCard("raiders");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(mountains);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, troop);
		scn.AddWoundsToChar(knight, 2);
		// 2 wounds of 3 vitality: the Raiding Party cannot exert.  The [moria] Troop could.
		scn.AddWoundsToChar(raiders, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, raiders);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);

		// 9 against 6: the Knight is killed.  Where an [isengard] minion can exert, the Shadow player is asked
		// here; now he is asked nothing and the game goes on to the regroup phase.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, knight);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.HAND, mountains);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(troop));
	}

	@Test
	public void IsNotOfferedWhenACompanionIsWoundedAndLivesNorWhenAMinionIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(knight, aragorn);

		var mountains = scn.GetShadowCard("mountains");
		var raiders = scn.GetShadowCard("raiders");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(mountains);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, servant);
		// The Servant is one wound from death.
		scn.AddWoundsToChar(servant, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{knight, raiders},
				new PhysicalCardImpl[]{aragorn, servant});

		// 9 against 6: the unwounded Knight loses, takes 2 wounds (1 + 1 for damage +1) of his 3 vitality, and lives.
		scn.FreepsResolveSkirmish(knight);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(knight));
		assertInZone(Zone.FREE_CHARACTERS, knight);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// 8 against 7: the Servant, an [isengard] minion, is killed, with the Raiding Party able to exert.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, servant);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.HAND, mountains);
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void IsNotOfferedWhenACompanionIsDiscardedFromPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		var mountains = scn.GetShadowCard("mountains");
		var despair = scn.GetShadowCard("despair");
		var raiders = scn.GetShadowCard("raiders");
		var axeman = scn.GetShadowCard("axeman");
		var axeman2 = scn.GetShadowCard("axeman2");
		scn.MoveCardsToHand(mountains, despair);

		scn.StartGame();
		// 3 set + 2 companions + 2 for Ettenmoors: the 7 that Fierce in Despair costs
		scn.SetTwilight(3);
		scn.FreepsPass();
		scn.MoveMinionsToTable(raiders, axeman, axeman2);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(despair));

		// The Knight is the only companion but the Ring-bearer: he is discarded, not killed.
		scn.ShadowPlayCard(despair);
		assertInZone(Zone.DISCARD, knight);
		assertEquals(0, scn.GetTwilight());

		// The Raiding Party could exert, and the event costs nothing: the Shadow player is asked nothing.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.HAND, mountains);
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetBurdens());
	}
}
