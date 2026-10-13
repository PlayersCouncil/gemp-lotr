package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_047_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stone", "9_47");
					// Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl, vitality 2; her text needs a [wraith]
					// condition on a companion.
					put("lemenya", "1_232");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, vitality 2; answers a weapon played on him
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text

					// Morgul Hunter: [sauron] Orc, cost 5, site number 6; strength +1 for each companion.
					put("hunter", "1_256");
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, cost 3, site number 6; Damage +1
					// Morgul Hound: [wraith] Orc, cost 2, site number 4; a bonus at 2 wounds on the Ring-bearer.
					put("hound", "7_192");
					// Strength Born of Fear: a [sauron] condition (support area), cost 1; a skirmish ability.
					put("fear", "1_278");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IthilStoneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Ithil Stone
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Support Area
		 * Game Text: To play, exert a [wraith] or [sauron] minion.
		 * At the start of each of your Shadow phases, you may draw a card.
		 * <b>Shadow:</b> Discard this artifact to play a [sauron] minion. Its twilight cost is -3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stone");

		assertEquals("Ithil Stone", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayExertsAWraithOrSauronMinionOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetShadowCard("stone");
		var lemenya = scn.GetShadowCard("lemenya");
		var chieftain = scn.GetShadowCard("chieftain");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, chieftain, troop);
		scn.SetShadowHand(stone);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(stone));
		scn.ShadowPlayCard(stone);

		// A [wraith] Nazgûl and a [sauron] Orc are offered; a [moria] Orc is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(lemenya));
		scn.ShadowChooseCard(lemenya);

		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.SUPPORT, stone);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileEveryWraithAndSauronMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetShadowCard("stone");
		var lemenya = scn.GetShadowCard("lemenya");
		var chieftain = scn.GetShadowCard("chieftain");
		var troop = scn.GetShadowCard("troop");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, chieftain, troop);
		// Vitality 2 with 1 wound: exhausted.  The [moria] Orc could exert.
		scn.AddWoundsToChar(lemenya, 1);
		scn.AddWoundsToChar(chieftain, 1);
		scn.SetShadowHand(stone, guard);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(stone));

		// One legal play later a [sauron] minion can exert, and the artifact can be played.
		scn.ShadowPlayCard(guard);
		assertTrue(scn.ShadowPlayAvailable(stone));
		scn.ShadowPlayCard(stone);

		// The Guard is the only minion that can exert, and exerts unasked.
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.SUPPORT, stone);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtTheStartOfTheShadowPhaseTheShadowPlayerMayDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetShadowCard("stone");
		var hunter = scn.GetShadowCard("hunter");
		scn.MoveCardsToSupportArea(stone);

		scn.StartGame();
		scn.SetShadowDrawDeck(hunter, scn.GetShadowCard("guard"));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDrawDeck().size());

		scn.FreepsPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(stone));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetShadowHandCount());
		assertInHand(hunter);
		assertEquals(1, scn.GetShadowDrawDeck().size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheDrawMayBeDeclinedAndIsNotOfferedAtTheStartOfTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetShadowCard("stone");
		scn.MoveCardsToSupportArea(stone);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SetShadowDrawDeck(scn.GetShadowCard("hunter"), scn.GetShadowCard("guard"));
		assertEquals(0, scn.GetShadowHandCount());

		scn.FreepsPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(stone));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDrawDeck().size());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPass();

		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(stone));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDrawDeck().size());
	}

	@Test
	public void ShadowActionDiscardsTheStoneToPlayASauronMinionFromHandForThreeLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetShadowCard("stone");
		var hunter = scn.GetShadowCard("hunter");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToSupportArea(stone);

		scn.StartGame();
		scn.SetShadowHand(hunter, guard, hound, fear);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowDeclineOptionalTrigger();
		// 10 set + 1 companion + 2 for Ettenmoors
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		assertInZone(Zone.SUPPORT, stone);
		assertInHand(hunter);
		assertTrue(scn.ShadowActionAvailable(stone));
		scn.ShadowUseCardAction(stone);
		assertInDiscard(stone);

		// Two [sauron] minions of two races are offered; a [wraith] minion and a [sauron] condition, each of
		// which the pool could pay for, are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hunter, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, fear));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hunter);

		assertInZone(Zone.SHADOW_CHARACTERS, hunter);
		// 13 less (5 - 3) and 2 roaming: the Hunter's site number is 6.
		assertEquals(9, scn.GetTwilight());
		assertInHand(guard, hound, fear);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionIsNotOfferedUntilThePoolCoversTheMinionLessThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetShadowCard("stone");
		var hunter = scn.GetShadowCard("hunter");
		scn.MoveCardsToSupportArea(stone);

		scn.StartGame();
		scn.SetShadowHand(hunter);
		scn.FreepsPass();
		scn.ShadowDeclineOptionalTrigger();

		// The Hunter costs 5 - 3 and 2 roaming: 4.  At 3 (enough for a discount of 4) the action is not offered.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(stone));

		// At 4 (too little for a discount of 2) it is.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, stone);
		assertInHand(hunter);
		assertTrue(scn.ShadowActionAvailable(stone));
		scn.ShadowUseCardAction(stone);

		assertInDiscard(stone);
		assertInZone(Zone.SHADOW_CHARACTERS, hunter);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
