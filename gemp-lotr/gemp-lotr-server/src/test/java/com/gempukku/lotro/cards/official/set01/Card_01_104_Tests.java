package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_104_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trails", "1_104");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man ranger, strength 8, vitality 4; his own maneuver ability is not used here
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf ranger, strength 6, vitality 3; +3 only against a Nazgûl
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man who is no ranger, vitality 3; "Knight." is his whole text

					// At site 2, where the maneuver phase of turn 1 is played:
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1, site 4: roaming; cheated in, so its text does nothing
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, site 5: roaming; damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, site 4: roaming; no text at all
					put("nelya", "2_84");       // Úlairë Nelya: Nazgûl, strength 10, site 2: not roaming; fierce and damage +1 only at 2 burdens or 2 wounds on the Ring-bearer

					put("lemenya", "1_232");    // Úlairë Lemenya: Nazgûl, strength 9, site 3: roaming; her text is about [wraith] conditions on companions
					put("nertea", "1_234");     // Úlairë Nertëa: Nazgûl, strength 9, site 3: roaming; her trigger on being played needs 5 companions
					put("fell", "15_182");      // A Shadow Fell Over Them: "Assignment: Spot a Nazgûl and remove (4) to play a Nazgûl from your discard pile."
					put("wrath", "4_261");      // Wrath of Harad: [raider] support condition; while a [raider] Man is spotted, each Free Peoples event that spots or exerts a ranger gains the cost "wound a companion"
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, strength 6, vitality 2, site 4; strength +3 and fierce only at 2 burdens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EregionsTrailsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Eregion's Trails
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert a ranger to make each roaming minion strength -3 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trails");

		assertEquals("Eregion's Trails", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsARangerToMakeEachRoamingMinionStrengthMinus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trails = scn.GetFreepsCard("trails");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, arwen, knight);
		scn.MoveCardsToHand(trails);

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage, nelya);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.HasKeyword(runner, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(trails));

		scn.FreepsPlayCard(trails);
		// A ranger of either culture and race may pay; the [gondor] Man who is no ranger may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(knight));

		// The roaming [moria] Orc and [isengard] Uruk-hai are 3 weaker; Nelya, at her own site number, is not roaming.
		assertEquals(2, scn.GetStrength(runner));
		assertEquals(2, scn.GetStrength(savage));
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(knight));
		assertInDiscard(trails);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyRangerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trails = scn.GetFreepsCard("trails");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveCardsToHand(trails);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		// Vitality 4 with 3 wounds: Aragorn cannot exert.  The Knight beside him, no ranger, could.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.HasKeyword(runner, Keyword.ROAMING));
		assertInHand(trails);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(trails));
	}

	@Test
	public void ArwenPaysAndTheMinionsStayWeakerThroughTheSkirmishesUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trails = scn.GetFreepsCard("trails");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.MoveCardsToHand(trails);

		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		scn.FreepsPlayCard(trails);
		scn.FreepsChooseCard(arwen);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetStrength(runner));
		assertEquals(10, scn.GetStrength(troop));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{arwen, troop});

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(runner);

		// In the second skirmish of the turn the Troop is still at 13 - 3, and beats Arwen without killing her.
		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(troop));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(arwen));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(13, scn.GetStrength(troop));
	}

	@Test
	public void ARoamingMinionPlayedAfterwardsKeepsItsStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trails = scn.GetFreepsCard("trails");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(trails);

		var lemenya = scn.GetShadowCard("lemenya");
		var nertea = scn.GetShadowCard("nertea");
		var fell = scn.GetShadowCard("fell");
		scn.MoveCardsToSupportArea(fell);
		scn.MoveCardsToDiscard(nertea);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInDiscard(nertea);
		assertTrue(scn.HasKeyword(lemenya, Keyword.ROAMING));
		assertEquals(9, scn.GetStrength(lemenya));
		scn.FreepsPlayCard(trails);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(6, scn.GetStrength(lemenya));

		// "Assignment: Spot a Nazgûl and remove (4) to play a Nazgûl from your discard pile."
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertInDiscard(nertea);
		scn.ShadowUseCardAction(fell);

		// Nertëa is roaming as well, and was not in play when the event was played.
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertTrue(scn.HasKeyword(nertea, Keyword.ROAMING));
		assertEquals(9, scn.GetStrength(nertea));
		assertEquals(6, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void UnderWrathOfHaradItAlsoCostsAWoundOnACompanionOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trails = scn.GetFreepsCard("trails");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveCardsToHand(trails);

		var wrath = scn.GetShadowCard("wrath");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(wrath);
		assertInZone(Zone.SUPPORT, wrath);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertInZone(Zone.SUPPORT, wrath);
		assertTrue(scn.HasKeyword(axeman, Keyword.ROAMING));
		assertEquals(6, scn.GetStrength(axeman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(trails));

		scn.FreepsPlayCard(trails);
		// The event exerts a ranger (Aragorn, the only one, unasked), so Wrath of Harad, with a [raider] Man spotted,
		// adds "wound a companion" to its cost: a cost of the Free Peoples player's event, which he pays and so
		// chooses (CR 4.2 "cost").
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, knight));
		scn.FreepsChooseCard(knight);

		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// The Axeman, site 4, roams at site 2.
		assertEquals(3, scn.GetStrength(axeman));
		assertInDiscard(trails);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
