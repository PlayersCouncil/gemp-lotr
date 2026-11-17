package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_140_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sauron", "13_140");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetMinionsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sauron", "13_140");
					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, vitality 3, no text at all
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("slayer", "1_153");     // Uruk Slayer: [isengard] Uruk-hai, vitality 2; a skirmish ability only
					put("runner", "1_178");     // Goblin Runner: vitality 1, so it can never exert
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion, the character who is no minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDiscountScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sauron", "13_140");
					put("troop1", "1_177");     // Goblin Patrol Troop: a minion of vitality 3 with no text at all
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SauronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Sauron, Dark Lord of Mordor
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 16
		 * Type: Minion
		 * Subtype: Maia
		 * Strength: 24
		 * Vitality: 5
		 * Site Number: 6
		 * Game Text: <b>Damage +1</b>. <b>Fierce</b>.<br>When you play Sauron, you may exert any number of minions. For each minion you exert, Sauron is twilight cost -X, where X is the current region number.
		*/

		var scn = GetScenario();

		var sauron = scn.GetFreepsCard("sauron");

		assertEquals("Sauron", sauron.getBlueprint().getTitle());
		assertEquals("Dark Lord of Mordor", sauron.getBlueprint().getSubtitle());
		assertTrue(sauron.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, sauron.getBlueprint().getSide());
		assertEquals(Culture.SAURON, sauron.getBlueprint().getCulture());
		assertEquals(CardType.MINION, sauron.getBlueprint().getCardType());
		assertEquals(Race.MAIA, sauron.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(sauron, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sauron, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(sauron, Keyword.FIERCE));
		assertEquals(16, sauron.getBlueprint().getTwilightCost());
		assertEquals(24, sauron.getBlueprint().getStrength());
		assertEquals(5, sauron.getBlueprint().getVitality());
		assertEquals(6, sauron.getBlueprint().getSiteNumber());
	}

	@Test
	public void ExertingAMinionAsHeIsPlayedTakesTheRegionNumberOffHisCostAndAPoolOneShortThenPlaysHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscountScenario();

		var sauron = scn.GetShadowCard("sauron");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCardsToHand(sauron);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		// 14, and for the move 1 companion and the 2 of Ettenmoors
		scn.SetTwilight(14);
		scn.FreepsPass();

		// Site 2 is in region 1.  16 to play + 2 roaming (his site number is 6) = 18: the pool is one short,
		// and one minion exerted takes 1 off.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(17, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.ShadowPlayAvailable(sauron));

		scn.ShadowPlayCard(sauron);
		// "Any number of minions", but no fewer than the pool needs: he cannot be played for less than he
		// costs by exerting nobody.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop1, troop2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(troop2);

		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(1, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, sauron);
		assertEquals(0, scn.GetWoundsOn(sauron));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThePoolOneShortAndNoMinionAbleToExertHeIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscountScenario();

		var sauron = scn.GetShadowCard("sauron");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCardsToHand(sauron);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		// Vitality 3 with 2 wounds: the one minion in play is exhausted, and can be spotted but not exerted.
		scn.AddWoundsToChar(troop1, 2);
		// 14, and for the move 1 companion and the 2 of Ettenmoors
		scn.SetTwilight(14);
		scn.FreepsPass();

		// 16 to play + 2 roaming = 18, with no exertion to take anything off it.
		assertEquals(17, scn.GetTwilight());
		assertInZone(Zone.HAND, sauron);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(sauron));

		// One more in the pool, and he is offered at his whole cost.
		scn.SetTwilight(18);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(sauron));
		assertEquals(2, scn.GetWoundsOn(troop1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void InRegionOneEachMinionExertedTakesOneOffAndOnlyMinionsAbleToExertAreOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMinionsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var frodo = scn.GetRingBearer();

		var sauron = scn.GetShadowCard("sauron");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var slayer = scn.GetShadowCard("slayer");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(sauron);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2, slayer, runner);
		// Vitality 3 with 2 wounds: exhausted, so it cannot exert.
		scn.AddWoundsToChar(troop2, 2);
		// 26, and for the move 2 companions and the 2 of Ettenmoors
		scn.SetTwilight(26);
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(30, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(2, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertEquals(0, scn.GetWoundsOn(runner));

		scn.ShadowPlayCard(sauron);
		// Minions of two cultures and races are offered; one exhausted, one of vitality 1 and the companions
		// are not.  The pool can pay in full, so none need be chosen.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop1, slayer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop2, runner, aragorn, frodo));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(troop1, slayer);

		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertEquals(2, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// Site 2 is in region 1: 16 to play + 2 roaming - 2 x 1 = 16.  (The site number, 2, would take 4.)
		assertEquals(14, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, sauron);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningToExertLeavesTheMinionsAsTheyWereAndAnotherMinionPlayedAfterHimIsAskedNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMinionsScenario();

		var sauron = scn.GetShadowCard("sauron");
		var troop1 = scn.GetShadowCard("troop1");
		var troop3 = scn.GetShadowCard("troop3");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCardsToHand(sauron, troop3);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, slayer);
		// 27, and for the move 1 companion and the 2 of Ettenmoors
		scn.SetTwilight(27);
		scn.FreepsPass();

		assertEquals(30, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(slayer));

		scn.ShadowPlayCard(sauron);
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop1, slayer));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		// 16 to play + 2 roaming, and nothing taken off.
		assertEquals(12, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertInZone(Zone.SHADOW_CHARACTERS, sauron);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another minion, played while Sauron is in play with minions able to exert, is asked nothing and pays in
		// full: 6 to play + 2 roaming.
		scn.ShadowPlayCard(troop3);
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertInZone(Zone.SHADOW_CHARACTERS, troop3);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void InRegionTwoEachMinionExertedTakesTwoOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMinionsScenario();

		var sauron = scn.GetShadowCard("sauron");
		var troop1 = scn.GetShadowCard("troop1");
		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(sauron);
		scn.MoveMinionsToTable(troop1, slayer);
		// 23, and for the move 1 companion and the 6 of The Bridge of Khazad-dum
		scn.SetTwilight(23);
		scn.FreepsPass();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(30, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(slayer));

		scn.ShadowPlayCard(sauron);
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop1, slayer));
		scn.ShadowChooseCards(troop1, slayer);

		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(1, scn.GetWoundsOn(slayer));
		// Site 5 is in region 2: 16 to play + 2 roaming - 2 x 2 = 14.  (Region 1 would take 2 off, the site
		// number 10.)
		assertEquals(16, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, sauron);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
