package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_090_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("skirmisher", "18_90");
					put("skirmisher2", "18_90");   // a second copy: not unique

					put("aragorn", "1_89");    // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_97");    // Boromir, Son of Denethor: [gondor] Man, strength 7, vitality 3; a skirmish ability used only on purpose
					put("guard", "1_7");       // Dwarf Guard: [dwarven] Dwarf, strength 4, vitality 2; to play, spot a Dwarf (cheated in)
					put("barliman", "1_70");   // Barliman Butterbur: [gandalf] ally, vitality 2; a fellowship ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishSkirmisherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Orkish Skirmisher
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: Each time this minion wins a skirmish, you may exert him twice to wound a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("skirmisher");

		assertEquals("Orkish Skirmisher", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningASkirmishOffersToExertHimTwiceToWoundACompanionOfAnyKind() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var guard = scn.GetFreepsCard("guard");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, boromir, guard);
		scn.MoveAlliesToTable(barliman);
		// One wound leaves the Dwarf Guard exhausted: a wound kills him.
		scn.AddWoundsToChar(guard, 1);

		var skirmisher = scn.GetShadowCard("skirmisher");
		var skirmisher2 = scn.GetShadowCard("skirmisher2");

		scn.StartGame();
		scn.MoveMinionsToTable(skirmisher, skirmisher2);
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, skirmisher);
		// The second copy is left over and stays out of the skirmishes.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// 9 against 8: he wins, and Aragorn takes the wound of the loss.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(skirmisher));
		assertEquals(0, scn.GetWoundsOn(skirmisher2));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(skirmisher2));
		scn.ShadowAcceptOptionalTrigger();

		// He exerts twice; the other copy does not.
		assertEquals(2, scn.GetWoundsOn(skirmisher));
		assertEquals(0, scn.GetWoundsOn(skirmisher2));

		// Every companion is offered, the exhausted one among them; the ally and the minions are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, boromir, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(barliman, skirmisher, skirmisher2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guard);

		// A wound, not an exertion: the exhausted Dwarf Guard is killed.
		assertInZone(Zone.DEAD, guard);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachWinOffersItAgainAndDecliningChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var skirmisher = scn.GetShadowCard("skirmisher");

		scn.StartGame();
		scn.MoveMinionsToTable(skirmisher);
		// Fierce only so that he fights a second skirmish in the same turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, skirmisher, null, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, skirmisher);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		scn.ShadowDeclineOptionalTrigger();

		// Declined: he does not exert and nobody else is wounded.
		assertEquals(0, scn.GetWoundsOn(skirmisher));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.InFierceRound());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, skirmisher);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		// 9 against 7: his second win, offered again.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(skirmisher));
		scn.ShadowChooseCard(aragorn);

		// One wound on the chosen companion.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishDoesNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var skirmisher = scn.GetShadowCard("skirmisher");

		scn.StartGame();
		scn.MoveMinionsToTable(skirmisher);
		// Aragorn at 12 beats his 9 without doubling it: he takes one wound and lives.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 4));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, skirmisher);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(skirmisher));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInPlay(skirmisher);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTwoVitalityLeftHeCannotExertTwiceAndIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var skirmisher = scn.GetShadowCard("skirmisher");

		scn.StartGame();
		scn.MoveMinionsToTable(skirmisher);
		// Vitality 3 with 1 wound: 2 left, and exerting twice needs 3 (CR 4.2 "exert").
		scn.AddWoundsToChar(skirmisher, 1);
		assertEquals(1, scn.GetWoundsOn(skirmisher));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, skirmisher);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// He still wins, 9 against 8.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		assertEquals(1, scn.GetWoundsOn(skirmisher));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
