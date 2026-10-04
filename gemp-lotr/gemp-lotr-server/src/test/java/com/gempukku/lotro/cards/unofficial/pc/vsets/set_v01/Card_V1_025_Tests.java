package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_025_Tests
{
	/*
	 * LEDGER  101_25  Crebain Flock
	 * Sources: the definition's gametext and printed fields (PC card); CR 4.2 names this card nowhere
	 *
	 * P1 Play       [+]             Played from hand for (2), and (2) more while roaming, to the minions in play.
	 * C1 Modifier   [+ - F S]       This minion is twilight cost -1 for each companion you can spot over 3.
	 * C2 Activated  [+ - B F L Z]   Shadow: Spot 2 wounded companions (or spot Saruman) to take an [isengard]
	 *                               weather into hand from your draw deck or discard pile (limit once per phase).
	 *
	 * Findings: F-0011
	 * Notes:
	 *   C2 has no C or C-: what stands before "to" is "spot", which is a requirement and not a cost
	 *   (CR 4.2 "spot"), so nothing is paid and the unmet requirement is the - check.
	 *   C1's cap is zero: CR 4.2 "modifier" changes a value that ends below zero to zero, and CR 4.2 "roaming"
	 *   makes the penalty an additional two on top of that cost, so with six companions the Flock still
	 *   takes (2) from the pool at a site before its own.
	 *   C2's L follows CR 4.2 "limit": the limit is per copy and does not span phases.
	 *   Every weather card in the game is [isengard], so the "[isengard]" in C2 is shown with a [moria]
	 *   card that an ad hoc modifier makes weather.
	 *   "Saruman" in C2 is any card of that title: the Saruman spotted is an [uruk-hai] one.
	 *   F-0011 / G-0001: ShadowActionAvailable(card) goes by the card's name and cannot tell two copies apart,
	 *   so the per-copy limit test counts the offered actions itself until the rig can.
	 */

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("flock", "101_25");
					put("flock2", "101_25");
					put("saruman", "3_69");     // Saruman, Servant of the Eye: [isengard] minion, not weather; an assignment ability never used here
					put("agent", "12_144");     // Saruman, Agent of the Dark Lord: [uruk-hai] minion, cost 4, site 4, lurker; a skirmish ability and a trigger on a companion's death, neither met here
					put("snows", "1_138");      // Saruman's Snows: [isengard] weather condition
					put("chill", "1_134");      // Saruman's Chill: [isengard] weather condition
					put("frost", "1_135");      // Saruman's Frost: [isengard] weather condition
					put("storm", "2_48");       // Wizard Storm: [isengard] weather condition
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] minion, no text at all

					put("guard1", "1_7");       // Dwarf Guard: companion, vitality 2; its only text is a requirement to play it
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
					put("guard5", "1_7");
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: ally, vitality 2; a maneuver ability never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CrebainFlockStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Crebain Flock
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Crow
		 * Strength: 2
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: This minion is twilight cost -1 for each companion you can spot over 3.
		 * 	Shadow: Spot 2 wounded companions (or spot Saruman) to take an [isengard] weather into hand from your draw deck or discard pile (limit once per phase).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("flock");

		assertEquals("Crebain Flock", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.CROW, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	// P1: +; C1: - F
	@Test
	public void CostsItsPrintedTwoWithThreeCompanionsAndAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flock = scn.GetShadowCard("flock");
		scn.MoveCardsToHand(flock);
		scn.MoveCompanionsToTable("guard1", "guard2");
		scn.MoveCardsToSupportArea(scn.GetFreepsCard("thrarin"));

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 5 set + 3 companions + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.HAND, flock);
		assertEquals(1, scn.GetShadowHandCount());

		scn.ShadowPlayCard(flock);

		assertInZone(Zone.SHADOW_CHARACTERS, flock);
		assertEquals(0, scn.GetShadowHandCount());
		// Frodo and two Dwarf Guards: no companion over 3, and the ally is not a companion.
		// 10 - 2 to play - 2 roaming (a site 4 minion played at site 2)
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// C1: + S
	@Test
	public void CostsOneLessWithFourCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flock = scn.GetShadowCard("flock");
		scn.MoveCardsToHand(flock);
		scn.MoveCompanionsToTable("guard1", "guard2", "guard3");

		scn.StartGame();
		scn.SetTwilight(4);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 4 set + 4 companions + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());

		scn.ShadowPlayCard(flock);

		assertInPlay(flock);
		// One companion over 3: 10 - (2 - 1) to play - 2 roaming at site 2
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// C1: S
	@Test
	public void CostsTwoLessWithFiveCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flock = scn.GetShadowCard("flock");
		scn.MoveCardsToHand(flock);
		scn.MoveCompanionsToTable("guard1", "guard2", "guard3", "guard4");

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 3 set + 5 companions + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());

		scn.ShadowPlayCard(flock);

		assertInPlay(flock);
		// Two companions over 3: 10 - (2 - 2) to play - 2 roaming at site 2
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// C1: S
	@Test
	public void CostDoesNotGoBelowZeroWithSixCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flock = scn.GetShadowCard("flock");
		scn.MoveCardsToHand(flock);
		scn.MoveCompanionsToTable("guard1", "guard2", "guard3", "guard4", "guard5");

		scn.StartGame();
		scn.SetTwilight(2);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 2 set + 6 companions + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());

		scn.ShadowPlayCard(flock);

		assertInPlay(flock);
		// Three companions over 3: 2 - 3 is below zero, so the cost is 0. 10 - 0 to play - 2 roaming at site 2
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// C2: + - B F Z
	@Test
	public void SpottingTwoWoundedCompanionsTakesAnIsengardWeatherFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flock = scn.GetShadowCard("flock");
		var flock2 = scn.GetShadowCard("flock2");
		var snows = scn.GetShadowCard("snows");
		var chill = scn.GetShadowCard("chill");
		var frost = scn.GetShadowCard("frost");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.MoveCardsToSupportArea(thrarin);
		scn.MoveCardsToHand(flock2);
		// Snows, Chill, Saruman and the Troop stay in the draw deck; Frost gives the discard pile a weather too.
		scn.MoveCardsToDiscard(frost);
		// A weather card that is not [isengard]
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, troop, null, Keyword.WEATHER));

		scn.StartGame();
		scn.MoveMinionsToTable(flock);
		scn.AddWoundsToChar(guard1, 1);
		scn.AddWoundsToChar(thrarin, 1);
		scn.SetTwilight(5);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 5 set + 3 companions + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.HasKeyword(troop, Keyword.WEATHER));
		// One wounded companion, a wounded ally, two unwounded companions and no Saruman
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertFalse(scn.ShadowActionAvailable(flock));

		// A second companion is wounded. Playing the other Flock is only there to have the game ask again.
		scn.AddWoundsToChar(guard2, 1);
		scn.ShadowPlayCard(flock2);
		assertTrue(scn.ShadowActionAvailable(flock));

		assertEquals(0, scn.GetShadowHandCount());
		int deckBefore = scn.GetShadowDeckCount();
		assertInZone(Zone.DECK, snows, chill);

		scn.ShadowUseCardAction(flock);
		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("draw deck");
		scn.ShadowDismissRevealedCards();

		assertTrue(scn.ShadowHasCardChoiceAvailable(snows, chill));
		// An [isengard] minion that is not weather, and a weather minion that is not [isengard]
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman, troop));
		scn.ShadowChooseCardBPFromSelection(snows);

		assertInZone(Zone.HAND, snows);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(deckBefore - 1, scn.GetShadowDeckCount());
		assertInZone(Zone.DECK, chill);
		assertInZone(Zone.DISCARD, frost);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// C2: + - B
	@Test
	public void SpottingSarumanIsEnoughWithNoWoundedCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flock = scn.GetShadowCard("flock");
		// "Saruman" is a title: this one is not [isengard], unlike the Flock.
		var saruman = scn.GetShadowCard("agent");
		var chill = scn.GetShadowCard("chill");
		var frost = scn.GetShadowCard("frost");
		scn.MoveCardsToHand(saruman);
		scn.MoveCardsToDiscard(frost);

		scn.StartGame();
		scn.MoveMinionsToTable(flock);
		scn.SetTwilight(3);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 3 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(6, scn.GetTwilight());
		// No wounded companion and no Saruman
		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		assertFalse(scn.ShadowActionAvailable(flock));

		scn.ShadowPlayCard(saruman);
		assertInPlay(saruman);
		// 6 - 4 to play Saruman - 2 roaming at site 2
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(flock));

		assertEquals(0, scn.GetShadowHandCount());
		scn.ShadowUseCardAction(flock);
		scn.ShadowChoose("draw deck");
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(chill);

		assertInZone(Zone.HAND, chill);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// C2: B F L Z
	@Test
	public void EachCopyTakesFromTheDiscardPileOnceInAPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flock = scn.GetShadowCard("flock");
		var flock2 = scn.GetShadowCard("flock2");
		var frost = scn.GetShadowCard("frost");
		var storm = scn.GetShadowCard("storm");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(guard1, guard2);
		// Frost and Storm are the weather in the discard pile; Snows and Chill stay in the draw deck.
		scn.MoveCardsToDiscard(frost, storm, saruman, troop);
		// A weather card that is not [isengard]
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, troop, null, Keyword.WEATHER));

		scn.StartGame();
		scn.MoveMinionsToTable(flock, flock2);
		scn.AddWoundsToChar(guard1, 1);
		scn.AddWoundsToChar(guard2, 1);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(troop, Keyword.WEATHER));
		// One action for each copy. (ShadowActionAvailable(card) goes by the card's name, so it cannot tell the copies apart.)
		assertEquals(2, scn.ShadowGetAvailableActions().stream().filter(x -> x.contains("Use Crebain Flock")).count()); // G-0001
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDiscardCount());

		scn.ShadowUseCardAction(flock);
		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("discard pile");

		assertTrue(scn.ShadowHasCardChoiceAvailable(frost, storm));
		// An [isengard] minion that is not weather, and a weather minion that is not [isengard]
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman, troop));
		scn.ShadowChooseCard(frost);

		assertInZone(Zone.HAND, frost);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDiscardCount());
		// The first copy has had its once; the limit of the second copy is its own (CR 4.2 "limit").
		// One action is left, and it is the second copy's: ShadowUseCardAction goes by the card itself.
		assertEquals(1, scn.ShadowGetAvailableActions().stream().filter(x -> x.contains("Use Crebain Flock")).count()); // G-0001

		scn.ShadowUseCardAction(flock2);
		scn.ShadowChoose("discard pile");
		// Storm is the only [isengard] weather left in the discard pile, so it is taken without asking.
		assertInZone(Zone.HAND, storm);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertInZone(Zone.DISCARD, saruman, troop);
		assertFalse(scn.ShadowActionAvailable(flock));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// C2: L
	@Test
	public void CanBeUsedAgainInASecondShadowPhaseOfTheSameTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flock = scn.GetShadowCard("flock");
		var frost = scn.GetShadowCard("frost");
		var storm = scn.GetShadowCard("storm");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.MoveCardsToDiscard(frost, storm);

		scn.StartGame();
		scn.MoveMinionsToTable(flock);
		scn.AddWoundsToChar(guard1, 1);
		scn.AddWoundsToChar(guard2, 1);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowActionAvailable(flock));

		scn.ShadowUseCardAction(flock);
		scn.ShadowChoose("discard pile");
		scn.ShadowChooseCard(frost);
		assertInZone(Zone.HAND, frost);
		assertFalse(scn.ShadowActionAvailable(flock));

		// Nobody is assigned to the Flock, so it is still in play when the fellowship moves again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInPlay(flock);
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertEquals(1, scn.GetWoundsOn(guard2));
		assertTrue(scn.ShadowActionAvailable(flock));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
