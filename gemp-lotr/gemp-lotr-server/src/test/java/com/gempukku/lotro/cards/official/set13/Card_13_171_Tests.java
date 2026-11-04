package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_171_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("invader1", "13_171");
					put("invader2", "13_171");
					put("guard", "10_81");      // Cirith Ungol Guard: a minion of cost 3, damage +1 only, kept in hand
					put("warrior", "4_165");    // Orthanc Warrior: a minion in play, damage +1 only

					// Companions with no text that reaches a test, of printed resistance 6, 6 and 7
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man, resistance 6
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard, resistance 6
					put("faramir", "12_47");    // Faramir, Dunadan of Gondor: resistance 7; a skirmish ability never used
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally, resistance 0; his ability is never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukInvaderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Uruk Invader
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * 	This minion is twilight cost -1 for each companion who has resistance 2 or less.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("invader1");

		assertEquals("Uruk Invader", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CostsOneLessForEachOfTwoCompanionsAtResistance2AndNotForAnAllyOrMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var gandalf = scn.GetFreepsCard("gandalf");
		var faramir = scn.GetFreepsCard("faramir");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(knight, gandalf, faramir);
		scn.MoveAlliesToTable(thrarin);

		var invader1 = scn.GetShadowCard("invader1");
		var invader2 = scn.GetShadowCard("invader2");
		var guard = scn.GetShadowCard("guard");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCardsToHand(invader1, invader2, guard);

		scn.StartGame();
		assertInHand(invader1);
		// Each burden takes 1 from every companion's resistance.
		scn.AddBurdens(4);
		assertEquals(4, scn.GetBurdens());
		assertEquals(2, scn.GetResistance(knight));
		assertEquals(2, scn.GetResistance(gandalf));
		assertEquals(3, scn.GetResistance(faramir));
		assertEquals(6, scn.GetResistance(frodo));
		assertEquals(0, scn.GetResistance(thrarin));

		scn.SetTwilight(10);
		scn.FreepsPass();
		// A minion in play has no resistance to count.
		scn.MoveMinionsToTable(warrior);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// The Knight and Gandalf, of two cultures and races; not Faramir at 3, nor the ally, nor the minion.
		// 4 - 2 = 2, for each copy; another minion in hand costs what it prints.
		assertEquals(2, scn.GetTwilightCost(invader1));
		assertEquals(2, scn.GetTwilightCost(invader2));
		assertEquals(3, scn.GetTwilightCost(guard));

		assertInHand(invader1);
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(invader1);
		assertInZone(Zone.SHADOW_CHARACTERS, invader1);
		// 4 - 2 for the companions, + 2 roaming at site 2
		assertEquals(twilight - 4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CostsOneLessForEachOfThreeCompanionsAtResistance2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		var gandalf = scn.GetFreepsCard("gandalf");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(knight, gandalf, faramir);

		var invader1 = scn.GetShadowCard("invader1");
		scn.MoveCardsToHand(invader1);

		scn.StartGame();
		assertInHand(invader1);
		scn.AddBurdens(5);
		assertEquals(1, scn.GetResistance(knight));
		assertEquals(1, scn.GetResistance(gandalf));
		assertEquals(2, scn.GetResistance(faramir));

		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 4 - 3 = 1
		assertEquals(1, scn.GetTwilightCost(invader1));
		assertInHand(invader1);
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(invader1);
		assertInZone(Zone.SHADOW_CHARACTERS, invader1);
		// 4 - 3 for the companions, + 2 roaming at site 2
		assertEquals(twilight - 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CompanionsAtResistance3GiveNoDiscount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(knight, gandalf);

		var invader1 = scn.GetShadowCard("invader1");
		scn.MoveCardsToHand(invader1);

		scn.StartGame();
		assertInHand(invader1);
		scn.AddBurdens(3);
		assertEquals(3, scn.GetResistance(knight));
		assertEquals(3, scn.GetResistance(gandalf));

		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(4, scn.GetTwilightCost(invader1));
		assertInHand(invader1);
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(invader1);
		assertInZone(Zone.SHADOW_CHARACTERS, invader1);
		// 4, + 2 roaming at site 2
		assertEquals(twilight - 6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
