package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_116_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("reaper", "13_116");
					put("reaper2", "13_116");
					put("goblin", "11_137");      // Scurrying Goblin: [orc] Orc of another title; text only on its own play
					put("path", "8_42");          // A Path Appointed: [gondor] condition; Fellowship: exert a [gondor] Man and a
					                              // [dwarven] or [rohan] companion to add a [gondor] token here
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man
					put("gimli", "1_13");         // Gimli, Son of Gloin: [dwarven] companion
					put("palaces", "6_10");       // Suspended Palaces: [dwarven] condition that holds [dwarven] tokens
					put("threatening", "13_103"); // Always Threatening: [orc] condition that holds [orc] tokens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcReaperStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Orc Reaper
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While you can spot a Free Peoples culture token, each minion that has the title Orc Reaper is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("reaper");

		assertEquals("Orc Reaper", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithAGondorTokenAddedEachOrcReaperIsStrengthPlus2ForEachCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reaper = scn.GetShadowCard("reaper");
		var reaper2 = scn.GetShadowCard("reaper2");
		var goblin = scn.GetShadowCard("goblin");
		var path = scn.GetFreepsCard("path");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(path);

		scn.StartGame();
		scn.MoveMinionsToTable(reaper, reaper2, goblin);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetCultureTokensOn(path));
		assertEquals(8, scn.GetStrength(reaper));
		assertEquals(8, scn.GetStrength(reaper2));
		assertEquals(5, scn.GetStrength(goblin));

		assertTrue(scn.FreepsActionAvailable(path));
		scn.FreepsUseCardAction(path);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetCultureTokensOn(path));

		// Each copy's text reaches every Orc Reaper: 8 + 2 + 2.
		assertEquals(12, scn.GetStrength(reaper));
		assertEquals(12, scn.GetStrength(reaper2));
		// A minion of another title is untouched.
		assertEquals(5, scn.GetStrength(goblin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShadowCultureTokenDoesNotCountAndADwarvenTokenDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reaper = scn.GetShadowCard("reaper");
		var threatening = scn.GetShadowCard("threatening");
		var palaces = scn.GetFreepsCard("palaces");
		scn.MoveCardsToSupportArea(threatening);
		scn.MoveCardsToSupportArea(palaces);

		scn.StartGame();
		scn.MoveMinionsToTable(reaper);
		scn.AddTokensToCard(threatening, 2);

		// Only an [orc] token in play: no Free Peoples culture token to spot.
		assertEquals(2, scn.GetCultureTokensOn(threatening));
		assertEquals(0, scn.GetCultureTokensOn(palaces));
		assertEquals(8, scn.GetStrength(reaper));

		scn.AddTokensToCard(palaces, 2);
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		// One copy in play: 8 + 2.
		assertEquals(10, scn.GetStrength(reaper));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
