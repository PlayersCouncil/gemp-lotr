package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_177_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "4_177");

					// Uglúk, Servant of Saruman: the [isengard] Uglúk, vitality 3; his roaming discount is for [isengard]
					// trackers, and his bonuses want 2 or 3 of them (none is laid)
					put("ugluk", "4_176");
					// Uglúk, Ugly Fellow: the [uruk-hai] Uglúk, vitality 3; his text answers the start of his own skirmish
					put("ugly", "15_172");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai who is no Uglúk; damage +1 its whole text
					put("sentry", "10_84");     // Cirith Ungol Sentry: a [sauron] Uruk-hai, cost 2, site 6; damage +1 its whole text
					put("forger", "3_56");      // Isengard Forger: an [isengard] Orc, no Uruk-hai; a Shadow ability used only on purpose

					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc, cost 6, site 4; no text
					put("troop2", "1_177");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, cost 2; no text
					// Uruk-hai Rampage: an [isengard] condition, cost 0, playable here (to play, spot an Uruk-hai); its
					// Response answers a skirmish won
					put("rampage", "1_159");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UgluksSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uglúk's Sword
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 3
		 * Game Text: Bearer must be an Uruk-hai.<br><b>Shadow:</b> If bearer is Uglúk, exert it and discard a minion from hand to play a minion. Its twilight cost is -3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("Uglúk's Sword", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAnUrukHaiOfEitherCultureAndNotOnAnOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var ugluk = scn.GetShadowCard("ugluk");
		var sentry = scn.GetShadowCard("sentry");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCardsToHand(sword);
		assertInHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(ugluk, sentry, forger);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(sword));
		scn.ShadowPlayCard(sword);
		assertTrue(scn.ShadowHasCardChoiceAvailable(ugluk, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sentry);

		assertAttachedTo(sword, sentry);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BorneByUglukItExertsHimAndDiscardsAMinionToPlayAMinionAtThreeLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var ugluk = scn.GetShadowCard("ugluk");
		var savage = scn.GetShadowCard("savage");
		var troop1 = scn.GetShadowCard("troop1");
		var brigand = scn.GetShadowCard("brigand");
		var sentry = scn.GetShadowCard("sentry");
		var rampage = scn.GetShadowCard("rampage");
		scn.MoveCardsToHand(troop1, brigand, sentry, rampage);
		assertInHand(troop1, brigand, sentry, rampage);

		scn.StartGame();
		scn.MoveMinionsToTable(ugluk, savage);
		scn.AttachCardsTo(ugluk, sword);
		assertAttachedTo(sword, ugluk);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(10, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(ugluk));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.ShadowActionAvailable(sword));
		scn.ShadowUseCardAction(sword);

		// The discard: minions of two cultures and races; not the condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop1, brigand, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(rampage));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(brigand);
		assertInDiscard(brigand);
		// Uglúk, the bearer, exerts; the other Uruk-hai does not.
		assertEquals(1, scn.GetWoundsOn(ugluk));
		assertEquals(0, scn.GetWoundsOn(savage));

		// The play: either minion left in hand; not the condition, which could be played.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop1, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(rampage));
		scn.ShadowChooseCard(troop1);
		assertInZone(Zone.SHADOW_CHARACTERS, troop1);
		// 10 - (6 printed + 2 roaming at site 2 - 3)
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A minion played from hand afterwards pays its whole cost: 5 - (2 printed + 2 roaming).
		scn.ShadowPlayCard(sentry);
		assertInZone(Zone.SHADOW_CHARACTERS, sentry);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BorneByTheUrukHaiUglukTheAbilityIsOfferedAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var ugly = scn.GetShadowCard("ugly");
		var troop1 = scn.GetShadowCard("troop1");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(troop1, brigand);
		assertInHand(troop1, brigand);

		scn.StartGame();
		scn.MoveMinionsToTable(ugly);
		scn.AttachCardsTo(ugly, sword);
		assertAttachedTo(sword, ugly);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(ugly));
		assertTrue(scn.ShadowActionAvailable(sword));
		scn.ShadowUseCardAction(sword);
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop1, brigand));
		scn.ShadowChooseCard(brigand);

		// The Troop, the one minion left in hand, is played: 10 - (6 + 2 roaming - 3).
		assertEquals(1, scn.GetWoundsOn(ugly));
		assertInDiscard(brigand);
		assertInZone(Zone.SHADOW_CHARACTERS, troop1);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BorneByAnotherUrukHaiWithUglukInPlayTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var ugluk = scn.GetShadowCard("ugluk");
		var savage = scn.GetShadowCard("savage");
		var troop1 = scn.GetShadowCard("troop1");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(troop1, brigand);
		assertInHand(troop1, brigand);

		scn.StartGame();
		scn.MoveMinionsToTable(ugluk, savage);
		scn.AttachCardsTo(savage, sword);
		assertAttachedTo(sword, savage);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// The Savage could exert, Uglúk could too, and the hand could pay; the bearer is not Uglúk.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(ugluk));
		assertFalse(scn.ShadowActionAvailable(sword));
	}

	@Test
	public void WithUglukExhaustedTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var ugluk = scn.GetShadowCard("ugluk");
		var troop1 = scn.GetShadowCard("troop1");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(troop1, brigand);
		assertInHand(troop1, brigand);

		scn.StartGame();
		scn.MoveMinionsToTable(ugluk);
		scn.AttachCardsTo(ugluk, sword);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(ugluk, 2);
		assertAttachedTo(sword, ugluk);
		assertEquals(2, scn.GetWoundsOn(ugluk));
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(sword));
	}

	@Test
	public void WithOneMinionInHandTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var ugluk = scn.GetShadowCard("ugluk");
		var troop1 = scn.GetShadowCard("troop1");
		var rampage = scn.GetShadowCard("rampage");
		scn.MoveCardsToHand(troop1, rampage);
		assertInHand(troop1, rampage);

		scn.StartGame();
		scn.MoveMinionsToTable(ugluk);
		scn.AttachCardsTo(ugluk, sword);
		assertAttachedTo(sword, ugluk);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// The one minion could be discarded or played, not both; the condition is neither.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(ugluk));
		assertTrue(scn.ShadowPlayAvailable(troop1));
		assertFalse(scn.ShadowActionAvailable(sword));
	}

	@Test
	public void WithThePoolOneShortOfTheMinionAtThreeLessTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var ugluk = scn.GetShadowCard("ugluk");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCardsToHand(troop1, troop2);
		assertInHand(troop1, troop2);

		scn.StartGame();
		scn.MoveMinionsToTable(ugluk);
		scn.AttachCardsTo(ugluk, sword);
		assertAttachedTo(sword, ugluk);
		scn.FreepsPass();
		// A Troop played by the Sword costs 6 + 2 roaming at site 2 - 3 = 5.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(sword));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(sword));
		scn.ShadowUseCardAction(sword);
		scn.ShadowChooseCard(troop2);

		assertInDiscard(troop2);
		assertInZone(Zone.SHADOW_CHARACTERS, troop1);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
