package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_012_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("city", "13_12");
					// Shake Off the Shadow: another [elven] support-area condition of the same player; its abilities
					// are used only on purpose
					put("shake", "8_13");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6; +3 only against a Nazgûl
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: Elf companion, strength 5; +2 only at sites 6 to 8
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion, strength 8
					// Elrond, Lord of Rivendell: Elf ally, home site 3, strength 8; heals allies of site 3 at the start
					// of his owner's turns and has a fellowship ability used only on purpose
					put("elrond", "1_40");
					put("savage1", "1_151");    // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					put("savage2", "1_151");
					put("savage3", "1_151");
					// Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability that exerts him, so not
					// usable once he is wounded
					put("soldier", "1_271");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CityoftheTreesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: City of the Trees
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: At the end of your turn, discard this from play if the fellowship did not move more than once this turn.<br>At the start of each skirmish involving an Elf and a wounded minion, you may draw a card.
		*/

		var scn = GetScenario();

		var city = scn.GetFreepsCard("city");

		assertEquals("City of the Trees", city.getBlueprint().getTitle());
		assertNull(city.getBlueprint().getSubtitle());
		assertFalse(city.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, city.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, city.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, city.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(city, Keyword.SUPPORT_AREA));
		assertEquals(1, city.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardedAtTheEndOfATurnInWhichTheFellowshipMovedOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var city = scn.GetFreepsCard("city");
		var shake = scn.GetFreepsCard("shake");
		scn.MoveCardsToSupportArea(city, shake);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, city, shake);

		// No minion: from the Shadow phase the game goes to the regroup phase, after the one move.
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetMoveCount());
		assertInZone(Zone.SUPPORT, city, shake);

		// The regroup move declined, the turn ends: the City is discarded, the other condition stays.
		scn.FreepsChooseToStay();
		scn.PassReconcile(scn.P1());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertInDiscard(city);
		assertInZone(Zone.SUPPORT, shake);
	}

	@Test
	public void StaysAfterATurnOfTwoMovesAndIsDiscardedAfterTheNextTurnOfOneMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var city = scn.GetFreepsCard("city");
		var shake = scn.GetFreepsCard("shake");
		scn.MoveCardsToSupportArea(city, shake);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, city, shake);

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(2, scn.GetMoveCount());

		// Two moves this turn: the City stays at its end.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertInZone(Zone.SUPPORT, city, shake);

		// The other player's turn, and then this player's next one, of one move.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.SUPPORT, city, shake);
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetMoveCount());
		assertInZone(Zone.SUPPORT, city);

		// The two moves of the turn before do not count: discarded.
		scn.FreepsChooseToStay();
		scn.PassReconcile(scn.P1());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertInDiscard(city);
		assertInZone(Zone.SUPPORT, shake);
	}

	@Test
	public void AtTheStartOfEachSkirmishOfAnElfAndAWoundedMinionItMayDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var city = scn.GetFreepsCard("city");
		var shake = scn.GetFreepsCard("shake");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var savage3 = scn.GetShadowCard("savage3");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(arwen, haldir, aragorn);

		scn.StartGame();
		// Site 3 is Elrond's home, where he may be assigned to a skirmish: the City and Elrond go down at site 2,
		// after the skip, so that the City meets no turn's end on the way.
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(city);
		scn.MoveAlliesToTable(elrond);
		scn.MoveMinionsToTable(savage1, savage2, savage3, soldier);
		scn.AddWoundsToChar(savage2, 1);
		scn.AddWoundsToChar(savage3, 1);
		scn.AddWoundsToChar(soldier, 1);
		// A card to draw: the skips' reconciles have taken most of the draw deck into hand.
		scn.MoveCardsToTopOfDeck(shake);
		assertEquals(shake, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, shake);
		assertInZone(Zone.SUPPORT, city);
		assertEquals(0, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertEquals(1, scn.GetWoundsOn(savage3));
		assertEquals(1, scn.GetWoundsOn(soldier));

		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{arwen, savage1},
				new PhysicalCardImpl[]{haldir, savage2},
				new PhysicalCardImpl[]{elrond, soldier},
				new PhysicalCardImpl[]{aragorn, savage3});

		// An Elf and an unwounded minion: nothing at the start of the skirmish, nor once Arwen (6) has beaten and
		// wounded the Uruk Savage (5).
		scn.FreepsResolveSkirmish(arwen);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(city));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(city));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// An Elf companion and a wounded [isengard] Uruk-hai.
		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();
		scn.FreepsResolveSkirmish(haldir);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(city));
		scn.FreepsAcceptOptionalTrigger();
		assertInHand(shake);
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// An Elf ally and a wounded [sauron] Orc: offered again, and declined.
		hand = scn.GetFreepsHandCount();
		deck = scn.GetFreepsDeckCount();
		scn.FreepsResolveSkirmish(elrond);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(city));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(hand, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// A companion who is no Elf and a wounded minion.
		scn.FreepsResolveSkirmish(aragorn);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(city));
		assertEquals(hand, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
