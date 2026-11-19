package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.TwilightCostModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_92_022_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("frost", "1_135");      // Saruman's Frost: [isengard] weather condition, cost 2; to play, exert an [isengard] minion; plays on a site
					put("urgency", "19_17");    // Urgency: [isengard] weather condition, cost 0; plays to the support area with nothing else to pay
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion, cost 2, vitality 3; Damage +1 and nothing else
					// Cards that are nearly a weather condition
					put("storm", "1_130");      // No Ordinary Storm: [isengard] support-area condition, cost 1, and no weather
					put("wisp", "101_28");      // Just a Wisp of Cloud: [isengard] weather, a Shadow event, cost 0: discard a Free Peoples card from hand to play Saruman from the draw deck or discard pile
					put("saruman", "3_69");     // Saruman, Servant of the Eye: [isengard] minion, cost 4, site 4: left in the draw deck for the Wisp to find
					put("sword", "1_299");      // Hobbit Sword: a Free Peoples card for the Wisp to discard
					// More of the draw deck
					put("filler1", "1_152");    // Uruk Shaman
					put("filler2", "1_153");    // Uruk Slayer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_22", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("frost", "1_135");      // Saruman's Frost: [isengard] weather condition, cost 2; to play, exert an [isengard] minion; plays on a site
					put("urgency", "19_17");    // Urgency: [isengard] weather condition, cost 0; plays to the support area with nothing else to pay
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion, cost 2, vitality 3; Damage +1 and nothing else
					// Cards that are nearly a weather condition
					put("storm", "1_130");      // No Ordinary Storm: [isengard] support-area condition, cost 1, and no weather
					put("wisp", "101_28");      // Just a Wisp of Cloud: [isengard] weather, a Shadow event, cost 0: discard a Free Peoples card from hand to play Saruman from the draw deck or discard pile
					put("saruman", "3_69");     // Saruman, Servant of the Eye: [isengard] minion, cost 4, site 4: left in the draw deck for the Wisp to find
					put("sword", "1_299");      // Hobbit Sword: a Free Peoples card for the Wisp to discard
					// More of the draw deck
					put("filler1", "1_152");    // Uruk Shaman
					put("filler2", "1_153");    // Uruk Slayer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_22"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_22
		 * Type: MetaSite
		 * Intensity: 1
		 * Game Text: Shadow: Play a weather condition to draw a card; its twilight cost is -2.
		 */

		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_22", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getIntensity());
	}

	@Test
	public void PlayWeatherConditionWithDiscountAndDraw() throws DecisionResultInvalidException, CardNotFoundException {
		// Frost costs 2, discount of 2 means it costs 0.
		// Playing it should also draw a card.

		var scn = GetShadowScenario();

		var frost = scn.GetShadowCard("frost");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCardsToHand(frost);
		scn.MoveMinionsToTable(savage);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.SHADOW);

		int handBefore = scn.GetShadowHandCount();
		int deckBefore = scn.GetShadowDeckCount();
		// 1 companion + 2 for site 2
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.HAND, frost);

		var mod = scn.GetShadowCard("mod");
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseAnyCard(); //Any site

		// Frost is played like any other card: onto a site, its own exertion paid, and for 2 - 2.
		assertInZone(Zone.ATTACHED, frost);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(3, scn.GetTwilight());

		// Drew a card after playing
		assertEquals(handBefore, scn.GetShadowHandCount());
		// handBefore - 1 (played frost) + 1 (drew card) = handBefore
		assertEquals(deckBefore - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOwnerGated() throws DecisionResultInvalidException, CardNotFoundException {
		// Freeps owns the modifier; Shadow should still be able to use it

		var scn = GetFreepsScenario();

		var frost = scn.GetShadowCard("frost");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCardsToHand(frost);
		scn.MoveMinionsToTable(savage);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.SHADOW);

		var mod = scn.GetFreepsCard("mod");
		int deckBefore = scn.GetShadowDeckCount();
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowActionAvailable(mod));

		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseAnyCard(); //Any site

		// Frost is played and it is the Shadow player, who used the action, who draws.
		assertInZone(Zone.ATTACHED, frost);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(deckBefore - 1, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithNoWeatherConditionInHandThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var frost = scn.GetShadowCard("frost");
		var storm = scn.GetShadowCard("storm");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCardsToHand(frost, storm);

		scn.StartGame();
		scn.FreepsPass();

		// A condition that is no weather and could be played, and a weather condition that cannot: there is no
		// [isengard] minion to exert for it.
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(storm));
		assertFalse(scn.ShadowPlayAvailable(frost));
		assertFalse(scn.ShadowActionAvailable(mod));

		// With an Uruk Savage on the table Frost can be played, and the action is there.
		scn.MoveMinionsToTable(savage);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(frost));
		assertTrue(scn.ShadowActionAvailable(mod));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OffersEachWeatherConditionInHandAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var frost = scn.GetShadowCard("frost");
		var urgency = scn.GetShadowCard("urgency");
		var storm = scn.GetShadowCard("storm");
		var wisp = scn.GetShadowCard("wisp");
		var sword = scn.GetShadowCard("sword");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCardsToHand(frost, urgency, storm, wisp, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// 5 set + 1 companion + 2 for site 2
		assertEquals(8, scn.GetTwilight());
		int deckBefore = scn.GetShadowDeckCount();
		assertEquals(5, scn.GetShadowHandCount());
		// A condition that is no weather and a weather card that is no condition, each of which could be played
		assertTrue(scn.ShadowPlayAvailable(storm));
		assertTrue(scn.ShadowPlayAvailable(wisp));
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// The two weather conditions, one played on a site and one to the support area, and nothing else
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(frost, urgency));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(storm, wisp, sword));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCard(urgency);

		// Urgency costs 0 and is played for 0; nothing is exerted for it.
		assertInZone(Zone.SUPPORT, urgency);
		assertInZone(Zone.HAND, frost);
		assertEquals(8, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(savage));
		// One card is drawn: 5 - 1 played + 1
		assertEquals(5, scn.GetShadowHandCount());
		assertEquals(deckBefore - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheConditionIsPlayedForTwoLessAndTheActionIsOfferedAsSoonAsThePoolHoldsThat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var frost = scn.GetShadowCard("frost");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCardsToHand(frost);
		// No weather condition costs more than 2, and a cost of 2 less 2 reads 0 under any larger discount as
		// well.  So each condition is made twilight cost +2, as Rohirrim Road 7_332 does for a turn: Frost costs 4.
		scn.ApplyAdHocModifier(new TwilightCostModifier(null, CardType.CONDITION, null, new ConstantEvaluator(2)));

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();

		assertEquals(4, scn.GetTwilightCost(frost));

		// With 1 in the pool, one short of 4 - 2, the action is not offered.
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(mod));

		// With 2 it is, and Frost still cannot be played in the ordinary way, for all of its 4.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(frost));
		assertTrue(scn.ShadowActionAvailable(mod));

		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCard(scn.GetCurrentSite());

		assertInZone(Zone.ATTACHED, frost);
		assertEquals(0, scn.GetTwilight());
		// The card drawn
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
