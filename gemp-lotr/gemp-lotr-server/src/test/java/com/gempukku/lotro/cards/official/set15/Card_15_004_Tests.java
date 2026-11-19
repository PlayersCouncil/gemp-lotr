package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fortunes", "15_4");
					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, vitality 3; a skirmish ability only
					// Thrarin, Dwarven Smith: a Dwarf ally, vitality 2; a maneuver ability used only on purpose
					put("thrarin", "1_27");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, no Dwarf; a maneuver ability used only on purpose
					// Wish For Our Kinfolk: [dwarven] support-area condition whose Fellowship ability adds [dwarven] tokens to it
					put("wish", "8_8");
					// Run Until Found: [dwarven] support-area condition that takes a [dwarven] token as it is played; a
					// Skirmish ability removes it
					put("run", "18_2");
					put("run2", "18_2");
					// A Path Appointed: [gondor] support-area condition whose Fellowship ability adds [gondor] tokens to it
					put("path", "8_42");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 and no other text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1 (exhausted); its play trigger does nothing when cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheFortunesofBalinsFolkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: The Fortunes of Balin's Folk
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert a Dwarf to shuffle a [dwarven] condition with one or more [dwarven] tokens on it into your draw deck. Wound a minion for each [dwarven] token that was on that condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fortunes");

		assertEquals("The Fortunes of Balin's Folk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsADwarfToShuffleAConditionWithTokensInAndWoundsAMinionForEachToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortunes = scn.GetFreepsCard("fortunes");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var wish = scn.GetFreepsCard("wish");
		var run = scn.GetFreepsCard("run");
		var run2 = scn.GetFreepsCard("run2");
		var path = scn.GetFreepsCard("path");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(wish, run, run2, path);
		scn.MoveCardsToHand(fortunes);

		scn.StartGame();
		scn.AddTokensToCard(wish, 3);
		scn.AddTokensToCard(run, 1);
		scn.AddTokensToCard(path, 1);
		scn.MoveMinionsToTable(savage, troop, runner);
		assertEquals(3, scn.GetCultureTokensOn(wish));
		assertEquals(1, scn.GetCultureTokensOn(run));
		assertEquals(0, scn.GetCultureTokensOn(run2));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertInZone(Zone.SUPPORT, wish, run, run2, path);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		assertTrue(scn.FreepsPlayAvailable(fortunes));
		scn.FreepsPlayCard(fortunes);

		// The cost: a Dwarf, companion or ally; not a companion who is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, thrarin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(thrarin));

		// One [dwarven] condition with a [dwarven] token on it; not one with none, nor a [gondor] condition with a
		// [gondor] token.
		assertTrue(scn.FreepsHasCardChoiceAvailable(wish, run));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(run2, path));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(wish);

		assertInZone(Zone.DECK, wish);
		assertEquals(deck + 1, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.SUPPORT, run, run2, path);
		assertEquals(1, scn.GetCultureTokensOn(run));
		assertEquals(1, scn.GetCultureTokensOn(path));

		// Three wounds, one minion at a time, the same minion more than once (CR 4.2 "for each").  The Runner,
		// exhausted, is offered and killed by the wound; no companion is offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, aragorn));
		scn.FreepsChooseCard(runner);
		assertInDiscard(runner);
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		scn.FreepsChooseCard(savage);
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		scn.FreepsChooseCard(savage);

		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInDiscard(fortunes);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void OneTokenOnTheConditionChosenWoundsOneMinionWhateverIsOnAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortunes = scn.GetFreepsCard("fortunes");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var wish = scn.GetFreepsCard("wish");
		var run = scn.GetFreepsCard("run");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(wish, run);
		scn.MoveCardsToHand(fortunes);

		scn.StartGame();
		scn.AddTokensToCard(wish, 3);
		scn.AddTokensToCard(run, 1);
		scn.MoveMinionsToTable(savage, troop);
		assertEquals(3, scn.GetCultureTokensOn(wish));
		assertEquals(1, scn.GetCultureTokensOn(run));
		assertInZone(Zone.SUPPORT, wish, run);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsPlayCard(fortunes);

		// The ally is the one Dwarf, and exerts unasked.
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(run);
		assertInZone(Zone.DECK, run);
		assertEquals(3, scn.GetCultureTokensOn(wish));

		// One token was on that condition: one wound, though 3 more lie on the other.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInDiscard(fortunes);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotPlayableWhileTheOnlyDwarfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortunes = scn.GetFreepsCard("fortunes");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var wish = scn.GetFreepsCard("wish");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(wish);
		scn.MoveCardsToHand(fortunes);

		scn.StartGame();
		// Gimli is exhausted; Aragorn could exert, and is no Dwarf.
		scn.AddWoundsToChar(gimli, 2);
		scn.AddTokensToCard(wish, 3);
		scn.MoveMinionsToTable(savage);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetCultureTokensOn(wish));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInHand(fortunes);
		assertFalse(scn.FreepsPlayAvailable(fortunes));
	}

	@Test
	public void WithNoDwarvenConditionHoldingATokenTheEventIsStillPlayedAndOnlyTheDwarfExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortunes = scn.GetFreepsCard("fortunes");
		var gimli = scn.GetFreepsCard("gimli");
		var run2 = scn.GetFreepsCard("run2");
		var path = scn.GetFreepsCard("path");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(run2, path);
		scn.MoveCardsToHand(fortunes);

		scn.StartGame();
		scn.AddTokensToCard(path, 1);
		scn.MoveMinionsToTable(savage, troop);
		assertEquals(0, scn.GetCultureTokensOn(run2));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertInZone(Zone.SUPPORT, run2, path);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gimli));
		int deck = scn.GetFreepsDeckCount();

		assertTrue(scn.FreepsPlayAvailable(fortunes));
		scn.FreepsPlayCard(fortunes);

		// Played and paid for; nothing to shuffle in, and so no wound.  (The engine shuffles the deck all the same, as
		// a shuffle of no cards from play does: Ketura's rulings Q-0102 and Q-0112.)
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInZone(Zone.SUPPORT, run2, path);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInDiscard(fortunes);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
