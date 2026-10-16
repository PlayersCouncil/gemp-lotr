package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_100_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("choice", "1_100");

					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("arwen", "1_30");           // Arwen, Daughter of Elrond: [elven] Elf companion, vitality 3; strength against a Nazgul only
					// Aragorn, Defender of Rohan: the [rohan] Aragorn, vitality 4; hunter for [rohan] companions only while the Ring-bearer is assigned
					put("rohanaragorn", "17_93");
					// Arwen, Lady of Rivendell: the Arwen who is an ally, vitality 3, home 3F; Aragorn strength +1 (defender +1
					// while she is exhausted), and she may take a wound in place of his: no wound is dealt here
					put("allyarwen", "102_6");
					put("boromir", "1_97");         // Boromir, Son of Denethor: a [gondor] Man companion who is not Aragorn; a skirmish ability only
					put("legolas", "1_50");         // Legolas, Greenleaf: an [elven] Elf companion who is not Arwen; an archery ability only

					put("troop", "1_177");          // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheChoiceofLuthienStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Choice of Lúthien
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Tale</b>. Plays to your support area.
		 * <b>Maneuver:</b> Exert Aragorn to heal Arwen, or exert Arwen to heal Aragorn.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("choice");

		assertEquals("The Choice of Lúthien", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertingAragornHealsArwenAndExertingArwenHealsAragorn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var choice = scn.GetFreepsCard("choice");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, arwen, boromir, legolas);
		scn.MoveCardsToSupportArea(choice);
		// Arwen, vitality 3 with 2 wounds, is exhausted. Boromir and Legolas are wounded, and are not Aragorn or Arwen.
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(legolas, 1);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsActionAvailable(choice));

		// Only Aragorn can exert, so the first half is what happens, unasked.
		scn.FreepsUseCardAction(choice);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Now both can exert, and the player is asked which half.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(choice));
		scn.FreepsUseCardAction(choice);
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Exert Aragorn to heal Arwen"));
		assertTrue(scn.FreepsChoiceAvailable("Exert Arwen to heal Aragorn"));
		scn.FreepsChoose("Exert Arwen to heal Aragorn");

		// Arwen exerts and Aragorn loses one of his two wounds.
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheRohanAragornAndTheArwenWhoIsAnAllyExertAndAreHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var choice = scn.GetFreepsCard("choice");
		var aragorn = scn.GetFreepsCard("rohanaragorn");
		var arwen = scn.GetFreepsCard("allyarwen");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(arwen);
		scn.MoveCardsToSupportArea(choice);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(arwen, 1);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsActionAvailable(choice));

		// Both can exert: the ally Arwen exerts to heal the [rohan] Aragorn.
		scn.FreepsUseCardAction(choice);
		assertTrue(scn.FreepsChoiceAvailable("Exert Aragorn to heal Arwen"));
		assertTrue(scn.FreepsChoiceAvailable("Exert Arwen to heal Aragorn"));
		scn.FreepsChoose("Exert Arwen to heal Aragorn");
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// The ally, vitality 3 with 2 wounds, is exhausted: the [rohan] Aragorn exerts to heal her, unasked.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(choice));
		scn.FreepsUseCardAction(choice);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AragornMayExertThoughArwenHasNoWoundToHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var choice = scn.GetFreepsCard("choice");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, arwen, boromir, legolas);
		scn.MoveCardsToSupportArea(choice);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(legolas, 1);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsActionAvailable(choice));

		// Arwen could exert and heal Aragorn. Aragorn may exert all the same, though Arwen has no wound.
		scn.FreepsUseCardAction(choice);
		assertTrue(scn.FreepsChoiceAvailable("Exert Aragorn to heal Arwen"));
		assertTrue(scn.FreepsChoiceAvailable("Exert Arwen to heal Aragorn"));
		scn.FreepsChoose("Exert Aragorn to heal Arwen");

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ArwenMayExertThoughAragornHasNoWoundToHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var choice = scn.GetFreepsCard("choice");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, arwen, boromir, legolas);
		scn.MoveCardsToSupportArea(choice);
		scn.AddWoundsToChar(arwen, 1);
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(legolas, 1);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsActionAvailable(choice));

		// Aragorn could exert and heal Arwen. Arwen may exert all the same, though Aragorn has no wound.
		scn.FreepsUseCardAction(choice);
		assertTrue(scn.FreepsChoiceAvailable("Exert Aragorn to heal Arwen"));
		assertTrue(scn.FreepsChoiceAvailable("Exert Arwen to heal Aragorn"));
		scn.FreepsChoose("Exert Arwen to heal Aragorn");

		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileAragornAndArwenAreBothExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var choice = scn.GetFreepsCard("choice");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, arwen, boromir, legolas);
		scn.MoveCardsToSupportArea(choice);
		// Aragorn, vitality 4 with 3 wounds, and Arwen, vitality 3 with 2, are exhausted. Boromir and Legolas,
		// unwounded, cannot exert in their places.
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(arwen, 2);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(choice));

		// One wound off Arwen, and she can pay.
		scn.RemoveWoundsFromChar(arwen, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(choice));
	}
}
