package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_19_039_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "19_39");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFierceScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("toldea", "19_39");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used
					// Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability never used
					put("boromir", "1_97");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireToldeaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Úlairë Toldëa, Dark Shadow
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br>Úlairë Toldëa cannot be assigned to skirmish the same companion in a fierce skirmish that he has already been assigned to this turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Úlairë Toldëa", card.getBlueprint().getTitle());
		assertEquals("Dark Shadow", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void InTheFierceRoundToldeaCannotBeAssignedToTheCompanionHeWasAssignedToEarlierThisTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFierceScenario();

		var toldea = scn.GetShadowCard("toldea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		scn.SkipToAssignments();

		// The first round: either player may assign him to either companion.
		assertFalse(scn.InFierceRound());
		assertTrue(scn.FreepsCanAssignAgainst(aragorn, toldea));
		assertTrue(scn.ShadowCanAssignAgainst(aragorn, toldea));
		assertTrue(scn.FreepsCanAssignAgainst(boromir, toldea));
		assertTrue(scn.ShadowCanAssignAgainst(boromir, toldea));

		// Aragorn's 8 loses to 12, and he takes a wound.
		scn.FreepsAssignAndResolve(aragorn, toldea);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(toldea));

		// The fierce round. He was assigned to Aragorn this turn and to Boromir he was not: neither player
		// may give him Aragorn again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		assertFalse(scn.FreepsCanAssignAgainst(aragorn, toldea));
		assertFalse(scn.ShadowCanAssignAgainst(aragorn, toldea));
		assertTrue(scn.FreepsCanAssignAgainst(boromir, toldea));
		assertTrue(scn.ShadowCanAssignAgainst(boromir, toldea));

		scn.FreepsAssignToMinions(boromir, toldea);
		assertTrue(scn.IsCharAssignedAgainst(boromir, toldea));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
