package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_061_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haradwaith", "8_61");

					put("soldier", "4_254");    // Southron Soldier: [raider] Man minion (Southron), Ambush (2), nothing else
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: [raider] Nazgul minion; his text needs 3 burdens
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, no text
					put("hope", "7_159");       // Small Hope: [raider] support condition; its abilities are maneuver and skirmish ones
					put("hope2", "7_159");

					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, no requirement to play
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, no requirement to play
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, no requirement to play

					// Cards for the Free Peoples player's hand, which sets who has initiative.
					put("card1", "1_177");
					put("card2", "1_177");
					put("card3", "1_177");
					put("card4", "1_177");
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFellowshipBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haradwaith", "8_61");

					put("soldier", "4_254");    // Southron Soldier: [raider] Man minion (Southron), Ambush (2), nothing else

					// Cards for the Free Peoples player's hand, which sets who has initiative.
					put("card1", "1_177");
					put("card2", "1_177");
					put("card3", "1_177");
					put("card4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HaradwaithStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Haradwaith
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: While you can spot a [raider] minion at site 4[K], the Shadow has initiative, regardless of the Free Peoples player's hand.
		 * <b>Response:</b> If a companion is played, discard a [raider] card from hand to add (5). Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("haradwaith");

		assertEquals("Haradwaith", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ARaiderMinionPlayedAtKingSite4GivesTheShadowInitiativeWithAFullFreePeoplesHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haradwaith = scn.GetShadowCard("haradwaith");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(haradwaith);
		scn.SetShadowHand(soldier);
		scn.SetFreepsHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"));
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, haradwaith);
		scn.FreepsPass();

		// The fellowship is at Osgiliath Fallen, site 4 of the King block.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());

		scn.ShadowPlayCard(soldier);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ADunlandManAndARaiderConditionAtKingSite4LeaveTheInitiativeUntilARaiderNazgulIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haradwaith = scn.GetShadowCard("haradwaith");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		var hope = scn.GetShadowCard("hope");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(haradwaith, hope);
		scn.SetShadowHand(khamul);
		scn.SetFreepsHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"));
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, haradwaith, hope);
		scn.FreepsPass();

		scn.MoveMinionsToTable(brigand);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());

		// Khamul is a [raider] minion who is no Man.
		scn.ShadowPlayCard(khamul);
		assertInZone(Zone.SHADOW_CHARACTERS, khamul);

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARaiderMinionAtKingSite3LeavesTheFreePeoplesPlayerTheInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haradwaith = scn.GetShadowCard("haradwaith");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(haradwaith);
		scn.SetFreepsHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"));
		assertInZone(Zone.SUPPORT, haradwaith);
		scn.FreepsPass();

		scn.MoveMinionsToTable(soldier);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);

		// The fellowship is at The Dimholt, site 3 of the King block.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARaiderMinionAtKingSite5LeavesTheFreePeoplesPlayerTheInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haradwaith = scn.GetShadowCard("haradwaith");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToSupportArea(haradwaith);
		scn.SetFreepsHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"));
		assertInZone(Zone.SUPPORT, haradwaith);
		scn.FreepsPass();

		scn.MoveMinionsToTable(soldier);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);

		// The fellowship is at site 5 of the King block, the site after the one the text names.
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARaiderMinionAtSite4OfTheFellowshipBlockLeavesTheFreePeoplesPlayerTheInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var haradwaith = scn.GetShadowCard("haradwaith");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(haradwaith);
		scn.SetFreepsHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"));
		assertInZone(Zone.SUPPORT, haradwaith);
		scn.FreepsPass();

		scn.MoveMinionsToTable(soldier);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);

		// The fellowship is at Balin's Tomb, site 4 of the Fellowship block.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ResponseToACompanionPlayedDiscardsARaiderCardFromHandToAddFiveAndDiscardsHaradwaith() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(gimli);

		var haradwaith = scn.GetShadowCard("haradwaith");
		var hope = scn.GetShadowCard("hope");
		var soldier = scn.GetShadowCard("soldier");
		var hope2 = scn.GetShadowCard("hope2");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(haradwaith, hope);
		scn.MoveCardsToHand(soldier, hope2, brigand);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, haradwaith, hope);
		assertInHand(soldier, hope2, brigand);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(haradwaith));
		// Gimli's own cost of 2 is in the pool as the response is offered.
		assertEquals(2, scn.GetTwilight());

		scn.ShadowChooseOptionalTrigger(haradwaith);
		// A [raider] minion and a [raider] condition may pay; the [dunland] Man may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, hope2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hope2);

		assertInDiscard(hope2);
		assertInHand(soldier, brigand);
		assertEquals(2, scn.GetShadowHandCount());
		// 2 for Gimli + 5
		assertEquals(7, scn.GetTwilight());
		assertInDiscard(haradwaith);
		// The other [raider] condition stays.
		assertInZone(Zone.SUPPORT, hope);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhenAnAllyIsPlayedAndIsWhenAnotherCompanionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bounder = scn.GetFreepsCard("bounder");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToHand(bounder, boromir);

		var haradwaith = scn.GetShadowCard("haradwaith");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(haradwaith);
		scn.MoveCardsToHand(soldier);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, haradwaith);
		assertInHand(soldier);

		scn.FreepsPlayCard(bounder);
		assertInZone(Zone.SUPPORT, bounder);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(haradwaith));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(boromir);
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(haradwaith));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, haradwaith);
		assertInHand(soldier);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWithNoRaiderCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(gimli);

		var haradwaith = scn.GetShadowCard("haradwaith");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(haradwaith);
		scn.MoveCardsToHand(brigand);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, haradwaith);
		assertInHand(brigand);

		scn.FreepsPlayCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(haradwaith));

		assertInZone(Zone.SUPPORT, haradwaith);
		assertInHand(brigand);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
