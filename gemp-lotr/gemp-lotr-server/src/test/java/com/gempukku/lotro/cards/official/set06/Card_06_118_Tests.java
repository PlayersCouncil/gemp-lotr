package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_118_Tests
{

	// The Towers path with Hornburg Hall as its site 6.  Both players hold every card below, and whatever a test
	// does not place lies in its owner's draw deck.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Companions with the Théoden signet, of four cultures and races.
					put("eowyn", "4_270");      // Éowyn, Lady of Rohan: [rohan] Man; a response to a wound on an unbound companion only
					put("gimli", "5_7");        // Gimli, Skilled Defender: [dwarven] Dwarf; a trigger on his own skirmish win only
					put("merry", "7_321");      // Merry, Swordthain: [shire] Hobbit; a skirmish ability only
					put("legolas", "5_121");    // Legolas, Archer of Mirkwood: [elven] Elf; a trigger on his own skirmish win only
					// A [rohan] Man companion with the Aragorn signet; an optional start-of-turn heal of a [rohan] ally only.
					put("eomer", "4_266");      // Éomer, Sister-son of Théoden

					// Cards to hold in hand: a companion, a possession and a Shadow minion.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					put("armor", "1_8");        // Dwarven Armor
					put("troop", "1_177");      // Goblin Patrol Troop

					// Cards to draw.
					put("guard", "1_7");        // Dwarf Guard
					put("runner", "1_178");     // Goblin Runner
					put("savage", "1_151");     // Uruk Savage
					put("marksman", "1_176");   // Goblin Marksman
					put("brigand", "4_10");     // Dunlending Brigand
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.TowersSites);
					put("site6", "6_118");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HornburgHallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Hornburg Hall
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 6T
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: <b>Sanctuary</b>. <b>Fellowship:</b> Spot 3 companions with the Théoden signet and discard your hand to draw 4 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(6);

		assertEquals("Hornburg Hall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(6, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void WithThreeCompanionsOfTheTheodenSignetTheHandIsDiscardedAndFourCardsAreDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var troop = scn.GetFreepsCard("troop");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetFreepsCard("runner");
		var savage = scn.GetFreepsCard("savage");
		var marksman = scn.GetFreepsCard("marksman");
		var brigand = scn.GetFreepsCard("brigand");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(eowyn, gimli, merry);

		// The site under the fellowship is the copy the other player laid.
		var hall = scn.GetShadowSite(6);
		assertSame(hall, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());

		// Three cards in hand, of three kinds, a Shadow card among them; Marksman, Savage, Runner, Guard and the
		// Brigand on top of the deck, the Brigand highest.  The other player holds two cards.
		scn.SetFreepsHand(aragorn, armor, troop);
		scn.MoveCardsToTopOfDeck(marksman, savage, runner, guard, brigand);
		assertInZone(Zone.DECK, marksman, savage, runner, guard, brigand);
		scn.SetShadowHand(scn.GetShadowCard("aragorn"), scn.GetShadowCard("armor"));
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(aragorn, armor, troop);
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();
		assertEquals(2, scn.GetShadowHandCount());
		int shadowDeck = scn.GetShadowDeckCount();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(hall));
		scn.FreepsUseCardAction(hall);

		// The whole hand is discarded, and then the four cards that were on top are drawn: the fifth stays.
		assertInDiscard(aragorn, armor, troop);
		assertEquals(discard + 3, scn.GetFreepsDiscardCount());
		assertEquals(4, scn.GetFreepsHandCount());
		assertInHand(brigand, guard, runner, savage);
		assertInZone(Zone.DECK, marksman);
		assertEquals(deck - 4, scn.GetFreepsDeckCount());

		// "Your" hand: the other player's hand and deck are as they were.
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAnEmptyHandAndThreeOtherCompanionsOfTheSignetTheActionStillDrawsFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetFreepsCard("runner");
		var savage = scn.GetFreepsCard("savage");
		var marksman = scn.GetFreepsCard("marksman");
		var brigand = scn.GetFreepsCard("brigand");

		scn.StartGame();
		scn.SkipToSite(6);
		// An Elf, a Dwarf and a Hobbit of the signet, and no [rohan] Man.
		scn.MoveCompanionsToTable(legolas, gimli, merry);

		var hall = scn.GetShadowSite(6);
		assertSame(hall, scn.GetCurrentSite());

		scn.SetFreepsHand();
		scn.MoveCardsToTopOfDeck(marksman, savage, runner, guard, brigand);
		assertInZone(Zone.DECK, marksman, savage, runner, guard, brigand);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();

		// A hand of no cards is discarded for nothing (Q-0255), and the draw is still made.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(hall));
		scn.FreepsUseCardAction(hall);

		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertEquals(4, scn.GetFreepsHandCount());
		assertInHand(brigand, guard, runner, savage);
		assertInZone(Zone.DECK, marksman);
		assertEquals(deck - 4, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTwoCompanionsOfTheTheodenSignetTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var gimli = scn.GetFreepsCard("gimli");
		var eomer = scn.GetFreepsCard("eomer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");

		scn.StartGame();
		scn.SkipToSite(6);
		// Two of the Théoden signet, and a third [rohan] Man companion beside them with the Aragorn signet (and
		// Frodo, with the Frodo signet).
		scn.MoveCompanionsToTable(eowyn, gimli, eomer);

		var hall = scn.GetShadowSite(6);
		assertSame(hall, scn.GetCurrentSite());

		scn.SetFreepsHand(aragorn, armor);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetFreepsHandCount());

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(hall));
		assertInHand(aragorn, armor);
	}

	@Test
	public void WithNoCompanionOfTheTheodenSignetTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");

		scn.StartGame();
		scn.SkipToSite(6);
		// Éomer (Aragorn signet) and Frodo (Frodo signet): companions, of no Théoden signet.
		scn.MoveCompanionsToTable(eomer);

		var hall = scn.GetShadowSite(6);
		assertSame(hall, scn.GetCurrentSite());

		scn.SetFreepsHand(aragorn, armor);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetFreepsHandCount());

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(hall));
		assertInHand(aragorn, armor);
	}
}
