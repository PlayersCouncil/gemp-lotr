package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.CantTakeWoundsModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "102_6");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor], strength 8, vitality 4; a maneuver ability never used here
					put("rohirrim", "17_93");   // Aragorn, Defender of Rohan: [rohan], strength 8, vitality 4; his text needs the Ring-bearer assigned to a skirmish
					put("boromir", "1_96");     // Boromir, Lord of Gondor: a [gondor] Man like Aragorn, strength 7, vitality 3; not overwhelmed unless his strength is tripled

					put("marksman1", "1_176");  // Goblin Marksman: Archer is its whole text
					put("marksman2", "1_176");
					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, vitality 3; Damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArwenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Arwen, Lady of Rivendell
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Site Number: 3F
		 * Game Text: Aragorn is strength +1. While Arwen is exhausted, Aragorn is defender +1.
		 * 	Each time Aragorn is about to take a wound, you may wound Arwen to prevent that wound.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arwen");

		assertEquals("Arwen", card.getBlueprint().getTitle());
		assertEquals("Lady of Rivendell", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void PlayedForTwoToTheSupportAreaAndAragornIsStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToHand(arwen);
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertInZone(Zone.HAND, arwen);
		assertTrue(scn.FreepsPlayAvailable(arwen));

		scn.FreepsPlayCard(arwen);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, arwen);
		assertEquals(9, scn.GetStrength(aragorn));
		// Another [gondor] Man beside him
		assertEquals(7, scn.GetStrength(boromir));
		// An unwounded Arwen gives him nothing else.
		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileArwenIsExhaustedAragornIsDefenderPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(arwen);

		scn.StartGame();
		// Boromir is exhausted from the start: it is Arwen's exhaustion that counts, not anyone's.
		scn.AddWoundsToChar(boromir, 2);

		assertEquals(1, scn.GetVitality(boromir));
		assertEquals(3, scn.GetVitality(arwen));
		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));

		scn.AddWoundsToChar(arwen, 1);
		// Wounded, with 2 vitality left: not exhausted
		assertEquals(2, scn.GetVitality(arwen));
		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));

		scn.AddWoundsToChar(arwen, 1);
		assertEquals(1, scn.GetVitality(arwen));
		assertTrue(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(boromir, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachWoundFromALostSkirmishMayBePreventedAndTheSecondLeavesArwenExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(arwen);

		var party = scn.GetShadowCard("party");

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, party);

		// Aragorn's 8 + 1 against the Raiding Party's 9: a tie is the Shadow's (CR 4.2 "skirmish phase"), and
		// damage +1 makes the loss two wounds.
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(party));
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));

		// The second wound of the same loss is offered in its turn.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(arwen));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(arwen));
		// Vitality 3 with 2 wounds: Arwen is exhausted.
		assertTrue(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertInZone(Zone.SUPPORT, arwen);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedForAnotherCompanionsWoundAndDecliningLetsAragornTakeHis() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(arwen);

		scn.StartGame();
		scn.MoveMinionsToTable("marksman1", "marksman2");
		scn.SkipToArcheryWounds();

		assertEquals(2, scn.GetShadowArcheryTotal());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir));
		scn.FreepsChooseCard(boromir);

		// Boromir's wound is his own: nothing is offered, and the second archery wound is waiting to be placed.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir));
		scn.FreepsChooseCard(aragorn);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnAragornOfAnotherCultureIsStrengthenedProtectedAndMadeDefenderToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var rohirrim = scn.GetFreepsCard("rohirrim");
		scn.MoveCompanionsToTable(rohirrim);

		scn.StartGame();

		assertEquals(8, scn.GetStrength(rohirrim));
		scn.MoveAlliesToTable(arwen);
		scn.AddWoundsToChar(arwen, 1);
		assertEquals(9, scn.GetStrength(rohirrim));
		assertFalse(scn.HasKeyword(rohirrim, Keyword.DEFENDER));

		scn.MoveMinionsToTable("marksman1");
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(rohirrim);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(arwen));
		assertEquals(0, scn.GetWoundsOn(rohirrim));
		assertEquals(1, scn.GetWoundsOn(arwen));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(rohirrim));
		// Her second wound exhausts her.
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertTrue(scn.HasKeyword(rohirrim, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(rohirrim, Keyword.DEFENDER));
		assertEquals(9, scn.GetStrength(rohirrim));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnExhaustedArwenIsKilledByTheWoundThatSavesAragorn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(arwen);

		scn.StartGame();
		scn.AddWoundsToChar(arwen, 2);
		scn.MoveMinionsToTable("marksman1");
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(aragorn);

		// With 1 vitality left Arwen cannot exert, but she can still be wounded.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(arwen));
		assertEquals(1, scn.GetVitality(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(9, scn.GetStrength(aragorn));
		assertTrue(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.DEAD, arwen);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// What she gave him goes with her.
		assertEquals(8, scn.GetStrength(aragorn));
		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenArwenCannotTakeWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(arwen);

		scn.StartGame();
		// No card makes an Elf ally unable to take wounds, so the state is put on her directly.
		scn.ApplyAdHocModifier(new CantTakeWoundsModifier(null, null, arwen));
		scn.MoveMinionsToTable("marksman1");
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(aragorn);

		// The archery wound went straight onto Aragorn.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
