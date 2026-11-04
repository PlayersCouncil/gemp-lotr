package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_078_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fire", "11_78");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a sturdy companion (vitality 4) to take archery wounds
					// Corps of Harad: [men] Man, vitality 3; fierce only at a battleground, and this path has none
					put("corps", "11_73");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, vitality 4; his abilities
					// are used only on purpose
					put("ted", "18_75");
					// Goblin Marksman: a [moria] archer and nothing else, so the minion archery total starts at 1
					put("marksman", "1_176");
					put("soldier", "4_254");    // Southron Soldier: a [raider] Man; Southron and ambush (2) only
					// Countless Companies: a [men] condition in the support area; nothing stacked on it, so its
					// Shadow ability is not offered
					put("companies", "12_58");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElevatedFireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Elevated Fire
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 7
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: <b>Toil 2</b>. <helper>(For each [men] character you exert when playing this, its twilight cost is -2.)</helper><br>Spot a [men] minion to make the minion archery total +3.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("fire");

		assertEquals("Elevated Fire", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.TOIL));
		assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(7, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAMenManMakesTheMinionArcheryTotalPlusThreeForTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fire = scn.GetShadowCard("fire");
		var corps = scn.GetShadowCard("corps");
		var marksman = scn.GetShadowCard("marksman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(fire);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(corps, marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.ShadowPlayAvailable(fire));
		scn.ShadowPlayCard(fire);
		// The pool pays in full: toil asks for none, and none is exerted.
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		// 1 for the Marksman + 3
		assertEquals(4, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(corps));
		assertInDiscard(fire);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// Still in force as the four archery wounds are placed, and gone with the phase.
		scn.PassArcheryActions();
		assertEquals(4, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(frodo);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(1, scn.GetShadowArcheryTotal());
	}

	@Test
	public void SpottingAMenMinionWhoIsNoManMakesTheMinionArcheryTotalPlusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fire = scn.GetShadowCard("fire");
		var ted = scn.GetShadowCard("ted");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(fire);

		scn.StartGame();
		scn.MoveMinionsToTable(ted, marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.ShadowPlayAvailable(fire));
		scn.ShadowPlayCard(fire);
		scn.ShadowDeclineChoosing();

		assertEquals(4, scn.GetShadowArcheryTotal());
		assertInDiscard(fire);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAMenMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fire = scn.GetShadowCard("fire");
		var marksman = scn.GetShadowCard("marksman");
		var soldier = scn.GetShadowCard("soldier");
		var companies = scn.GetShadowCard("companies");
		scn.MoveCardsToHand(fire);

		scn.StartGame();
		// A [men] card that is no minion, a [moria] minion and a Man minion of [raider].
		scn.MoveCardsToSupportArea(companies);
		scn.MoveMinionsToTable(marksman, soldier);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();

		assertInHand(fire);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(fire));
		assertEquals(1, scn.GetShadowArcheryTotal());
	}

	@Test
	public void ToilTakesTwoOffItsCostForEachMenCharacterExertedAsItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fire = scn.GetShadowCard("fire");
		var corps = scn.GetShadowCard("corps");
		var ted = scn.GetShadowCard("ted");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(fire);

		scn.StartGame();
		scn.MoveMinionsToTable(corps, ted, marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		// 7 - 2 - 2 is the least it can cost, with both [men] minions exerted; the [moria] Marksman does not count.
		// Toil 1 would want 5, and toil 3 (or the Marksman counted) would let 2 do.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(fire));
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(fire));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(corps));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(marksman));

		scn.ShadowPlayCard(fire);
		// The pool is 2 short of 7: one exertion is owed, and the second is the player's to make or not.
		// Toil 1 would owe two.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(corps, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(marksman));

		scn.ShadowChooseCard(ted);
		assertEquals(0, scn.GetWoundsOn(corps));
		assertEquals(1, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(marksman));
		// 5 - (7 - 2)
		assertEquals(0, scn.GetTwilight());
		assertEquals(4, scn.GetShadowArcheryTotal());
		assertInDiscard(fire);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
