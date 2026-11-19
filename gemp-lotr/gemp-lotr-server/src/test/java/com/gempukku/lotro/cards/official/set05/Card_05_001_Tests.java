package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_001_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rampager", "5_1");
					put("rampager2", "5_1");

					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man with no text, cost 2, site number 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion, to hold in hand
					put("armor", "1_8");        // Dwarven Armor: a possession, to hold in hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Frodo, Courteous Halfling is the Ring-bearer here: "While you can spot 3 unbound companions, Shadow cards
	// may not discard cards from your hand or from the top of your draw deck."
	protected VirtualTableScenario GetCourteousHalflingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rampager", "5_1");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion
					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound companion
					put("legolas", "1_50");     // Legolas, Greenleaf: an unbound companion; no test here reaches the archery phase
					put("armor", "1_8");        // Dwarven Armor: a possession, to hold in hand
					put("guard", "1_7");        // Dwarf Guard: a companion, to hold in hand
					// Orc Inquisitor: [sauron] Orc, cost 3, site number 6.  "When you play this minion, you may make
					// the Free Peoples player discard a card at random from hand."  A discard the Free Peoples
					// player has no choice in, which Frodo's text stops: it shows that text in force.
					put("inquisitor", "1_268");
				}},
				VirtualTableScenario.FellowshipSites,
				"4_301",
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunlendingRampagerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Dunlending Rampager
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: When you play this minion, the Free Peoples player may discard 2 cards from hand to discard him.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rampager");

		assertEquals("Dunlending Rampager", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void DunlendingRampagerDoesNotOfferChoiceWhenNotEnoughCardsInFreepsHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rampager = scn.GetShadowCard("rampager");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(rampager);
		// One card in hand: one short of the two the Free Peoples player would have to discard.
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();
		// 1 companion + 2 for Ettenmoors
		scn.FreepsPass();
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowPlayAvailable(rampager));

		// 1 to play + 2 roaming (site number 3, at site 2)
		scn.ShadowPlayCard(rampager);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, rampager);

		assertFalse(scn.FreepsDecisionAvailable("Would you like to discard 2 cards from hand"));
		assertEquals(1, scn.GetFreepsHandCount());
		assertInHand(aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerDiscardsTwoCardsOfHisChoiceFromHandAndTheRampagerJustPlayedIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inPlay = scn.GetShadowCard("rampager");
		var played = scn.GetShadowCard("rampager2");
		// A companion, a possession and a Shadow minion of his own: any card in his hand will do.
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var brigand = scn.GetFreepsCard("brigand");
		scn.MoveCardsToHand(played);
		scn.MoveCardsToHand(aragorn, armor, brigand);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(inPlay);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetTwilight());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.ShadowPlayCard(played);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, played);
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard 2 cards from hand to discard Dunlending Rampager?"));

		scn.FreepsChooseYes();
		// Exactly two, of the three he holds.
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, armor, brigand));

		scn.FreepsChooseCards(aragorn, brigand);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInHand(armor);
		assertInDiscard(aragorn, brigand);
		// "Him": the Rampager that was played, and not the one already on the table.
		assertInZone(Zone.DISCARD, played);
		assertInZone(Zone.SHADOW_CHARACTERS, inPlay);
		// What was paid to play him stays paid.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayDeclineAndTheRampagerStays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rampager = scn.GetShadowCard("rampager");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var brigand = scn.GetFreepsCard("brigand");
		scn.MoveCardsToHand(rampager);
		scn.MoveCardsToHand(aragorn, armor, brigand);

		scn.StartGame();
		scn.FreepsPass();

		scn.ShadowPlayCard(rampager);
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard 2 cards from hand to discard Dunlending Rampager?"));

		scn.FreepsChooseNo();
		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(aragorn, armor, brigand);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInZone(Zone.SHADOW_CHARACTERS, rampager);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherMinionPlayedBesideARampagerAsksTheFreePeoplesPlayerNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rampager = scn.GetShadowCard("rampager");
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
		scn.MoveMinionsToTable(rampager);
		scn.ShadowRefreshDecision();
		assertEquals(4, scn.GetTwilight());

		// 2 to play + 2 roaming (site number 3, at site 2)
		scn.ShadowPlayCard(played);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, played, rampager);

		assertFalse(scn.FreepsDecisionAvailable("Would you like to discard 2 cards from hand"));
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BesideFrodoCourteousHalflingWithThreeUnboundCompanionsTheFreePeoplesPlayerMayStillDiscardTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCourteousHalflingScenario();

		var rampager = scn.GetShadowCard("rampager");
		var inquisitor = scn.GetShadowCard("inquisitor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var armor = scn.GetFreepsCard("armor");
		var guard = scn.GetFreepsCard("guard");
		var spare = scn.GetFreepsCard("inquisitor");
		scn.MoveCompanionsToTable(aragorn, gimli, legolas);
		scn.MoveCardsToHand(armor, guard, spare);
		scn.MoveCardsToHand(inquisitor, rampager);

		scn.StartGame();
		// 2 set + 4 companions + 2 for Ettenmoors
		scn.SetTwilight(2);
		scn.FreepsPass();
		assertEquals(8, scn.GetTwilight());
		assertEquals(3, scn.GetFreepsHandCount());

		// Frodo's text is in force: the Inquisitor's discard, which gives the Free Peoples player no choice, is
		// not offered.  3 to play + 2 roaming (site number 6, at site 2).
		scn.ShadowPlayCard(inquisitor);
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(inquisitor));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetFreepsHandCount());

		// The Rampager's is his own to make or not, and Frodo does not stop it.
		scn.ShadowPlayCard(rampager);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard 2 cards from hand to discard Dunlending Rampager?"));

		scn.FreepsChooseYes();
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(armor, spare);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInHand(guard);
		assertInDiscard(armor, spare);
		assertInZone(Zone.DISCARD, rampager);
		assertInZone(Zone.SHADOW_CHARACTERS, inquisitor);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
