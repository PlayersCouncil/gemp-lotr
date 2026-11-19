package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_002_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("renegade", "5_2");
					put("renegade2", "5_2");

					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man with no text, cost 2, site number 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion, to hold in hand
					put("armor", "1_8");        // Dwarven Armor: a possession, to hold in hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunlendingRenegadeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Dunlending Renegade
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: When you play this minion, the Free Peoples player may discard 2 cards from hand to discard him.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("renegade");

		assertEquals("Dunlending Renegade", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void DunlendingRenegadeDoesNotOfferChoiceWhenNotEnoughCardsInFreepsHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var renegade = scn.GetShadowCard("renegade");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(renegade);
		// One card in hand: one short of the two the Free Peoples player would have to discard.
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();
		assertInHand(aragorn);
		// 1 set + 1 companion + 2 for Ettenmoors
		scn.SetTwilight(1);
		scn.FreepsPass();
		assertEquals(4, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowPlayAvailable(renegade));

		// 2 to play + 2 roaming (site number 3, at site 2)
		scn.ShadowPlayCard(renegade);
		assertInZone(Zone.SHADOW_CHARACTERS, renegade);

		assertFalse(scn.FreepsDecisionAvailable("Would you like to discard 2 cards from hand"));
		assertEquals(1, scn.GetFreepsHandCount());
		assertInHand(aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerDiscardsTwoCardsOfHisChoiceFromHandAndTheRenegadeJustPlayedIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inPlay = scn.GetShadowCard("renegade");
		var played = scn.GetShadowCard("renegade2");
		// A companion, a possession and a Shadow minion of his own: any card in his hand will do.
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var brigand = scn.GetFreepsCard("brigand");
		scn.MoveCardsToHand(played);
		scn.MoveCardsToHand(aragorn, armor, brigand);

		scn.StartGame();
		assertInHand(aragorn, armor, brigand);
		// 1 set + 1 companion + 2 for Ettenmoors
		scn.SetTwilight(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(inPlay);
		scn.ShadowRefreshDecision();
		assertEquals(4, scn.GetTwilight());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		// 2 to play + 2 roaming (site number 3, at site 2)
		scn.ShadowPlayCard(played);
		assertInZone(Zone.SHADOW_CHARACTERS, played);
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard 2 cards from hand to discard Dunlending Renegade?"));

		scn.FreepsChooseYes();
		// Exactly two, of the three he holds.
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, armor, brigand));

		scn.FreepsChooseCards(aragorn, brigand);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInHand(armor);
		assertInDiscard(aragorn, brigand);
		// "Him": the Renegade that was played, and not the one already on the table.
		assertInZone(Zone.DISCARD, played);
		assertInZone(Zone.SHADOW_CHARACTERS, inPlay);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayDeclineAndTheRenegadeStays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var renegade = scn.GetShadowCard("renegade");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var brigand = scn.GetFreepsCard("brigand");
		scn.MoveCardsToHand(renegade);
		scn.MoveCardsToHand(aragorn, armor, brigand);

		scn.StartGame();
		assertInHand(aragorn, armor, brigand);
		scn.SetTwilight(1);
		scn.FreepsPass();

		scn.ShadowPlayCard(renegade);
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard 2 cards from hand to discard Dunlending Renegade?"));

		scn.FreepsChooseNo();
		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(aragorn, armor, brigand);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInZone(Zone.SHADOW_CHARACTERS, renegade);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherMinionPlayedBesideARenegadeAsksTheFreePeoplesPlayerNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var renegade = scn.GetShadowCard("renegade");
		var played = scn.GetShadowCard("brigand");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var brigand = scn.GetFreepsCard("brigand");
		scn.MoveCardsToHand(played);
		// Three cards: he could pay, were he asked.
		scn.MoveCardsToHand(aragorn, armor, brigand);

		scn.StartGame();
		// 1 set + 1 companion + 2 for Ettenmoors
		scn.SetTwilight(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(renegade);
		scn.ShadowRefreshDecision();
		assertEquals(4, scn.GetTwilight());
		assertEquals(3, scn.GetFreepsHandCount());

		// 2 to play + 2 roaming (site number 3, at site 2)
		scn.ShadowPlayCard(played);
		assertInZone(Zone.SHADOW_CHARACTERS, played, renegade);

		assertFalse(scn.FreepsDecisionAvailable("Would you like to discard 2 cards from hand"));
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
