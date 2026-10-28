package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_286_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fighter", "7_286");
					put("fighter2", "7_286");
					put("swordsman", "3_98");   // Orc Swordsman: [sauron] Orc, cost 3; strength +2 only while he bears a weapon
					// Cirith Ungol Guard: [sauron] Uruk-hai, a [sauron] minion who is no Orc; Damage +1 is its whole text
					put("uruk", "10_81");
					put("morgul", "7_199");     // Morgul Soldier: a [wraith] Orc; Nazgul are strength +1 (none in play)

					// Four Free Peoples cards to hold in hand: four cards give him the initiative by the rule.
					put("hand1", "1_89");       // Aragorn, Ranger of the North
					put("hand2", "1_97");       // Boromir, Son of Denethor
					put("hand3", "1_13");       // Gimli, Son of Gloin
					put("hand4", "1_50");       // Legolas, Greenleaf
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MordorFighterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Mordor Fighter
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: While you can spot a threat and another [sauron] Orc, the Shadow has initiative, regardless of the Free People player's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fighter");

		assertEquals("Mordor Fighter", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ASauronOrcPlayedBesideFighterWithAThreatGivesTheShadowInitiativeWithFourCardsInTheFreePeoplesHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"),
				scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));

		var fighter = scn.GetShadowCard("fighter");
		var swordsman = scn.GetShadowCard("swordsman");
		scn.MoveCardsToHand(swordsman);

		scn.StartGame();
		scn.MoveMinionsToTable(fighter);
		scn.AddThreats(1);
		scn.SetTwilight(10);
		scn.FreepsRefreshDecision();

		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.HAND, swordsman);
		// A threat, but no other [sauron] Orc: Fighter does not count himself.
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.FreepsHasInitiative());

		scn.ShadowPlayCard(swordsman);

		assertInZone(Zone.SHADOW_CHARACTERS, swordsman);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondMordorFighterIsAnotherSauronOrcForEachOfThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"),
				scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));

		var fighter = scn.GetShadowCard("fighter");
		var fighter2 = scn.GetShadowCard("fighter2");

		scn.StartGame();
		scn.MoveMinionsToTable(fighter, fighter2);
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();

		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, fighter, fighter2);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASauronMinionWhoIsNoOrcAndAnOrcOfAnotherCultureLeaveTheFreePeoplesPlayerTheInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"),
				scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));

		var fighter = scn.GetShadowCard("fighter");
		var uruk = scn.GetShadowCard("uruk");
		var morgul = scn.GetShadowCard("morgul");

		scn.StartGame();
		scn.MoveMinionsToTable(fighter, uruk, morgul);
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();

		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, fighter, uruk, morgul);
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoThreatTheFreePeoplesPlayerKeepsTheInitiativeUntilOneIsAddedAndRegainsItWhenItIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"),
				scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));

		var fighter = scn.GetShadowCard("fighter");
		var swordsman = scn.GetShadowCard("swordsman");

		scn.StartGame();
		scn.MoveMinionsToTable(fighter, swordsman);
		scn.FreepsRefreshDecision();

		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, fighter, swordsman);
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());

		scn.AddThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());

		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
