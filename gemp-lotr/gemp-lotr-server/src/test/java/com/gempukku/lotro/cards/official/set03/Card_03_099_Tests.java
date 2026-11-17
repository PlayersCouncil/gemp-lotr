package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_03_099_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trooper", "3_99");
					put("trooper2", "3_99");

					// Orc Soldier: [sauron] Orc minion, cost 2, strength 7; a skirmish ability used only on purpose
					put("soldier", "1_271");
					// The Number Must Be Few: [sauron] condition, cost 2, plays to the support area with no requirement;
					// its text wants 7 companions
					put("fewer", "1_260");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Free Peoples card, which the Shadow player cannot play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcTrooperStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Orc Trooper
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: For each card in your hand, this minion is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trooper");

		assertEquals("Orc Trooper", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrength1MoreForEachCardInHisOwnersHandAndNotTheOtherPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trooper = scn.GetShadowCard("trooper");
		var trooper2 = scn.GetShadowCard("trooper2");
		var soldier = scn.GetShadowCard("soldier");
		var fewer = scn.GetShadowCard("fewer");
		var aragorn = scn.GetShadowCard("aragorn");
		// Four cards in the Free Peoples player's hand all along, which are not "your hand" to the trooper.
		scn.MoveCardsToHand(scn.GetFreepsCard("trooper"), scn.GetFreepsCard("soldier"), scn.GetFreepsCard("fewer"),
				scn.GetFreepsCard("aragorn"));

		scn.StartGame();
		scn.MoveMinionsToTable(trooper, trooper2);

		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(7, scn.GetStrength(trooper));
		assertEquals(7, scn.GetStrength(trooper2));

		// A minion, a condition and a Free Peoples card: every card in the hand counts.
		scn.MoveCardsToHand(soldier, fewer, aragorn);
		assertEquals(3, scn.GetShadowHandCount());
		// Two copies in play: each has the bonus once, not once for each copy.
		assertEquals(10, scn.GetStrength(trooper));
		assertEquals(10, scn.GetStrength(trooper2));

		scn.SetTwilight(10);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(soldier);
		assertInPlay(soldier);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(9, scn.GetStrength(trooper));
		assertEquals(9, scn.GetStrength(trooper2));
		// Another [sauron] Orc gains nothing from the hand.
		assertEquals(7, scn.GetStrength(soldier));

		scn.ShadowPlayCard(fewer);
		assertInZone(Zone.SUPPORT, fewer);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(8, scn.GetStrength(trooper));
		assertEquals(8, scn.GetStrength(trooper2));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
