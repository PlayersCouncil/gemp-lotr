package com.gempukku.lotro.cards.unofficial.pc.errata.set11;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_249_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; no printed resistance, so 6; a maneuver ability never used here
					put("soldier", "17_101");   // Soldier of Rohan: vitality 3, resistance 5; stronger while the Ring-bearer is assigned to a skirmish
					put("isildur", "18_54");    // Isildur, Sword-bearer: vitality 3, resistance 1; his text is about skirmishes he wins or loses
					put("bounder", "1_286");    // Bounder: ally, vitality 2; a skirmish ability
					put("bounder2", "1_286");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc with no text, vitality 3
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; Damage +1 is its whole text
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");     // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to; there are none
					put("bog", "61_249");
					put("bog2", "61_249");      // stands for the other player's copy: each adventure deck may hold one
					put("site3", "11_234");     // Crags of Emyn Muil: wounds each companion when left in the regroup phase in region 2; here it is in region 1
					// Never laid in these tests.
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	// The turn-skippers walk a Fellowship-block path, which they cannot do on a Shadows-block one. Each adventure
	// deck has the Bog in place of one site, and the test puts the Shadow player's copy on the path as the site
	// the fellowship moves to next.
	protected VirtualTableScenario GetSiteSixScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");
					put("isildur", "18_54");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site6", "61_249");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSiteSevenScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");
					put("isildur", "18_54");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site7", "61_249");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NeekerbreekersBogStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Neekerbreekers' Bog
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 1
		 * Direction: Left
		 * Game Text: <b>Marsh.</b> When the fellowship moves here in region 1 or 2, heal each character who has
		 * 	resistance 5 or more and exert all others.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("bog");

		assertEquals("Neekerbreekers' Bog", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MARSH));
	}

	@Test
	public void MovingToTheBogInRegionOneHealsEachCharacterOfResistanceFiveOrMoreAndExertsAllOthersAndMovingOnDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var soldier = scn.GetFreepsCard("soldier");
		var isildur = scn.GetFreepsCard("isildur");
		var bounder = scn.GetFreepsCard("bounder");
		var bounder2 = scn.GetFreepsCard("bounder2");
		scn.MoveCompanionsToTable(aragorn, soldier, isildur);
		scn.MoveAlliesToTable(bounder, bounder2);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(soldier, 2);
		scn.AddWoundsToChar(isildur, 1);
		// Vitality 2 with 1 wound: this Bounder is exhausted and cannot exert.
		scn.AddWoundsToChar(bounder2, 1);

		assertEquals(0, scn.GetBurdens());
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(soldier));
		assertEquals(1, scn.GetResistance(isildur));
		// Allies have a resistance of zero.
		assertEquals(0, scn.GetResistance(bounder));
		assertEquals(0, scn.GetWoundsOn(bounder));

		scn.FreepsPass();
		// The Shadow player lays the Bog as site 2: region 1.
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("bog"));

		assertEquals("Neekerbreekers' Bog", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// Resistance 10, 6 and exactly 5: one wound healed from each.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(soldier));
		// Resistance 1, and an ally: each exerts, and the wounded one is not healed.
		assertEquals(2, scn.GetWoundsOn(isildur));
		assertEquals(1, scn.GetWoundsOn(bounder));
		// The exhausted ally cannot exert; a wound would have killed him.
		assertEquals(1, scn.GetWoundsOn(bounder2));
		assertInZone(Zone.SUPPORT, bounder2);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Moving on from the Bog in the regroup phase does nothing: it is moving to the Bog that heals and exerts.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site3"));

		assertEquals("Crags of Emyn Muil", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(2, scn.GetWoundsOn(isildur));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(bounder2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ABurdenLowersResistanceSoACompanionAtFiveIsHealedAndOneAtFourExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var soldier = scn.GetFreepsCard("soldier");
		scn.MoveCompanionsToTable(aragorn, soldier);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(soldier, 1);
		scn.AddBurdens(1);

		// Each burden lowers every companion's resistance by 1.
		assertEquals(1, scn.GetBurdens());
		assertEquals(9, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(soldier));

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("bog"));

		assertEquals("Neekerbreekers' Bog", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(1, scn.GetWoundsOn(frodo));
		// 6 less 1 burden is exactly 5: healed.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// The Soldier's printed 5 is now 4: he exerts, where with no burden he would be healed.
		assertEquals(2, scn.GetWoundsOn(soldier));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MinionsAreAmongTheOthersAndASecondBogHealsAndExertsOnceMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var isildur = scn.GetFreepsCard("isildur");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(isildur);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.AddWoundsToChar(frodo, 3);

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("bog"));

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(isildur));

		// Two minions of different cultures and races, who stay on the table when the fellowship moves again
		// in the regroup phase. Nobody is assigned to them.
		scn.MoveMinionsToTable(troop, savage);
		scn.AddWoundsToChar(troop, 1);
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(isildur));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.FreepsChooseToMove();
		// A second Bog is laid as site 3, with the first still on the path at site 2.
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("bog2"));

		assertEquals("Neekerbreekers' Bog", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(3, scn.GetCurrentSiteNumber());
		// Once more, and once only: one more wound healed from Frodo and one more exertion of Isildur.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(isildur));
		// A minion is a character with no resistance: each exerts, and the wounded one is not healed.
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToTheBogAsSiteSixInRegionTwoHealsAndExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteSixScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var isildur = scn.GetFreepsCard("isildur");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCompanionsToTable(aragorn, isildur);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(isildur, 1);
		// The Bog becomes site 6, the last site of region 2.
		scn.MoveCardToAdventurePath(scn.GetShadowSite("site6"));

		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.FreepsPass();

		assertEquals("Neekerbreekers' Bog", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(isildur));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToTheBogAsSiteSevenInRegionThreeDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteSevenScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var isildur = scn.GetFreepsCard("isildur");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(aragorn, isildur);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(isildur, 1);
		// The Bog becomes site 7, the first site of region 3.
		scn.MoveCardToAdventurePath(scn.GetShadowSite("site7"));

		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.FreepsPass();

		assertEquals("Neekerbreekers' Bog", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(7, scn.GetCurrentSiteNumber());
		// The same board as at site 6: nobody is healed and nobody exerts.
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(isildur));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOtherPlayersFellowshipMovingToTheBogIsHealedAndExertedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var theirFrodo = scn.GetShadowRingBearer();
		var theirSoldier = scn.GetShadowCard("soldier");
		var theirIsildur = scn.GetShadowCard("isildur");
		scn.MoveCompanionsToTable(aragorn);

		// Player 1 moves to the Bog, laid as site 2 by player 2 from his own adventure deck, and ends his turn there.
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("bog"));
		scn.SkipToOtherPlayersTurn();

		// Player 2's fellowship phase, at site 1.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(1, scn.GetCurrentSiteNumber());
		scn.MoveCompanionsToTable(theirSoldier, theirIsildur);
		scn.AddWoundsToChar(theirFrodo, 2);
		scn.AddWoundsToChar(theirSoldier, 2);
		scn.AddWoundsToChar(theirIsildur, 1);
		// Player 1's Aragorn is on the table, but not active on player 2's turn.
		scn.AddWoundsToChar(aragorn, 2);

		scn.ShadowPass();

		assertEquals("Neekerbreekers' Bog", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(theirFrodo));
		assertEquals(1, scn.GetWoundsOn(theirSoldier));
		assertEquals(2, scn.GetWoundsOn(theirIsildur));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
