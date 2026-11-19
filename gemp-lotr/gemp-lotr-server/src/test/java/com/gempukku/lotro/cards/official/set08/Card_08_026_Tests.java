package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_026_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shelob", "8_26");
					put("rider", "12_161");     // Black Rider: [wraith] Nazgûl, fierce; strength +1 for each companion, nothing else
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShelobStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Shelob, Last Child of Ungoliant
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Spider
		 * Strength: 8
		 * Vitality: 8
		 * Site Number: 8
		 * Game Text: Enduring. Fierce.
		 * 	While in a fierce skirmish, Shelob is damage +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shelob");

		assertEquals("Shelob", card.getBlueprint().getTitle());
		assertEquals("Last Child of Ungoliant", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.SPIDER, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(8, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SheIsDamagePlus1InHerFierceSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var shelob = scn.GetShadowCard("shelob");
		var rider = scn.GetShadowCard("rider");

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, rider);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// In her first skirmish, which is no fierce skirmish.
		assertFalse(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, shelob));
		assertEquals(0, scn.GetKeywordCount(shelob, Keyword.DAMAGE));

		// Strength 8 against 8: Aragorn loses and takes a wound; both live on to the fierce round.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.InFierceRound());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, rider}, new PhysicalCardImpl[]{boromir, shelob});
		scn.FreepsResolveSkirmish(aragorn);

		// Another fierce minion's fierce skirmish: he is not damage +1, and Shelob, assigned to a fierce skirmish
		// that has not begun, is not in a fierce skirmish yet.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, rider));
		assertEquals(0, scn.GetKeywordCount(rider, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(shelob, Keyword.DAMAGE));

		scn.PassFierceSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, shelob));
		assertEquals(1, scn.GetKeywordCount(shelob, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(rider, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
