package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.InitiativeHandSizeModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("reaver", "102_3");
					put("reaver2", "102_3");
					// Dunlending Brigand: another [dunland] Man minion, strength 8, no text. The spare copies
					// fill out hands.
					put("brigand", "4_10");
					put("brigand2", "4_10");
					put("brigand3", "4_10");
					put("brigand4", "4_10");

					put("pippin", "1_306");     // Pippin, Friend to Frodo: cost 1, nothing to spot, protects [shire] tales
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunlendingReaverStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Dunlending Reaver
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: While you have initiative, this minion is strength +3 and damage +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("reaver");

		assertEquals("Dunlending Reaver", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedWhileTheFreePeoplesPlayerHasFourCardsInHandItHasNoBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reaver = scn.GetShadowCard("reaver");
		scn.MoveCardsToHand(reaver);
		// The Shadow player's own hand is the larger, and is not what decides who has initiative.
		scn.MoveCardsToShadowHand("brigand", "brigand2", "brigand3", "brigand4");
		scn.MoveCardsToFreepsHand("brigand", "brigand2", "brigand3", "brigand4");

		scn.StartGame();
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		// 4 set + 1 companion + 2 for the site
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(reaver));

		scn.ShadowPlayCard(reaver);
		// 2 to play + 2 roaming: site number 3, played at site 2
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, reaver);

		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetShadowHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(8, scn.GetStrength(reaver));
		assertFalse(scn.HasKeyword(reaver, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsStrengthPlusThreeAndDamagePlusOneWhileTheFreePeoplesPlayerHasThreeCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reaver = scn.GetShadowCard("reaver");
		var reaver2 = scn.GetShadowCard("reaver2");
		var brigand = scn.GetShadowCard("brigand");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCardsToFreepsHand("pippin", "brigand", "brigand2", "brigand3");

		scn.StartGame();
		// The Shadow player's own hand is empty throughout.
		scn.MoveMinionsToTable(reaver, reaver2, brigand);
		scn.FreepsRefreshDecision();

		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(8, scn.GetStrength(reaver));
		assertEquals(8, scn.GetStrength(reaver2));
		assertEquals(8, scn.GetStrength(brigand));
		assertFalse(scn.HasKeyword(reaver, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(reaver2, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(brigand, Keyword.DAMAGE));

		// The Free Peoples player plays one of his 4 cards, and the Shadow has initiative.
		scn.FreepsPlayCard(pippin);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		// Each copy for itself: 8 + 3 and damage +1, not 8 + 3 + 3 and damage +2.
		assertEquals(11, scn.GetStrength(reaver));
		assertEquals(11, scn.GetStrength(reaver2));
		assertEquals(1, scn.GetKeywordCount(reaver, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(reaver2, Keyword.DAMAGE));
		// The Brigand beside them is another [dunland] Man of strength 8.
		assertEquals(8, scn.GetStrength(brigand));
		assertFalse(scn.HasKeyword(brigand, Keyword.DAMAGE));

		// A fourth card in hand gives the Free Peoples player initiative again.
		scn.MoveCardsToFreepsHand("brigand4");
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(8, scn.GetStrength(reaver));
		assertEquals(8, scn.GetStrength(reaver2));
		assertFalse(scn.HasKeyword(reaver, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(reaver2, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HasItsBonusAgainstFourCardsInHandWhenInitiativeTakesFive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reaver = scn.GetShadowCard("reaver");
		scn.MoveCardsToFreepsHand("brigand", "brigand2", "brigand3", "brigand4");

		scn.StartGame();
		scn.MoveMinionsToTable(reaver);

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(8, scn.GetStrength(reaver));
		assertFalse(scn.HasKeyword(reaver, Keyword.DAMAGE));

		// Initiative is made to take one card more, as Streaming to the Field (8_78) and Driven into the Hills
		// (V2_1) make it. The hand is the same 4 cards and the Shadow now has initiative: the bonus goes by
		// initiative, not by a count of the cards in hand.
		scn.ApplyAdHocModifier(new InitiativeHandSizeModifier(null, null, new ConstantEvaluator(1)));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(11, scn.GetStrength(reaver));
		assertEquals(1, scn.GetKeywordCount(reaver, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
