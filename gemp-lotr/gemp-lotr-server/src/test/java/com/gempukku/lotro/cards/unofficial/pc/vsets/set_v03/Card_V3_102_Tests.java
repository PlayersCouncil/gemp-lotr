package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_102_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mouth", "103_102");      // The Mouth of Sauron
					put("runner", "1_178");       // Goblin Runner - weak minion
					put("sauron", "9_48");        // Sauron - strong minion to beat companions

					put("aragorn", "1_89");       // Strength 8
					put("boromir", "1_97");       // Strength 7
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheMouthofSauronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: The Mouth of Sauron, Emissary of Mordor
		 * Unique: true
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: Lurker.
		* 	Each time a companion loses a skirmish, the Free Peoples player must add a threat or a burden.
		* 	Skirmish: Exert this minion to make a wounded companion strength -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mouth");

		assertEquals("The Mouth of Sauron", card.getBlueprint().getTitle());
		assertEquals("Emissary of Mordor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}



	@Test
	public void MouthTriggerLetsFPChooseThreatOrBurdenWhenCompanionLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(mouth);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, mouth);
		scn.FreepsResolveSkirmish(aragorn);

		int threats = scn.GetThreats();
		int burdens = scn.GetBurdens();

		// Aragorn (8) loses to Mouth (9)
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();

		// FP must choose threat or burden
		assertTrue(scn.FreepsChoiceAvailable("threat"));
		assertTrue(scn.FreepsChoiceAvailable("burden"));

		scn.FreepsChoose("threat");

		assertEquals(threats + 1, scn.GetThreats());
		assertEquals(burdens, scn.GetBurdens());
	}

	@Test
	public void MouthTriggerAllowsBurdenChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(mouth);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, mouth);
		scn.FreepsResolveSkirmish(aragorn);

		int threats = scn.GetThreats();
		int burdens = scn.GetBurdens();

		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();

		// FP must choose threat or burden
		assertTrue(scn.FreepsChoiceAvailable("threat"));
		assertTrue(scn.FreepsChoiceAvailable("burden"));

		scn.FreepsChoose("burden");

		assertEquals(threats, scn.GetThreats());
		assertEquals(burdens + 1, scn.GetBurdens());
	}

	@Test
	public void MouthTriggerFiresWhenCompanionLosesToOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var sauron = scn.GetShadowCard("sauron");
		var aragorn = scn.GetFreepsCard("aragorn");

		// Mouth is Lurker, but trigger still fires when companion loses to Sauron
		scn.MoveMinionsToTable(mouth, sauron);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();

		// Assign Aragorn to Sauron, not Mouth
		scn.FreepsAssignToMinions(aragorn, sauron);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		int burdens = scn.GetBurdens();

		// Aragorn loses to Sauron (8 vs 24, overwhelmed and killed)
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();

		// Mouth's trigger still fires
		assertTrue(scn.FreepsChoiceAvailable("threat"));
		scn.FreepsChoose("burden");

		assertEquals(burdens + 1, scn.GetBurdens());
	}

	@Test
	public void MouthSkirmishAbilityDebuffsWoundedCompanionOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");

		scn.MoveMinionsToTable(mouth);
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 1);  // Aragorn wounded
		// Boromir unwounded

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, mouth);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);

		int aragornStr = scn.GetStrength(aragorn);
		int boromirStr = scn.GetStrength(boromir);

		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(mouth));
		scn.ShadowUseCardAction(mouth);

		// Only wounded Aragorn is valid target
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn));
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo));
		assertFalse(scn.ShadowHasCardChoiceAvailable(boromir));

		scn.ShadowChooseCard(aragorn);

		assertEquals(aragornStr - 1, scn.GetStrength(aragorn));
		assertEquals(boromirStr, scn.GetStrength(boromir));
		assertEquals(1, scn.GetWoundsOn(mouth));
	}
}
