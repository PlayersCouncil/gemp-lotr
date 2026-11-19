package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("durability", "12_4");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6; a skirmish ability used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, home 3F, strength 3; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose

					put("runner", "1_178");     // Goblin Runner: Orc, strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Pass of Caradhras 1_348 as site 4: a mountain (its Shadow ability needs an
	// [isengard] minion).  Thrarin's home, site 3, is Rivendell Valley 1_341, a sanctuary and no more.  No Dwarf ally
	// has a home that is a battleground or a mountain: each is of site 3 of the Fellowship path.  Aragorn fights the
	// skirmish; the Dwarves are in none.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("durability", "12_4");
					put("durability2", "12_4");
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion, strength 5, resistance 6; +2 while skirmishing an Orc
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, home 3F, strength 4; a maneuver ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8; a maneuver ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("spare", "1_177");      // a card to draw
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site4", "1_348");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path: site 4 is White Rocks 4_346, a battleground and no mountain (its Shadow number counts
	// mounted companions, of whom there are none).  Aragorn fights the skirmish.
	protected VirtualTableScenario GetBattlegroundScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("durability", "12_4");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8; a maneuver ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("spare", "1_177");      // a card to draw
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DurabilityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Durability
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a Dwarf strength +2. Then, if that Dwarf is at a battleground or mountain site, draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("durability");

		assertEquals("Durability", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AwayFromABattlegroundOrMountainADwarfIsStrengthPlus2ForThatSkirmishOnlyAndNothingIsDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var durability = scn.GetFreepsCard("durability");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(durability);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		assertInHand(durability);
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Ettenmoors, site 2: a plains, neither a battleground nor a mountain.
		assertEquals("Ettenmoors", scn.GetCurrentSite().getBlueprint().getTitle());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MOUNTAIN));
		// Not a Skirmish phase: not playable among the assignment actions.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(durability));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner}, new PhysicalCardImpl[]{aragorn, runner2});
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(3, scn.GetStrength(grimir));
		assertTrue(scn.FreepsPlayAvailable(durability));
		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(durability);
		// A Dwarf in the skirmish and a Dwarf in none are offered; companions who are no Dwarf are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(gimli);
		// 6 + 2
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(3, scn.GetStrength(grimir));
		assertEquals(8, scn.GetStrength(aragorn));
		assertInDiscard(durability);
		// Away from a battleground or mountain, nothing is drawn: the hand is one card (the event) smaller.
		assertEquals(hand - 1, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());

		// Still +2 at the next decision of the skirmish.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(gimli));

		// Gimli beats the Runner; in the next skirmish, a phase of its own, he is 6 again.
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtABattlegroundTheDwarfChosenDrawsACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundScenario();

		var durability = scn.GetFreepsCard("durability");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var spare = scn.GetFreepsCard("spare");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(durability);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInHand(durability);
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("White Rocks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MOUNTAIN));
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		assertInHand(durability);
		scn.MoveCardsToTopOfDeck(spare);
		scn.FreepsRefreshDecision();
		assertSame(spare, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, spare);
		int hand = scn.GetFreepsHandCount();
		assertEquals(6, scn.GetStrength(gimli));

		// Gimli is with the fellowship, at the battleground: 6 + 2, and the top card drawn, with nothing asked.
		scn.FreepsPlayCard(durability);
		assertEquals(8, scn.GetStrength(gimli));
		assertInDiscard(durability);
		assertInHand(spare);
		assertEquals(hand, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoDwarfInPlayItIsPlayedForNothingAndDrawsNothingAtAMountain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var durability = scn.GetFreepsCard("durability");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(durability);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInHand(durability);
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("Pass of Caradhras", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MOUNTAIN));
		scn.FreepsAssignAndResolve(aragorn, troop);
		assertEquals(8, scn.GetStrength(aragorn));
		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();
		// Nothing in the text is a cost or a requirement: the event can be played with no Dwarf to make stronger.
		assertTrue(scn.FreepsPlayAvailable(durability));

		scn.FreepsPlayCard(durability);
		assertInDiscard(durability);
		assertEquals(8, scn.GetStrength(aragorn));
		// No Dwarf was made stronger, so there is no "that Dwarf" at the mountain: nothing drawn.
		assertEquals(hand - 1, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAllyWhoseHomeIsNoMountainDrawsNothingWithTheFellowshipAtAMountain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var durability = scn.GetFreepsCard("durability");
		var durability2 = scn.GetFreepsCard("durability2");
		var farin = scn.GetFreepsCard("farin");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(farin, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(durability, durability2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInHand(durability, durability2);
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("Pass of Caradhras", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MOUNTAIN));
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		assertInHand(durability, durability2);
		var spare = scn.GetFreepsCard("spare");
		var spare2 = scn.GetFreepsCard("troop");
		scn.MoveCardsToTopOfDeck(spare2, spare);
		scn.FreepsRefreshDecision();
		assertSame(spare, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, spare, spare2);
		int hand = scn.GetFreepsHandCount();

		// The companion is with the fellowship, at the mountain: 5 + 2, and a card drawn.
		scn.FreepsPlayCard(durability);
		scn.FreepsChooseCard(farin);
		assertEquals(7, scn.GetStrength(farin));
		assertEquals(hand, scn.GetFreepsHandCount());
		assertInHand(spare);

		// Thrarin is at his home site, Rivendell Valley (CR 4.2 "ally"): 4 + 2, and nothing drawn.
		scn.ShadowPass();
		scn.FreepsPlayCard(durability2);
		scn.FreepsChooseCard(thrarin);
		assertEquals(6, scn.GetStrength(thrarin));
		assertEquals(hand - 1, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, spare2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
