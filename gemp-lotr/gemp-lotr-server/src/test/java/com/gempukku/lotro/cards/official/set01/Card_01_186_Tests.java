package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_186_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("commander", "1_186");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its (2) only when played
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13; no text
					put("cavetroll", "1_165");  // Cave Troll of Moria: [moria] Troll, strength 15, no Orc; its other text is about playing it
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7; its text answers a weapon played on it

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GuardCommanderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Guard Commander
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Skirmish:</b> Remove (3) to make a [moria] Orc strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("commander");

		assertEquals("Guard Commander", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityRemovesThreeToMakeAMoriaOrcAnywhereStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander = scn.GetShadowCard("commander");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var cavetroll = scn.GetShadowCard("cavetroll");
		var chieftain = scn.GetShadowCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(commander, runner, troop, cavetroll, chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, commander}, new PhysicalCardImpl[]{frodo, runner});
		// The Troop, the Cave Troll and the Orc Chieftain are left over, and stay unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, commander));
		assertEquals(7, scn.GetStrength(commander));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(15, scn.GetStrength(cavetroll));
		assertEquals(7, scn.GetStrength(chieftain));
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(commander));
		scn.ShadowUseCardAction(commander);
		assertEquals(2, scn.GetTwilight());
		// [moria] Orcs in this skirmish, in another and in none; not a [moria] Troll, a [sauron] Orc, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(commander, runner, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(cavetroll, chieftain, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);

		assertEquals(15, scn.GetStrength(troop));
		assertEquals(7, scn.GetStrength(commander));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(15, scn.GetStrength(cavetroll));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheStrengthBonusLastsToTheEndOfTheSkirmishPhaseAndNotIntoTheNextSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander = scn.GetShadowCard("commander");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(commander, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, commander}, new PhysicalCardImpl[]{frodo, runner});
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);

		// The Runner waits for his own skirmish, the second of the turn.
		assertEquals(5, scn.GetStrength(runner));
		scn.FreepsPass();
		scn.ShadowUseCardAction(commander);
		scn.ShadowChooseCard(runner);
		assertEquals(7, scn.GetStrength(runner));

		// Still in force at the last decision of the skirmish phase it was made in.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(runner));

		// Gone when the next skirmish phase begins, and in his own skirmish.
		scn.ShadowPass();
		assertEquals(5, scn.GetStrength(runner));
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, runner));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithOnlyTwoTwilightTheAbilityIsNotOfferedAndWithThreeItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander = scn.GetShadowCard("commander");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(commander);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, commander);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(commander));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(commander));
	}
}
