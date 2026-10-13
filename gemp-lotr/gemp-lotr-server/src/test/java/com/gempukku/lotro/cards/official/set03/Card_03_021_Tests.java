package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_021_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("knives", "3_21");

					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, an archer; an archery ability used only on purpose
					put("legolasally", "31_9"); // Legolas, Prince of Mirkwood: the one Legolas who is an ally (Hobbit set); an archery ability only
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] Elf companion of another title; text only against a Nazgul
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose

					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; text only when played
					put("runner2", "1_178");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; damage +1 and nothing else
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LongknivesofLegolasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Long-knives of Legolas
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +1
		 * Game Text: Bearer must be Legolas.<br>Each Orc or Uruk-hai skirmishing Legolas is strength -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("knives");

		assertEquals("Long-knives of Legolas", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnlyOnLegolas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knives = scn.GetFreepsCard("knives");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(knives, legolas);

		scn.StartGame();

		// Arwen, another [elven] Elf, and Frodo cannot bear it.
		assertFalse(scn.FreepsPlayAvailable(knives));

		scn.FreepsPlayCard(legolas);
		assertTrue(scn.FreepsPlayAvailable(knives));

		// One bearer it may go on: no choice is asked.
		scn.FreepsPlayCard(knives);
		assertAttachedTo(knives, legolas);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysOnLegolasTheAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knives = scn.GetFreepsCard("knives");
		var legolas = scn.GetFreepsCard("legolasally");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(legolas);
		scn.MoveCardsToHand(knives);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(knives));
		scn.FreepsPlayCard(knives);
		assertAttachedTo(knives, legolas);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachOrcAndUrukHaiSkirmishingLegolasIsStrengthMinus2AndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knives = scn.GetFreepsCard("knives");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, savage, brigand);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Legolas is an archer: laid after the archery phase, so that he wounds nobody.
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, knives);
		scn.PassAssignmentActions();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner2}, new PhysicalCardImpl[]{legolas, runner1});
		// The minions left over go to Legolas as well.
		scn.ShadowAssignToMinions(legolas, savage, brigand);
		scn.FreepsResolveSkirmish(aragorn);

		// An Orc skirmishing Aragorn, and an Orc and an Uruk-hai assigned to Legolas whose skirmish has not begun.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner2));
		assertTrue(scn.IsCharAssignedAgainst(legolas, runner1));
		assertEquals(5, scn.GetStrength(runner2));
		assertEquals(5, scn.GetStrength(runner1));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(8, scn.GetStrength(brigand));

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(legolas);

		assertTrue(scn.IsCharSkirmishingAgainst(legolas, runner1));
		assertTrue(scn.IsCharSkirmishingAgainst(legolas, savage));
		assertTrue(scn.IsCharSkirmishingAgainst(legolas, brigand));
		// [moria] Orc and [isengard] Uruk-hai: 5 - 2.  The Man is not touched.
		assertEquals(3, scn.GetStrength(runner1));
		assertEquals(3, scn.GetStrength(savage));
		assertEquals(8, scn.GetStrength(brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
