package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_207_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("powers", "11_207");
					// Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl, vitality 2; her text weakens only bearers of a [wraith] condition
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl; his bonuses need 3 burdens
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] Orc and no Nazgûl, vitality 2; +4 and fierce only at 2 wounds on the Ring-bearer
					put("hound", "7_192");
					// Goblin Patrol Troop: a minion with no text, four of them to fill the draw deck the cards are drawn from
					put("troop1", "1_177");
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("troop4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DarkPowersStrengthenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Dark Powers Strengthen
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Toil 2</b>. <helper>(For each [wraith] character you exert when playing this, its twilight cost is -2.)</helper><br>Spot a Nazgûl to draw 3 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("powers");

		assertEquals("Dark Powers Strengthen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.TOIL));
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(6, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAWraithNazgulDrawsThreeCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var powers = scn.GetShadowCard("powers");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(powers);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.FreepsPass();
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();

		int deck = scn.GetShadowDeckCount();
		// The deck holds more than 3, so a draw of 4 would show.
		assertTrue(deck > 3);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(powers));
		scn.ShadowPlayCard(powers);
		// The pool pays in full: toil asks for none.
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertInDiscard(powers);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(deck - 3, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingANazgulWhoIsNotWraithDrawsThreeCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var powers = scn.GetShadowCard("powers");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(powers);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		scn.FreepsPass();
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();

		int deck = scn.GetShadowDeckCount();
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(powers));
		scn.ShadowPlayCard(powers);

		assertInDiscard(powers);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(deck - 3, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithAWraithMinionWhoIsNoNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var powers = scn.GetShadowCard("powers");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(powers);

		scn.StartGame();
		scn.MoveMinionsToTable(hound);
		assertInHand(powers);
		scn.FreepsPass();
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();

		assertInHand(powers);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(powers));
		assertEquals(1, scn.GetShadowHandCount());
	}

	@Test
	public void ToilTakesTwoOffItsCostForEachWraithCharacterExertedAsItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var powers = scn.GetShadowCard("powers");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(powers);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, hound, khamul);
		scn.FreepsPass();

		// 6 - 2 - 2 is the least it can cost, with both [wraith] minions exerted; the [raider] Khamûl does not count.
		// Toil 1 would want 4, and toil 3 (or Khamûl counted) would let 1 do.
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(powers));
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(powers));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertEquals(0, scn.GetWoundsOn(khamul));
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(powers);
		// The pool is 2 short of 6: one exertion is owed, and the second is the player's to make or not.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul));

		scn.ShadowChooseCard(hound);
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetWoundsOn(hound));
		assertEquals(0, scn.GetWoundsOn(khamul));
		// 4 - (6 - 2)
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(powers);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(deck - 3, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
