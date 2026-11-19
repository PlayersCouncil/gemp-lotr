package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_052_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("voice", "3_52");
					put("saruman", "3_68");     // Saruman, Keeper of Isengard: [isengard] Wizard minion, vitality 4; makes Uruk-hai fierce
					put("rabble", "4_33");      // Saruman, Rabble-rouser: [dunland] Wizard minion, vitality 4; nothing of his text reaches these tests
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 and nothing else
					put("follower", "17_38");   // Saruman, Servant of Sauron: [isengard] follower, no character; a skirmish-win trigger only

					put("frost", "1_135");      // Saruman's Frost: [isengard] weather condition, cost 2; to play, exert an [isengard] minion; plays on a site
					put("urgency", "19_17");    // Urgency: [isengard] weather condition, cost 0, plays to the support area
					put("caradhras", "1_124");  // Cruel Caradhras: [isengard] weather EVENT, cost 2
					put("armory", "1_157");     // Uruk-hai Armory: [isengard] condition that is no weather, support area
					put("snows", "1_138");      // Saruman's Snows: [isengard] weather condition, laid in the Free Peoples player's discard pile only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The King-block path with Rohirrim Road as site 1, whose Fellowship action makes each condition's twilight
	// cost +2 until the end of the turn, so that the size of the discount can be read above the floor of 0.
	protected VirtualTableScenario GetRoadScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("voice", "3_52");
					put("saruman", "3_68");     // Saruman, Keeper of Isengard: [isengard] Wizard minion, vitality 4
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 and nothing else
					put("frost", "1_135");      // Saruman's Frost: [isengard] weather condition, cost 2
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: the Elf who exerts for Rohirrim Road
				}},
				new HashMap<>()
				{{
					put("site1", "7_332");      // Rohirrim Road
					put("site2", "7_335");      // King's Tent: a Shadow ability that needs initiative, not used
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AFellVoiceontheAirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: A Fell Voice on the Air
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 	<b>Shadow:</b> Exert Saruman to play an [isengard] weather condition from your discard pile. Its twilight cost is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("voice");

		assertEquals("A Fell Voice on the Air", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowAbilityExertsSarumanToPlayAnIsengardWeatherConditionFromTheDiscardPileForTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var frost = scn.GetShadowCard("frost");
		var urgency = scn.GetShadowCard("urgency");
		var caradhras = scn.GetShadowCard("caradhras");
		var armory = scn.GetShadowCard("armory");
		var freepsSnows = scn.GetFreepsCard("snows");
		scn.MoveCardsToSupportArea(voice);
		scn.MoveCardsToDiscard(frost, urgency, caradhras, armory, freepsSnows);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage);
		assertInDiscard(frost, urgency, caradhras, armory, freepsSnows);
		scn.FreepsPass();

		// 1 companion + 2 for Ettenmoors
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.ShadowActionAvailable(voice));

		scn.ShadowUseCardAction(voice);
		assertEquals(1, scn.GetWoundsOn(saruman));
		// Two [isengard] weather conditions; not a weather event, a condition that is no weather, nor the Free
		// Peoples player's weather condition in his own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frost, urgency));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(caradhras, armory, freepsSnows));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(frost);

		// Saruman's Frost is played on a site, and asks its own cost: an [isengard] minion exerts. The Savage
		// pays it.
		var site = scn.GetCurrentSite();
		scn.ShadowChooseCard(site);
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, savage));
		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(saruman));

		assertAttachedTo(frost, site);
		// 2 - 2: the Frost is played for nothing
		assertEquals(3, scn.GetTwilight());
		assertInDiscard(urgency, caradhras, armory, freepsSnows);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheDunlandSarumanPaysToPlayAWeatherConditionThatGoesToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var rabble = scn.GetShadowCard("rabble");
		var urgency = scn.GetShadowCard("urgency");
		scn.MoveCardsToSupportArea(voice);
		scn.MoveCardsToDiscard(urgency);

		scn.StartGame();
		scn.MoveMinionsToTable(rabble);
		assertInDiscard(urgency);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(rabble));
		assertTrue(scn.ShadowActionAvailable(voice));

		// Urgency, the one weather condition there, is played unasked.
		scn.ShadowUseCardAction(voice);
		assertEquals(1, scn.GetWoundsOn(rabble));

		assertInZone(Zone.SUPPORT, urgency);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhileSarumanIsExhaustedThoughAnotherIsengardMinionCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var urgency = scn.GetShadowCard("urgency");
		scn.MoveCardsToSupportArea(voice);
		scn.MoveCardsToDiscard(urgency);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(saruman, 3);
		assertEquals(3, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInDiscard(urgency);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(voice));
		assertEquals(3, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInDiscard(urgency);
	}

	@Test
	public void TheSarumanFollowerCannotExertToPayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var follower = scn.GetShadowCard("follower");
		var savage = scn.GetShadowCard("savage");
		var urgency = scn.GetShadowCard("urgency");
		scn.MoveCardsToSupportArea(voice, follower);
		scn.MoveCardsToDiscard(urgency);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		assertInZone(Zone.SUPPORT, follower);
		assertInDiscard(urgency);
		scn.FreepsPass();

		// The one Saruman in play is a follower, which is no character and cannot exert.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(voice));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInDiscard(urgency);
	}

	@Test
	public void NotOfferedWithNoIsengardWeatherConditionInTheShadowPlayersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var caradhras = scn.GetShadowCard("caradhras");
		var armory = scn.GetShadowCard("armory");
		var freepsSnows = scn.GetFreepsCard("snows");
		scn.MoveCardsToSupportArea(voice);
		scn.MoveCardsToDiscard(caradhras, armory, freepsSnows);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage);
		assertInDiscard(caradhras, armory, freepsSnows);
		scn.FreepsPass();

		// The play is part of the price: with nothing it could play, the ability is not offered.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(voice));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertInDiscard(caradhras, armory, freepsSnows);
	}

	@Test
	public void NotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var saruman = scn.GetShadowCard("saruman");
		var urgency = scn.GetShadowCard("urgency");
		scn.MoveCardsToSupportArea(voice);
		scn.MoveCardsToDiscard(urgency);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		assertInDiscard(urgency);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(voice));
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(voice));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertInDiscard(urgency);
	}

	@Test
	public void TheWeatherConditionCostsExactlyTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRoadScenario();

		var voice = scn.GetShadowCard("voice");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var frost = scn.GetShadowCard("frost");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(voice);
		scn.MoveCardsToDiscard(frost);
		assertInDiscard(frost);

		scn.StartGame();
		var road = scn.GetCurrentSite();
		assertEquals(2, scn.GetTwilightCost(frost));
		assertEquals(0, scn.GetWoundsOn(arwen));
		scn.FreepsUseCardAction(road);
		assertEquals(1, scn.GetWoundsOn(arwen));
		// 2 printed + 2 for Rohirrim Road
		assertEquals(4, scn.GetTwilightCost(frost));

		scn.MoveMinionsToTable(saruman, savage);
		scn.FreepsPass();

		// 4 - 2 = 2: one short, and it is not offered.
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(voice));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(voice));
		scn.ShadowUseCardAction(voice);
		var site = scn.GetCurrentSite();
		scn.ShadowChooseCard(site);
		scn.ShadowChooseCard(savage);

		assertAttachedTo(frost, site);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(savage));
		// 2 - (4 - 2)
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
