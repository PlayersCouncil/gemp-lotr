package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_290_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("soldier", "7_290");
					put("soldier2", "7_290");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; its text answers a weapon played on it

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MordorSoldierStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Mordor Soldier
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 3
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Skirmish:</b> Remove a threat to make this minion strength +4.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("soldier");

		assertEquals("Mordor Soldier", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityRemovesOneThreatToMakeThisSoldierStrengthPlus4AndNotTheOtherCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		// Frodo, Aragorn and Gimli: room for the two threats.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.MoveMinionsToTable(soldier, soldier2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, soldier);
		// The other copy, left over, goes into the same skirmish.
		scn.ShadowAssignToMinions(aragorn, soldier2);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(3, scn.GetStrength(soldier));
		assertEquals(3, scn.GetStrength(soldier2));
		assertTrue(scn.ShadowActionAvailable(soldier));
		assertTrue(scn.ShadowActionAvailable(soldier2));

		scn.ShadowUseCardAction(soldier);
		assertEquals(1, scn.GetThreats());
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(3, scn.GetStrength(soldier2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsOfferedWhileHeWaitsForHisSkirmishAndTheBonusLastsOnlyForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier");
		var chieftain = scn.GetShadowCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.MoveMinionsToTable(soldier, chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, soldier}, new PhysicalCardImpl[]{gimli, chieftain});

		// Gimli's skirmish first: the Soldier is waiting for his own against Aragorn.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertEquals(3, scn.GetStrength(soldier));
		assertTrue(scn.ShadowActionAvailable(soldier));
		scn.ShadowUseCardAction(soldier);
		assertEquals(0, scn.GetThreats());
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();
		// The Chieftain's 7 against Gimli's 6: a wound, not a kill.
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(aragorn);

		// The +4 was made in Gimli's skirmish and is gone in the next.
		assertEquals(3, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoThreatToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		assertEquals(0, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, soldier);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(soldier));
		assertEquals(3, scn.GetStrength(soldier));
	}
}
