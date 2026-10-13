package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_068_Tests
{
	// The turns that bring the fellowship to site 3 draw most of this small deck into its owner's hand, so each test
	// lays the hand, the draw deck and the discard pile out afresh once it is there.  The fellowship decision has
	// been sent by then, so each test first plays a Rider of Rohan it has held since before the game began: after a
	// legal play the game asks again, with the hand as the test laid it out.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard1", "5_83");          // Household Guard: [rohan] Man, cost 2, not unique; Valiant is its whole text
					put("guard2", "5_83");
					put("guard3", "5_83");
					// Eomer, Eored Captain: [rohan] Man, cost 3, unique, valiant; his text is about minions he skirmishes
					put("eomer1", "19_25");
					put("eomer2", "19_25");
					// Lorien Swordsman: [elven] Elf, cost 2, not unique, valiant; his text is about minions he skirmishes
					put("swordsman", "54_78");
					// Rider of Rohan: [rohan] Man, cost 2, not unique and not valiant; a skirmish ability that needs a mount
					put("rider1", "4_286");
					put("rider2", "4_286");
					// Aldor, Soldier of Edoras: [rohan] ally, cost 1, home site 3; an archery ability that needs a villager
					put("aldor", "4_262");
					put("spear", "4_288");          // Rider's Spear: [rohan] hand weapon, cost 1, for a [rohan] Man
					put("runner1", "1_178");        // Goblin Runner: a card for the top of the draw deck, to be drawn
					put("runner2", "1_178");
					put("runner3", "1_178");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "102_68");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheodredsTombStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Theodred's Tomb
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 3
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: Sanctuary. Fellowship: Play a valiant character to draw a card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("Theodred's Tomb", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(3, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void FellowshipActionPlaysAValiantCharacterFromHandToDrawACardAsOftenAsOneCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var eomer1 = scn.GetFreepsCard("eomer1");
		var eomer2 = scn.GetFreepsCard("eomer2");
		var swordsman = scn.GetFreepsCard("swordsman");
		var rider1 = scn.GetFreepsCard("rider1");
		var rider2 = scn.GetFreepsCard("rider2");
		var aldor = scn.GetFreepsCard("aldor");
		var spear = scn.GetFreepsCard("spear");
		var runner1 = scn.GetFreepsCard("runner1");
		var runner2 = scn.GetFreepsCard("runner2");
		var runner3 = scn.GetFreepsCard("runner3");

		scn.MoveCardsToHand(rider1);
		scn.StartGame();
		scn.SkipToSite(3);

		var tomb = scn.GetCurrentSite();
		assertEquals("Theodred's Tomb", tomb.getBlueprint().getTitle());

		// In hand: three valiant companions, of two cultures, and a companion, an ally and a possession that are
		// not valiant (no ally and no possession is valiant in print).  A valiant companion lies in the discard
		// pile and two more in the draw deck, the first of them under the two cards that are to be drawn first.
		scn.MoveCardsToHand(rider1, guard1, eomer1, swordsman, aldor, spear, rider2);
		scn.MoveCardsToDiscard(guard2, runner3);
		scn.MoveCardsToTopOfDeck(guard3, eomer2, runner2, runner1);

		// The Rider is a [rohan] Man for the Spear to go on, so each card that is not valiant could be played.
		scn.FreepsPlayCard(rider1);
		assertTrue(scn.FreepsPlayAvailable(spear));
		assertTrue(scn.FreepsPlayAvailable(aldor));
		assertTrue(scn.FreepsPlayAvailable(rider2));

		assertTrue(scn.FreepsActionAvailable(tomb));
		assertEquals(2, scn.GetTwilight());     // the Rider
		assertEquals(6, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(runner1, scn.GetFreepsTopOfDeck());

		scn.FreepsUseCardAction(tomb);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard1, eomer1, swordsman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rider2, aldor, spear, guard2, guard3, eomer2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard1);

		// The Guard is played, for his 2, and one card is drawn: the hand is one fewer and one more.
		assertInZone(Zone.FREE_CHARACTERS, guard1);
		assertEquals(4, scn.GetTwilight());     // 2 + 2 for the Household Guard
		assertInZone(Zone.HAND, runner1);
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(6, scn.GetFreepsHandCount());
		assertEquals(runner2, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// There is no limit.  A valiant companion of another culture.
		assertTrue(scn.FreepsActionAvailable(tomb));
		scn.FreepsUseCardAction(tomb);
		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer1, swordsman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rider2, aldor, spear, runner1));
		scn.FreepsChooseCard(swordsman);
		assertInZone(Zone.FREE_CHARACTERS, swordsman);
		assertEquals(6, scn.GetTwilight());     // 4 + 2 for the Lorien Swordsman
		assertInZone(Zone.HAND, runner2);
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(6, scn.GetFreepsHandCount());

		// Eomer is the last valiant character in hand, so he is not asked for.
		assertTrue(scn.FreepsActionAvailable(tomb));
		scn.FreepsUseCardAction(tomb);
		assertInZone(Zone.FREE_CHARACTERS, eomer1);
		assertEquals(9, scn.GetTwilight());     // 6 + 3 for Eomer
		// The card drawn is the other Eomer, a valiant character in hand again, and he cannot be played.
		assertInZone(Zone.HAND, eomer2);
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(6, scn.GetFreepsHandCount());

		assertFalse(scn.FreepsActionAvailable(tomb));
		assertInZone(Zone.HAND, rider2, aldor, spear);
		assertInZone(Zone.DISCARD, guard2);
		assertInZone(Zone.DECK, guard3);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilAValiantCharacterWhoCanBePlayedIsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var eomer1 = scn.GetFreepsCard("eomer1");
		var eomer2 = scn.GetFreepsCard("eomer2");
		var swordsman = scn.GetFreepsCard("swordsman");
		var rider1 = scn.GetFreepsCard("rider1");
		var rider2 = scn.GetFreepsCard("rider2");
		var aldor = scn.GetFreepsCard("aldor");
		var spear = scn.GetFreepsCard("spear");
		var runner1 = scn.GetFreepsCard("runner1");
		var runner2 = scn.GetFreepsCard("runner2");
		var runner3 = scn.GetFreepsCard("runner3");
		// Eomer is unique: with one copy in play the copy in hand cannot be played.
		scn.MoveCompanionsToTable(eomer1);

		scn.MoveCardsToHand(rider1);
		scn.StartGame();
		scn.SkipToSite(3);

		var tomb = scn.GetCurrentSite();

		// In hand: a companion, an ally and a possession that are not valiant, and a valiant companion who cannot
		// be played.  Valiant companions who could be played lie in the discard pile and on top of the draw deck.
		scn.MoveCardsToHand(rider1, rider2, aldor, spear, eomer2);
		scn.MoveCardsToDiscard(guard2, swordsman);
		scn.MoveCardsToTopOfDeck(runner3, runner2, runner1, guard3, guard1);

		scn.FreepsPlayCard(rider1);
		assertTrue(scn.FreepsPlayAvailable(rider2));
		assertTrue(scn.FreepsPlayAvailable(aldor));
		assertTrue(scn.FreepsPlayAvailable(spear));
		assertFalse(scn.FreepsPlayAvailable(eomer2));
		assertFalse(scn.FreepsActionAvailable(tomb));

		// A Household Guard comes into hand, and after the next legal play the Tomb's action is there.
		scn.MoveCardsToHand(guard1);
		scn.FreepsPlayCard(aldor);
		assertTrue(scn.FreepsActionAvailable(tomb));
		assertEquals(3, scn.GetTwilight());     // 2 for the Rider + 1 for Aldor
		assertEquals(guard3, scn.GetFreepsTopOfDeck());

		// The Guard is the only valiant character who can be played, so he is not asked for.
		scn.FreepsUseCardAction(tomb);
		assertInZone(Zone.FREE_CHARACTERS, guard1);
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.HAND, guard3, eomer2, rider2, spear);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckTheActionIsStillOfferedAndPlaysTheCharacterToDrawNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var eomer1 = scn.GetFreepsCard("eomer1");
		var eomer2 = scn.GetFreepsCard("eomer2");
		var swordsman = scn.GetFreepsCard("swordsman");
		var rider1 = scn.GetFreepsCard("rider1");
		var rider2 = scn.GetFreepsCard("rider2");
		var aldor = scn.GetFreepsCard("aldor");
		var spear = scn.GetFreepsCard("spear");
		var runner1 = scn.GetFreepsCard("runner1");
		var runner2 = scn.GetFreepsCard("runner2");
		var runner3 = scn.GetFreepsCard("runner3");

		scn.MoveCardsToHand(rider1);
		scn.StartGame();
		scn.SkipToSite(3);

		var tomb = scn.GetCurrentSite();

		// Two Household Guards and a Rider in hand, and every other card in the discard pile: nothing to draw.
		scn.MoveCardsToHand(rider1, guard1, guard2);
		scn.MoveCardsToDiscard(guard3, eomer1, eomer2, swordsman, rider2, aldor, spear, runner1, runner2, runner3);

		scn.FreepsPlayCard(rider1);
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(tomb));

		scn.FreepsUseCardAction(tomb);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard1, guard2));
		scn.FreepsChooseCard(guard1);

		assertInZone(Zone.FREE_CHARACTERS, guard1);
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, guard2);
		assertTrue(scn.FreepsActionAvailable(tomb));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
