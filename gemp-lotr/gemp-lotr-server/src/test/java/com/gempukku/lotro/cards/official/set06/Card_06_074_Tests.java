package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_074_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sharku", "6_74");
					put("warwarg", "5_64");     // War-warg: [isengard] mount on a warg-rider, strength +4, vitality +2; a skirmish Response
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, strength 13, vitality 3

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a Maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SharkuStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Sharku, Vile Marauder
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Warg-rider</b>.
		 * While Sharku is not exhausted, he is <b>fierce</b>.
		 * <b>Regroup:</b> Exert Sharku to add (1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sharku");

		assertEquals("Sharku", card.getBlueprint().getTitle());
		assertEquals("Vile Marauder", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.WARG_RIDER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupAbilityExertsSharkuToAddOneAndLeavesHimExhaustedAndNotFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sharku = scn.GetShadowCard("sharku");

		scn.StartGame();
		scn.MoveMinionsToTable(sharku);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();

		// Unwounded, vitality 2: not exhausted, and fierce.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(sharku));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.HasKeyword(sharku, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(sharku));

		scn.ShadowUseCardAction(sharku);

		assertEquals(1, scn.GetWoundsOn(sharku));
		// 1 + 1
		assertEquals(2, scn.GetTwilight());
		// Vitality 2 with 1 wound: the exertion has exhausted him, and he is fierce no longer.
		assertTrue(scn.IsExhausted(sharku));
		assertFalse(scn.HasKeyword(sharku, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ExhaustedSharkuIsNotOfferedHisRegroupAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sharku = scn.GetShadowCard("sharku");

		scn.StartGame();
		scn.MoveMinionsToTable(sharku);
		scn.AddWoundsToChar(sharku, 1);
		assertEquals(1, scn.GetWoundsOn(sharku));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();

		// Vitality 2 with 1 wound: exhausted, so he cannot exert.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(sharku));
		assertTrue(scn.IsExhausted(sharku));
		assertFalse(scn.ShadowActionAvailable(sharku));
		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void ExhaustedHeIsNotFierceUntilAWarWargPlayedOnHimRaisesHisVitality() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sharku = scn.GetShadowCard("sharku");
		var warwarg = scn.GetShadowCard("warwarg");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(warwarg);
		assertInHand(warwarg);

		scn.StartGame();
		scn.SetTwilight(4);
		scn.MoveMinionsToTable(sharku, troop);
		// Unwounded he has vitality 2, one above exhausted, and is fierce.
		assertEquals(0, scn.GetWoundsOn(sharku));
		assertEquals(2, scn.GetVitality(sharku));
		assertFalse(scn.IsExhausted(sharku));
		assertTrue(scn.HasKeyword(sharku, Keyword.FIERCE));
		scn.AddWoundsToChar(sharku, 1);
		assertEquals(1, scn.GetWoundsOn(sharku));
		scn.FreepsPass();

		// Vitality 2 with 1 wound: exhausted, and not fierce; the Troop beside him is neither.
		assertEquals(1, scn.GetWoundsOn(sharku));
		assertTrue(scn.IsExhausted(sharku));
		assertFalse(scn.HasKeyword(sharku, Keyword.FIERCE));
		assertFalse(scn.IsExhausted(troop));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));

		// He is the one warg-rider on the table.
		scn.ShadowPlayCard(warwarg);
		assertAttachedTo(warwarg, sharku);

		// 2 + 2 less 1 wound: 3 left, still wounded but not exhausted, and fierce.
		assertEquals(1, scn.GetWoundsOn(sharku));
		assertEquals(3, scn.GetVitality(sharku));
		assertTrue(scn.HasKeyword(sharku, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
