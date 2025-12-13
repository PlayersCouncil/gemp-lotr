package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_045_Tests
{

// ----------------------------------------
// CORSAIR QUARTERMASTER TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("quartermaster", "103_45"); // Corsair Quartermaster

					put("aragorn", "1_89");
					put("anduril", "7_79");        // Artifact - item
					put("athelas", "1_94");        // Possession - item
					put("lastalliance", "1_49");  // Condition - NOT an item
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorsairQuartermasterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Corsair Quartermaster
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Corsair.</b>
		* 	At the start of each skirmish involving this minion, the Free Peoples player may hinder any number of Free Peoples items on a character in that skirmish.
		* 	This minion is <b>damage +1</b> for each Free Peoples item you can spot on each character in its skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("quartermaster");

		assertEquals("Corsair Quartermaster", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.CORSAIR));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}



	@Test
	public void CorsairQuartermasterGainsDamageForEachItemOnSkirmishingCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quartermaster = scn.GetShadowCard("quartermaster");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var athelas = scn.GetFreepsCard("athelas");
		var lastalliance = scn.GetFreepsCard("lastalliance");
		scn.MoveMinionsToTable(quartermaster);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril, athelas, lastalliance);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, quartermaster);
		scn.FreepsResolveSkirmish(aragorn);

		// Should only be able to hinder items, not the condition
		assertTrue(scn.FreepsHasCardChoicesAvailable(anduril, athelas));
		assertFalse(scn.FreepsHasCardChoiceAvailable(lastalliance));

		// FP is offered to hinder items - decline all
		scn.FreepsDeclineChoosing(); // Hinder none

		// 2 items (anduril, athelas) on Aragorn - lastalliance is a condition, not an item
		assertEquals(2, scn.GetKeywordCount(quartermaster, Keyword.DAMAGE));
	}

	@Test
	public void CorsairQuartermasterHinderingItemsReducesDamage() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quartermaster = scn.GetShadowCard("quartermaster");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var athelas = scn.GetFreepsCard("athelas");
		scn.MoveMinionsToTable(quartermaster);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril, athelas);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, quartermaster);
		scn.FreepsResolveSkirmish(aragorn);

		// Start with 2 items = damage +2
		assertEquals(2, scn.GetKeywordCount(quartermaster, Keyword.DAMAGE));

		// FP hinders one item
		scn.FreepsChooseCard(anduril);

		assertTrue(scn.IsHindered(anduril));
		assertFalse(scn.IsHindered(athelas));

		// Now only 1 unhindred item = damage +1
		assertEquals(1, scn.GetKeywordCount(quartermaster, Keyword.DAMAGE));
	}

	@Test
	public void CorsairQuartermasterDoesNotCountItemsOnNonSkirmishingCharacters() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quartermaster = scn.GetShadowCard("quartermaster");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var athelas = scn.GetFreepsCard("athelas");
		scn.MoveMinionsToTable(quartermaster);
		scn.MoveCompanionsToTable(aragorn);
		// Attach items to Frodo (not skirmishing) instead of Aragorn
		scn.AttachCardsTo(frodo, anduril, athelas);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, quartermaster);
		scn.FreepsResolveSkirmish(aragorn);

		// No items on skirmishing characters - no damage bonus
		assertEquals(0, scn.GetKeywordCount(quartermaster, Keyword.DAMAGE));
	}
}
