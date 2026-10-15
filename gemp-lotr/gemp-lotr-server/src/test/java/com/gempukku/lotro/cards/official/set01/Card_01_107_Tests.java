package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_107_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shield", "1_107");

					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability used only on purpose
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, valiant only
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally; a fellowship ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion; a skirmish ability used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion, archer; his archery ability is not used

					put("marksman1", "1_176");  // Goblin Marksman: [moria] Orc archer and nothing else
					put("marksman2", "1_176");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatShieldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Great Shield
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Shield
		 * Game Text: Bearer must be a Man.<br>The minion archery total is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shield");

		assertEquals("Great Shield", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.SHIELD));
		assertEquals(1, card.getBlueprint().getPossessionClasses().size());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnAManCompanionOrAManAllyAndMakesTheMinionArcheryTotalMinusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var boromir = scn.GetFreepsCard("boromir");
		var guard = scn.GetFreepsCard("guard");
		var barliman = scn.GetFreepsCard("barliman");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(boromir, guard, gimli, legolas);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(shield);

		scn.StartGame();
		// Laid in the fellowship phase of the turn whose archery phase they stand in.
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		scn.MoveMinionsToTable(marksman1, marksman2);

		// Two minion archers; Legolas is the one Free Peoples archer.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertInHand(shield);
		assertTrue(scn.FreepsPlayAvailable(shield));

		scn.FreepsPlayCard(shield);
		// Man companions of two cultures and a Man ally; not a Dwarf, an Elf or a Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, guard, barliman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, legolas, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(barliman);
		assertAttachedTo(shield, barliman);

		// A -2 would read 0; the fellowship archery total is left alone.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
