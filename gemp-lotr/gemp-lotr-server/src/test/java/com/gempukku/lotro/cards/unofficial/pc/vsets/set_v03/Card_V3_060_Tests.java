package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_060_Tests
{

// ----------------------------------------
// THE MOUTH OF SAURON, HERALD OF THE DARK LORD TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mouth", "103_60");       // The Mouth of Sauron, Herald of the Dark Lord
					put("redwrath", "7_157");     // Red Wrath - [Raider] Skirmish event, cost 5
					put("southron", "4_253");     // Desert Sentry - for Red Wrath

					put("aragorn", "1_89");
					put("anduril", "7_79");       // FP weapon (artifact)
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
		 * Name: The Mouth of Sauron, Herald of the Dark Lord
		 * Unique: true
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: Lurker.
		* 	Your Shadow events are twilight cost -1. Response: If a minion is about to take a wound, exert this minion to prevent that wound and hinder a Free Peoples weapon.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mouth");

		assertEquals("The Mouth of Sauron", card.getBlueprint().getTitle());
		assertEquals("Herald of the Dark Lord", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}



	@Test
	public void MouthOfSauronReducesShadowEventCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var redwrath = scn.GetShadowCard("redwrath");
		var southron = scn.GetShadowCard("southron");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveMinionsToTable(mouth, southron);
		scn.MoveCardsToHand(redwrath);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, southron);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Red Wrath normally costs 5, should cost 4 with Mouth
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(redwrath));
	}

	@Test
	public void MouthOfSauronPreventsWoundAndHindersWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var southron = scn.GetShadowCard("southron");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		scn.MoveMinionsToTable(mouth, southron);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, southron);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		int mouthWoundsBefore = scn.GetWoundsOn(mouth);
		int southronWoundsBefore = scn.GetWoundsOn(southron);
		assertFalse(scn.IsHindered(anduril));

		// Aragorn (8) beats Southron (6) - Southron about to take wound
		scn.PassCurrentPhaseActions();

		// Response available
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Mouth exerts
		assertEquals(mouthWoundsBefore + 1, scn.GetWoundsOn(mouth));
		// Southron's wound prevented
		assertEquals(southronWoundsBefore, scn.GetWoundsOn(southron));
		// Weapon hindered
		assertTrue(scn.IsHindered(anduril));
	}

	@Test
	public void MouthOfSauronCanProtectAnyMinionIncludingSelf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		scn.MoveMinionsToTable(mouth);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, mouth);
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn (8) + Anduril (+2?) vs Mouth (9) - let's wound Aragorn to ensure Mouth loses
		// Actually Mouth is 9, Aragorn is 8 - Mouth wins tie. Let's pump Aragorn.
		// Anduril gives +2, so Aragorn is 10 vs Mouth 9 - Aragorn wins

		int mouthWoundsBefore = scn.GetWoundsOn(mouth);

		scn.PassCurrentPhaseActions();

		// Mouth about to take wound from losing - can respond to protect self
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Choose Mouth as the minion to protect (may need to select)
		// Mouth exerts (1 wound) but prevents the skirmish wound
		assertEquals(mouthWoundsBefore + 1, scn.GetWoundsOn(mouth)); // Just the exertion
		assertTrue(scn.IsHindered(anduril));
	}

	@Test
	public void MouthOfSauronCannotRespondIfFullyExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var southron = scn.GetShadowCard("southron");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		scn.MoveMinionsToTable(mouth, southron);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);
		// Exhaust Mouth (3 vitality, so 2 wounds = exhausted)
		scn.AddWoundsToChar(mouth, 2);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, southron);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		scn.PassCurrentPhaseActions();

		// Mouth is exhausted - cannot exert to respond
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}
}
