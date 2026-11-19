package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_12_014_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stalwart", "12_14");

					put("gimli", "1_13");         // Gimli, Son of Gloin: Dwarf companion; a skirmish ability used only on purpose
					put("farin", "1_11");         // Farin, Dwarven Emissary: Dwarf companion of another title
					put("grimir", "1_17");        // Grimir, Dwarven Elder: Dwarf ally; a fellowship ability used only on purpose
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers sites: site 1, Eastemnet Gullies 4_325, is no plains; site 2, Eastfold 4_331, is a plains.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stalwart", "12_14");

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a companion, held in hand
					put("armor", "1_8");          // Dwarven Armor: a Free Peoples possession, held in hand
					put("fury", "1_4");           // Battle Fury: a Free Peoples event, held in hand
					put("runner", "1_178");       // Goblin Runner: a Shadow card, held in hand
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers sites with Westemnet Plains 4_342 (a plains and a sanctuary) for site 3, so that the fellowship
	// moves from two plains sites in one turn: Eastfold 4_331 and then Westemnet Plains.
	protected VirtualTableScenario GetTwoPlainsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stalwart", "12_14");

					put("armor", "1_8");          // Dwarven Armor: a Free Peoples possession, held in hand
					put("runner", "1_178");       // Goblin Runner: a Shadow card, held in hand
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site3", "4_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StalwartSupportStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Stalwart Support
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each Dwarf gains <b>muster</b>. <helper>(At the start of the regroup phase, you may discard a card from hand to draw a card.)</helper><br>Each time the fellowship moves from a plains site, discard a card from your hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stalwart");

		assertEquals("Stalwart Support", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachDwarfCompanionAndAllyGainsMusterAndACompanionWhoIsNoDwarfDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stalwart = scn.GetFreepsCard("stalwart");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, farin, aragorn);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();

		assertFalse(scn.HasKeyword(gimli, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(farin, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(grimir, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.MUSTER));

		scn.MoveCardsToSupportArea(stalwart);
		assertInZone(Zone.SUPPORT, stalwart);

		assertTrue(scn.HasKeyword(gimli, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(farin, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(grimir, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.MUSTER));
	}

	@Test
	public void MovingFromAPlainsSiteDiscardsACardOfThePlayersChoiceFromHisHandAndMovingToOneDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var stalwart = scn.GetFreepsCard("stalwart");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var fury = scn.GetFreepsCard("fury");
		var runner = scn.GetFreepsCard("runner");
		var shadowArmor = scn.GetShadowCard("armor");
		var shadowRunner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(stalwart);

		scn.StartGame();
		scn.SetFreepsHand(aragorn, armor, fury, runner);
		scn.SetShadowHand(shadowArmor, shadowRunner);
		assertInZone(Zone.SUPPORT, stalwart);
		assertInZone(Zone.HAND, aragorn, armor, fury, runner, shadowArmor, shadowRunner);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		// From Eastemnet Gullies, no plains, to Eastfold, a plains: nothing is discarded.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		// From Eastfold: the player chooses one card of his own hand, of any kind.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, armor, fury, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(armor);

		assertInZone(Zone.DISCARD, armor);
		assertInZone(Zone.HAND, aragorn, fury, runner);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		// The Shadow player's hand is not his: none of it is discarded (his reconcile in the regroup phase has
		// drawn him more cards, so his hand is not read by its size).
		assertInZone(Zone.HAND, shadowArmor, shadowRunner);
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMoveFromAPlainsSiteDiscardsAgainASanctuaryPlainsIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoPlainsScenario();

		var stalwart = scn.GetFreepsCard("stalwart");
		var armor = scn.GetFreepsCard("armor");
		var runner = scn.GetFreepsCard("runner");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(stalwart);
		scn.SetFreepsHand(armor, runner);
		assertInZone(Zone.SUPPORT, stalwart);
		assertInZone(Zone.HAND, armor, runner);
		assertEquals(2, scn.GetFreepsHandCount());
		var discard = scn.GetFreepsDiscardCount();

		// From Eastfold, a plains, to Westemnet Plains.
		scn.FreepsPass();
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, runner));
		scn.FreepsChooseCard(runner);
		assertInZone(Zone.DISCARD, runner);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(discard + 1, scn.GetFreepsDiscardCount());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// From Westemnet Plains, a plains and a sanctuary, again: the one card left goes with no question.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertInZone(Zone.DISCARD, armor);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(discard + 2, scn.GetFreepsDiscardCount());
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
