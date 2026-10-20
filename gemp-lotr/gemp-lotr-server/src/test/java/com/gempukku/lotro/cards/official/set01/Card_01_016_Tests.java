package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("kingdom", "1_16");

					put("gimli", "1_13");         // Gimli, Son of Gloin: [dwarven] Dwarf companion, vitality 3
					put("guard", "1_7");          // Dwarf Guard: [dwarven] Dwarf companion, cost 1, vitality 2
					put("grimir", "1_17");        // Grimir, Dwarven Elder: [dwarven] Dwarf ally, vitality 3
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, vitality 4

					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, cost 6, no text
					put("worker", "3_62");        // Isengard Worker: [isengard] Orc, cost 2, a regroup ability
					put("taskmaster", "2_90");    // Orc Taskmaster: [sauron] Orc, cost 3, a Shadow ability used only on purpose
					put("wary", "18_93");         // Wary Orc: Orc of the [orc] culture; stronger only beside hunter companions
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, cost 2
					put("troll", "19_23");        // Troll of the Deep: a Troll of the [orc] culture, cost 6; to play, spot an [orc] Orc
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatestKingdomofMyPeopleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Greatest Kingdom of My People
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Tale</b>. Exert a Dwarf to play this condition. Plays to your support area.
		 * 	Each time your opponent plays an Orc, that player discards the top card of his or her draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("kingdom");

		assertEquals("Greatest Kingdom of My People", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedToTheSupportAreaByExertingADwarfCompanionOrAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(kingdom);
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.FreepsPlayAvailable(kingdom));

		scn.FreepsPlayCard(kingdom);
		// A Dwarf companion and a Dwarf ally; not Aragorn, a companion who is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(grimir);
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, kingdom);
		assertNotAttachedTo(kingdom, grimir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilADwarfIsAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(kingdom, guard);
		// Vitality 3 with 2 wounds: Gimli is exhausted. Aragorn could exert, but is no Dwarf.
		scn.AddWoundsToChar(gimli, 2);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(kingdom));

		scn.FreepsPlayCard(guard);
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.FreepsPlayAvailable(kingdom));

		// The Guard is the only Dwarf who can exert, so he is the one who does.
		scn.FreepsPlayCard(kingdom);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// 1 for the Guard + 0 for the condition
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, kingdom);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachOrcTheOpponentPlaysDiscardsTheTopCardOfHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var troop = scn.GetShadowCard("troop");
		var worker = scn.GetShadowCard("worker");
		// The Shadow player's own copies of four Free Peoples cards, to know his draw deck by.
		var first = scn.GetShadowCard("aragorn");
		var second = scn.GetShadowCard("gimli");
		var third = scn.GetShadowCard("guard");
		var bottom = scn.GetShadowCard("grimir");
		scn.MoveCardsToSupportArea(kingdom);
		scn.MoveCardsToHand(troop, worker);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(third, second, first);
		scn.MoveCardsToBottomOfDeck(bottom);
		scn.SetTwilight(30);

		int shadowDeck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertSame(first, scn.GetShadowTopOfDeck());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.FreepsPass();

		// A [moria] Orc.
		scn.ShadowPlayCard(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.DISCARD, first);
		assertInZone(Zone.DECK, second, third, bottom);
		assertSame(second, scn.GetShadowTopOfDeck());
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		// It is the opponent's deck, not the deck of the player whose condition it is.
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// An [isengard] Orc, in the same phase: it happens again.
		scn.ShadowPlayCard(worker);
		assertInZone(Zone.SHADOW_CHARACTERS, worker);
		assertInZone(Zone.DISCARD, first, second);
		assertInZone(Zone.DECK, third, bottom);
		assertSame(third, scn.GetShadowTopOfDeck());
		assertSame(bottom, scn.GetShadowBottomOfDeck());
		assertEquals(shadowDeck - 2, scn.GetShadowDeckCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionThatIsNoOrcDiscardsNothingThoughAnOrcIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var wary = scn.GetShadowCard("wary");
		var savage = scn.GetShadowCard("savage");
		var troll = scn.GetShadowCard("troll");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var first = scn.GetShadowCard("aragorn");
		var second = scn.GetShadowCard("gimli");
		scn.MoveCardsToSupportArea(kingdom);
		scn.MoveCardsToHand(savage, troll, taskmaster);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(second, first);
		scn.SetTwilight(30);
		// An Orc already on the table, which the Troll needs: it was not played while the condition looked on.
		scn.MoveMinionsToTable(wary);

		int shadowDeck = scn.GetShadowDeckCount();
		assertSame(first, scn.GetShadowTopOfDeck());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.FreepsPass();

		// An Uruk-hai, and a Troll whose culture is called [orc].
		scn.ShadowPlayCard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertSame(first, scn.GetShadowTopOfDeck());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(troll);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertSame(first, scn.GetShadowTopOfDeck());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A [sauron] Orc on the same board.
		scn.ShadowPlayCard(taskmaster);
		assertInZone(Zone.SHADOW_CHARACTERS, taskmaster);
		assertInZone(Zone.DISCARD, first);
		assertSame(second, scn.GetShadowTopOfDeck());
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
