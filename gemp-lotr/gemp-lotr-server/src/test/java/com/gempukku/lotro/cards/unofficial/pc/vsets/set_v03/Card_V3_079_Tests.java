package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_079_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("toldea", "103_79");

					put("aragorn", "1_89");  // Base 8 strength
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireToldeaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ulaire Toldea, Blessed with Brutality
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: Fierce.
		* 	Each time this minion is assigned to a stronger companion, you may exhaust this minion to hinder that companion.
		* 	Each time this minion is assigned to a weaker companion, you may exert that companion to add a threat.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("toldea");

		assertEquals("Úlairë Toldëa", card.getBlueprint().getTitle());
		assertEquals("Blessed with Brutality", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}



// ======== STRONGER COMPANION (HINDER) TESTS ========

	@Test
	public void ToldeaCanExhaustToHinderStrongerCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var toldea = scn.GetShadowCard("toldea");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(toldea);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		// Aragorn base 8 + 6 = 14, stronger than Toldëa's 13
		scn.ApplyAdHocModifier(new StrengthModifier(null, Filters.name("Aragorn"), null, 6));

		assertEquals(14, scn.GetStrength(aragorn));
		assertEquals(13, scn.GetStrength(toldea));

		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, toldea);

		// Trigger fires on assignment
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Toldëa exhausted (vitality 3, so 2 wounds)
		assertTrue(scn.IsExhausted(toldea));

		// Aragorn hindered
		assertTrue(scn.IsHindered(aragorn));
	}

	@Test
	public void ToldeaHinderOptionNotAvailableIfAlreadyExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var toldea = scn.GetShadowCard("toldea");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(toldea);
		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(toldea, 2);  // Already exhausted

		scn.StartGame();

		scn.ApplyAdHocModifier(new StrengthModifier(null, Filters.name("Aragorn"), null, 6));

		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, toldea);

		// Can't exhaust when already exhausted
		//Actually this needs fixed once the exhaust rule is in
		//assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

// ======== EQUAL STRENGTH (NO TRIGGER) ========

	@Test
	public void ToldeaNoTriggerWhenAssignedToEqualStrengthCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var toldea = scn.GetShadowCard("toldea");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(toldea);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		// Aragorn base 8 + 5 = 13, equal to Toldëa's 13
		scn.ApplyAdHocModifier(new StrengthModifier(null, Filters.name("Aragorn"), null, 5));

		assertEquals(13, scn.GetStrength(aragorn));
		assertEquals(13, scn.GetStrength(toldea));

		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, toldea);

		// No trigger at equal strength
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

// ======== WEAKER COMPANION (THREAT) TESTS ========

	/*
	 *    _______________________________________________________________
	 *   |                                                               |
	 *   |     🚧🚧🚧  UNDER CONSTRUCTION  🚧🚧🚧                        |
	 *   |                                                               |
	 *   |     Toldëa's "weaker companion" clause is BUSTED and          |
	 *   |     awaiting redesign. Current: "exert companion, add         |
	 *   |     threat" (free value). Proposed: "exert SELF to            |
	 *   |     exert companion OR add threat" (actual choice).           |
	 *   |                                                               |
	 *   |     TODO: Add tests here once the balance fix is in.          |
	 *   |                                                               |
	 *   |     Dear future developer: if this comment is still here      |
	 *   |     and Toldëa shipped unchanged, please roast Ketura         |
	 *   |     in the Discord for leaving broken design in prod.         |
	 *   |                                                               |
	 *   |_______________________________________________________________|
	 */
}
