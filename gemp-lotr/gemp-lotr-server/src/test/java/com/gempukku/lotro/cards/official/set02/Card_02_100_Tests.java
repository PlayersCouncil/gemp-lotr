package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_100_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fearing", "2_100");

					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, vitality 4; a skirmish ability used only on purpose
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, vitality 2; a skirmish ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit, vitality 4; an assignment ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, vitality 4; a maneuver ability used only on purpose

					// Search cards and trackers, of two cultures and kinds each, and a card of each kind beside them with neither keyword.
					put("spies", "2_92");       // Spies of Mordor: [sauron] search condition, cost 1
					put("reach", "1_137");      // Saruman's Reach: [isengard] search event, cost 0
					put("footsoldier", "4_187");// Uruk Foot Soldier: [isengard] Uruk-hai tracker, cost 1, site 5
					put("ambusher", "1_261");   // Orc Ambusher: [sauron] Orc tracker, cost 1 (its roaming discount is for minions played beside him in play)
					put("seeking", "1_276");    // Seeking Its Master: [sauron] condition, no search, cost 1
					put("parry", "1_132");      // Parry: [isengard] event, no search, cost 2
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, no tracker, cost 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FearingtheWorstStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Fearing the Worst
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Stealth</b>. To play, exert 2 Hobbits. Plays to your support area.
		 * 	The twilight cost of each search card and each tracker is +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fearing");

		assertEquals("Fearing the Worst", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.STEALTH));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayExertsTwoHobbitsOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fearing = scn.GetFreepsCard("fearing");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(fearing);

		scn.StartGame();
		assertInHand(fearing);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.FreepsPlayAvailable(fearing));

		scn.FreepsPlayCard(fearing);
		// Hobbits of two cultures, companions and an ally; not a [shire] companion who is no Hobbit, nor a Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol, aragorn));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(pippin, bounder);

		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, fearing);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileOnlyOneHobbitCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fearing = scn.GetFreepsCard("fearing");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, smeagol, aragorn);
		scn.MoveCardsToHand(fearing, bounder);

		scn.StartGame();
		// Frodo, vitality 4 with 3 wounds, is exhausted: Pippin is the one Hobbit who can exert, with two
		// companions who are no Hobbits able to exert beside him.
		scn.AddWoundsToChar(frodo, 3);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsExhausted(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertInHand(fearing);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fearing));

		// A second Hobbit able to exert comes into play, and it can be played.
		scn.FreepsPlayCard(bounder);
		assertInZone(Zone.SUPPORT, bounder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(fearing));
	}

	@Test
	public void TheTwilightCostOfEachSearchCardAndEachTrackerIsPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fearing = scn.GetFreepsCard("fearing");
		var pippin = scn.GetFreepsCard("pippin");
		var spies = scn.GetShadowCard("spies");
		var reach = scn.GetShadowCard("reach");
		var footsoldier = scn.GetShadowCard("footsoldier");
		var ambusher = scn.GetShadowCard("ambusher");
		var seeking = scn.GetShadowCard("seeking");
		var parry = scn.GetShadowCard("parry");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(fearing);
		scn.MoveCardsToHand(spies, reach, footsoldier, ambusher, seeking, parry, savage);

		scn.StartGame();
		assertEquals(1, scn.GetTwilightCost(spies));
		assertEquals(0, scn.GetTwilightCost(reach));
		assertEquals(1, scn.GetTwilightCost(footsoldier));
		assertEquals(1, scn.GetTwilightCost(ambusher));
		assertEquals(1, scn.GetTwilightCost(seeking));
		assertEquals(2, scn.GetTwilightCost(parry));
		assertEquals(2, scn.GetTwilightCost(savage));

		// Frodo and Pippin are the two Hobbits, and exert without being asked.
		scn.FreepsPlayCard(fearing);
		assertInZone(Zone.SUPPORT, fearing);

		assertEquals(3, scn.GetTwilightCost(spies));
		assertEquals(2, scn.GetTwilightCost(reach));
		assertEquals(3, scn.GetTwilightCost(footsoldier));
		assertEquals(3, scn.GetTwilightCost(ambusher));
		assertEquals(1, scn.GetTwilightCost(seeking));
		assertEquals(2, scn.GetTwilightCost(parry));
		assertEquals(2, scn.GetTwilightCost(savage));

		// The raised cost is the one paid.
		scn.SetTwilight(10);
		scn.FreepsPass();
		// 10 + 2 companions + 2 for Ettenmoors
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(footsoldier);
		// 1 printed + 2 + 2 roaming at site 2
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, footsoldier);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
