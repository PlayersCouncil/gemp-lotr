package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_001_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aggression", "8_1");
					put("aggression2", "8_1");  // a second copy, to show which one is discarded

					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, no damage bonus; to play, spot a Dwarf
					put("guard2", "1_7");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, printed damage +1; a skirmish ability only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, no damage bonus of his own; a fellowship ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally; a maneuver ability only
					put("huorn", "9_25");       // Huorn: a [gandalf] companion who is damage +1 and no Dwarf; a response that needs an Ent
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, no Dwarf; a maneuver ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase

					// Goblin Patrol Troop: no text; held in hand for the initiative, or drawn
					put("filler1", "1_177");
					put("filler2", "1_177");
					put("filler3", "1_177");
					put("filler4", "1_177");
					put("filler5", "1_177");
					put("filler6", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AggressionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Aggression
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: While you have initiative, each Dwarf is <b>damage +1</b>.<br><b>Fellowship:</b> Spot a Dwarf who is damage +X to draw X cards. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aggression");

		assertEquals("Aggression", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhileTheFreePeoplesPlayerHasInitiativeEachDwarfIsDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aggression = scn.GetFreepsCard("aggression");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(aggression);
		scn.MoveCompanionsToTable(guard, gimli, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(guard2, scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));

		scn.StartGame();

		// 4 cards in hand: the Free Peoples player has initiative.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));
		// Printed damage +1, and +1 more.
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		// Aragorn is a companion who is no Dwarf.
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		// Playing the second Dwarf Guard takes the hand to 3 and the initiative away.
		scn.FreepsPlayCard(guard2);

		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard2, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipSpotsADwarfWhoIsDamagePlusXToDrawXCardsAndDiscardsTheCopyUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aggression = scn.GetFreepsCard("aggression");
		var aggression2 = scn.GetFreepsCard("aggression2");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var huorn = scn.GetFreepsCard("huorn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(aggression, aggression2);
		scn.MoveCompanionsToTable(gimli, huorn, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"),
				scn.GetFreepsCard("filler4"));
		assertInZone(Zone.SUPPORT, aggression, aggression2);

		scn.StartGame();

		// Initiative, and two copies: each Dwarf is +2 more.
		int deck = scn.GetFreepsDeckCount();
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(huorn, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(aggression));
		scn.FreepsUseCardAction(aggression);

		// A Dwarf, companion or ally; not Huorn, who is damage +1 and no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(huorn, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		// Gimli was damage +3: three cards, and the copy used is discarded.
		assertEquals(7, scn.GetFreepsHandCount());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertInDiscard(aggression);
		assertInZone(Zone.SUPPORT, aggression2);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// With one copy left, Grimir is damage +1: one card, the fourth of the phase.
		assertEquals(1, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		scn.FreepsUseCardAction(aggression2);
		scn.FreepsChooseCard(grimir);

		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(deck - 4, scn.GetFreepsDeckCount());
		assertInDiscard(aggression, aggression2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void XIsTheBonusTheDwarfHadWhenSpottedThoughTheDrawGivesTheInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aggression = scn.GetFreepsCard("aggression");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(aggression);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));
		assertInZone(Zone.SUPPORT, aggression);

		scn.StartGame();

		// 3 cards in hand: no initiative, and Gimli has his printed +1 only.
		int deck = scn.GetFreepsDeckCount();
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		scn.FreepsUseCardAction(aggression);

		// The one card drawn makes 4 in hand, and Gimli is damage +2 by then; X was 1.
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertInDiscard(aggression);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheOnlyDwarfHasNoDamageBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aggression = scn.GetFreepsCard("aggression");
		var grimir = scn.GetFreepsCard("grimir");
		var huorn = scn.GetFreepsCard("huorn");
		var filler4 = scn.GetFreepsCard("filler4");
		scn.MoveCardsToSupportArea(aggression);
		scn.MoveCompanionsToTable(huorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));

		scn.StartGame();

		// 3 cards in hand: Grimir is a Dwarf of no damage bonus; Huorn has one and is no Dwarf.
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(huorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(aggression));

		// A fourth card in hand: initiative, and Grimir is damage +1.
		scn.MoveCardsToHand(filler4);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(aggression));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aggression = scn.GetFreepsCard("aggression");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(aggression);
		scn.MoveCompanionsToTable(gimli);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(aggression));
	}
}
