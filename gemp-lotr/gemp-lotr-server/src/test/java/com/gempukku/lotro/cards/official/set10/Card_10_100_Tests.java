package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_100_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("speak", "10_100");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability only
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, vitality 2; damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3; no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability only
					put("song1", "8_112");      // Song of the Shire: [shire] support-area condition; fellowship and skirmish abilities
					put("song2", "8_112");
					put("trolls", "1_314");     // Stone Trolls: [shire] condition borne by a character; +3 against a Troll or Uruk-hai in a skirmish
					put("mallorn", "1_54");     // Mallorn-trees: [elven] support-area condition; a fellowship ability only
					put("gift", "3_19");        // Gift of the Evenstar: [elven] condition borne by Aragorn; a skirmish ability only
					put("toby", "1_305");       // Old Toby: [shire] support-area possession; a draw on its own play
					put("defense", "1_244");    // Desperate Defense of the Ring: [sauron] condition on a companion; answers his assignment
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SpeakNoMoretoMeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Speak No More to Me
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Exert a [sauron] minion to return a Free Peoples condition to its owner's hand (or 2 if both are [shire] conditions).
		*/

		var scn = GetScenario();

		var speak = scn.GetFreepsCard("speak");

		assertEquals("Speak No More to Me", speak.getBlueprint().getTitle());
		assertNull(speak.getBlueprint().getSubtitle());
		assertFalse(speak.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, speak.getBlueprint().getSide());
		assertEquals(Culture.SAURON, speak.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, speak.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(speak, Timeword.SHADOW));
		assertEquals(1, speak.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithOneShireConditionItExertsASauronMinionToReturnAFreePeoplesConditionToItsOwnersHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var speak = scn.GetShadowCard("speak");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var defense = scn.GetShadowCard("defense");
		var aragorn = scn.GetFreepsCard("aragorn");
		var song = scn.GetFreepsCard("song1");
		var mallorn = scn.GetFreepsCard("mallorn");
		var gift = scn.GetFreepsCard("gift");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(song, mallorn, toby);
		scn.AttachCardsTo(aragorn, defense, gift);
		scn.MoveCardsToHand(speak);
		assertInHand(speak);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, song, mallorn, toby);
		assertAttachedTo(defense, aragorn);
		assertAttachedTo(gift, aragorn);
		scn.MoveMinionsToTable(soldier, guard, troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(speak));
		scn.ShadowPlayCard(speak);

		// A [sauron] minion of either race; not the [moria] Orc, though he could exert.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guard);

		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));

		// One [shire] condition is not two, so a Free Peoples condition of either culture, in the support area or
		// borne: not the Free Peoples possession, nor the Shadow condition a companion bears.
		assertTrue(scn.ShadowHasCardChoiceAvailable(song, mallorn, gift));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(toby, defense));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(mallorn);

		assertInHand(mallorn);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, song, toby);
		assertAttachedTo(defense, aragorn);
		assertAttachedTo(gift, aragorn);
		assertInDiscard(speak);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreeShireConditionsExactlyTwoOfThemAreReturnedAndNoOtherCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var speak = scn.GetShadowCard("speak");
		var soldier = scn.GetShadowCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		var song1 = scn.GetFreepsCard("song1");
		var song2 = scn.GetFreepsCard("song2");
		var trolls = scn.GetFreepsCard("trolls");
		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(song1, song2, mallorn);
		scn.AttachCardsTo(aragorn, trolls);
		scn.MoveCardsToHand(speak);
		assertInHand(speak);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, song1, song2, mallorn);
		assertAttachedTo(trolls, aragorn);
		scn.MoveMinionsToTable(soldier);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetFreepsHandCount());
		scn.ShadowPlayCard(speak);
		// The only [sauron] minion exerts with no question.
		assertEquals(1, scn.GetWoundsOn(soldier));

		// Two [shire] conditions can be returned, so the parenthesis is what is done, with no choice of the lesser
		// return (CR 4.2 "effect"; Q-0035): exactly 2 of the three [shire] conditions, borne or in the support area.
		// Not the [elven] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(song1, song2, trolls));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(mallorn));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(song1, trolls);

		assertInHand(song1, trolls);
		assertEquals(2, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, song2, mallorn);
		assertInDiscard(speak);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithExactlyTwoShireConditionsBothAreReturnedWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var speak = scn.GetShadowCard("speak");
		var soldier = scn.GetShadowCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		var song = scn.GetFreepsCard("song1");
		var trolls = scn.GetFreepsCard("trolls");
		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(song, mallorn);
		scn.AttachCardsTo(aragorn, trolls);
		scn.MoveCardsToHand(speak);
		assertInHand(speak);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, song, mallorn);
		assertAttachedTo(trolls, aragorn);
		scn.MoveMinionsToTable(soldier);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetFreepsHandCount());
		scn.ShadowPlayCard(speak);
		assertEquals(1, scn.GetWoundsOn(soldier));

		// Both [shire] conditions go back, unasked; the [elven] one stays.
		assertInHand(song, trolls);
		assertEquals(2, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, mallorn);
		assertInDiscard(speak);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithItsOnlySauronMinionExhaustedItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var speak = scn.GetShadowCard("speak");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var song = scn.GetFreepsCard("song1");
		scn.MoveCardsToSupportArea(song);
		scn.MoveCardsToHand(speak);
		assertInHand(speak);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, song);
		// Vitality 2 with 1 wound: exhausted.  The [moria] Orc beside him could exert.
		scn.MoveMinionsToTable(guard, troop);
		scn.AddWoundsToChar(guard, 1);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(speak));
		assertInHand(speak);
		assertInZone(Zone.SUPPORT, song);
	}

	@Test
	public void WithNoFreePeoplesConditionItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var speak = scn.GetShadowCard("speak");
		var soldier = scn.GetShadowCard("soldier");
		var defense = scn.GetShadowCard("defense");
		var aragorn = scn.GetFreepsCard("aragorn");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(toby);
		scn.AttachCardsTo(aragorn, defense);
		scn.MoveCardsToHand(speak);
		assertInHand(speak);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, toby);
		assertAttachedTo(defense, aragorn);
		scn.MoveMinionsToTable(soldier);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetFreepsHandCount());
		// An event's target is no requirement (CR 4.2 "effect"): it is played, and its cost paid, for nothing.
		assertTrue(scn.ShadowPlayAvailable(speak));
		scn.ShadowPlayCard(speak);

		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, toby);
		assertAttachedTo(defense, aragorn);
		assertInDiscard(speak);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
