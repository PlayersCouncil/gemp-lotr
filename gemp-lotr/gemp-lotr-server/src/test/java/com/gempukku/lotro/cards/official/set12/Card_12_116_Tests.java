package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_116_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haethen", "12_116");
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3
					put("rider1", "12_161");    // Black Rider: fierce, strength 8 +1 for each companion
					put("rider2", "12_161");
					put("skirmisher", "18_90"); // Orkish Skirmisher: strength 9; wins a skirmish, may exert twice to wound a companion
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HaethenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Haethen, Veteran Fighter
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 5
		 * Game Text: Haethen cannot take wounds during fierce skirmishes.<br><b>Skirmish:</b> Exert Haethen to make him strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("haethen");

		assertEquals("Haethen", card.getBlueprint().getTitle());
		assertEquals("Veteran Fighter", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotTakeWoundsInHisFierceSkirmishThoughHeDoesInHisFirstAndBoromirDoesInBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haethen = scn.GetFreepsCard("haethen");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(haethen, boromir);

		var rider1 = scn.GetShadowCard("rider1");
		var rider2 = scn.GetShadowCard("rider2");

		scn.StartGame();
		scn.MoveMinionsToTable(rider1, rider2);

		scn.SkipToAssignments();
		// 8 + 3 companions (Frodo, Haethen, Boromir): each Man loses, and is not overwhelmed.
		assertEquals(11, scn.GetStrength(rider1));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{haethen, rider1}, new PhysicalCardImpl[]{boromir, rider2});

		assertEquals(0, scn.GetWoundsOn(haethen));
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.FreepsResolveSkirmish(haethen);
		scn.PassSkirmishActions();
		// A skirmish that is not fierce: he takes the wound for losing.
		assertEquals(1, scn.GetWoundsOn(haethen));
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));

		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{haethen, rider1}, new PhysicalCardImpl[]{boromir, rider2});

		scn.FreepsResolveSkirmish(haethen);
		// His own exertion is no wound, and is placed in the fierce skirmish all the same.
		scn.FreepsUseCardAction(haethen);
		assertEquals(2, scn.GetWoundsOn(haethen));
		assertEquals(8, scn.GetStrength(haethen));
		scn.PassCurrentPhaseActions();
		// He loses at 8 against 11 and takes no wound.
		assertEquals(2, scn.GetWoundsOn(haethen));

		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		// Boromir loses his own fierce skirmish and is wounded.
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotTakeWoundsDuringAFierceSkirmishHeIsNotIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var haethen = scn.GetFreepsCard("haethen");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(haethen, boromir);

		var skirmisher = scn.GetShadowCard("skirmisher");

		scn.StartGame();
		scn.MoveMinionsToTable(skirmisher);
		// Fierce only so that there is a fierce skirmish for Haethen to stay out of.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, skirmisher, null, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, skirmisher);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		// 9 against 7: the Skirmisher wins; his trigger is kept for the fierce skirmish.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		scn.ShadowDeclineOptionalTrigger();

		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(1, scn.GetWoundsOn(boromir));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, skirmisher);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(skirmisher));
		// Haethen is in no skirmish, but it is a fierce skirmish: he cannot be wounded.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, boromir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(haethen));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.ShadowChooseCard(frodo);
		// The Ruling Ring's response to the wound is declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(haethen));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsHimForStrengthPlusOneForTheSkirmishInOrOutOfHisOwnUntilExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haethen = scn.GetFreepsCard("haethen");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(haethen, boromir);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{haethen, runner1}, new PhysicalCardImpl[]{boromir, runner2});
		scn.FreepsResolveSkirmish(haethen);

		assertEquals(0, scn.GetWoundsOn(haethen));
		assertEquals(7, scn.GetStrength(haethen));
		assertTrue(scn.FreepsActionAvailable(haethen));
		scn.FreepsUseCardAction(haethen);
		assertEquals(1, scn.GetWoundsOn(haethen));
		assertEquals(8, scn.GetStrength(haethen));
		scn.PassCurrentPhaseActions();

		scn.FreepsResolveSkirmish(boromir);
		// The bonus lasted his own skirmish only.
		assertEquals(7, scn.GetStrength(haethen));
		// In Boromir's skirmish he is in none, and may still use it on himself.
		assertTrue(scn.FreepsActionAvailable(haethen));
		scn.FreepsUseCardAction(haethen);
		assertEquals(2, scn.GetWoundsOn(haethen));
		assertEquals(8, scn.GetStrength(haethen));

		scn.ShadowPass();
		// Vitality 3 with 2 wounds: exhausted, and he cannot exert again.
		assertFalse(scn.FreepsActionAvailable(haethen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
