package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_070_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("servants", "53_70");
					put("servants2", "53_70");

					put("retainer", "3_57");    // Isengard Retainer: [isengard] Orc, strength 8; a regroup ability only
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, strength 7; a regroup ability only
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, strength 6; a Shadow ability only
					put("commander", "3_64");   // Orc Commander: [isengard] Orc, strength 11; each other [isengard] Orc is strength +1
					put("warrior", "4_165");    // Orthanc Warrior: [isengard] Uruk-hai, strength 7, damage +1
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7; a skirmish ability only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ServantsToSarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Servants to Saruman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Skirmish: Make an [isengard] Orc strength +2 (or +4 if that Orc is strength 7 or less).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("servants");

		assertEquals("Servants to Saruman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAStrength8IsengardOrcStrengthPlus2UntilTheEndOfTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var retainer = scn.GetShadowCard("retainer");
		var servant = scn.GetShadowCard("servant");
		var warrior = scn.GetShadowCard("warrior");
		var soldier = scn.GetShadowCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(servants);

		scn.StartGame();
		scn.MoveMinionsToTable(retainer, servant, warrior, soldier);

		// Not in the assignment phase, the Shadow player's last chance to act before the skirmishes.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(servants));
		scn.ShadowPass();

		scn.FreepsAssignAndResolve(aragorn, retainer);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(servants));
		assertEquals(8, scn.GetStrength(retainer));
		assertEquals(7, scn.GetStrength(servant));

		scn.ShadowPlayCard(servants);
		// An [isengard] Orc in the skirmish or out of it; not an [isengard] Uruk-hai, nor a [sauron] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(retainer, servant));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(warrior, soldier));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(retainer);

		// Strength 8 is not 7 or less: +2.
		assertInDiscard(servants);
		assertEquals(10, scn.GetStrength(retainer));
		assertEquals(7, scn.GetStrength(servant));
		assertEquals(7, scn.GetStrength(warrior));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(retainer));
	}

	@Test
	public void AnIsengardOrcOfStrength7OutsideTheSkirmishGetsPlus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var retainer = scn.GetShadowCard("retainer");
		var servant = scn.GetShadowCard("servant");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(servants);

		scn.StartGame();
		scn.MoveMinionsToTable(retainer, servant);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, retainer);
		scn.FreepsPass();

		assertFalse(scn.IsCharAssigned(servant));
		assertEquals(7, scn.GetStrength(servant));

		scn.ShadowPlayCard(servants);
		scn.ShadowChooseCard(servant);

		// Strength 7: +4, with nothing asked.
		assertEquals(11, scn.GetStrength(servant));
		assertEquals(8, scn.GetStrength(retainer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheOrcsStrengthIsReadAsItIsNotAsPrinted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var servants2 = scn.GetShadowCard("servants2");
		var commander = scn.GetShadowCard("commander");
		var servant = scn.GetShadowCard("servant");
		var forger = scn.GetShadowCard("forger");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(servants, servants2);

		scn.StartGame();
		scn.MoveMinionsToTable(commander, servant, forger);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, commander);
		scn.FreepsPass();

		// Printed 7, made 8 by Orc Commander: +2.
		assertEquals(8, scn.GetStrength(servant));
		scn.ShadowPlayCard(servants);
		scn.ShadowChooseCard(servant);
		assertEquals(10, scn.GetStrength(servant));

		// Printed 6, made 7 by Orc Commander: +4.
		scn.FreepsPass();
		assertEquals(7, scn.GetStrength(forger));
		scn.ShadowPlayCard(servants2);
		scn.ShadowChooseCard(forger);
		assertEquals(11, scn.GetStrength(forger));
		assertEquals(10, scn.GetStrength(servant));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
