package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_104_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("morgai", "8_104");

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc minion; his text answers a weapon played on him
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai minion, damage +1, nothing else
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc minion; his text needs 2 wounds on the Ring-bearer
					put("teeth", "5_110");      // Teeth of Mordor: [sauron] support condition; regroup and Shadow abilities only
					put("teeth2", "5_110");

					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, no requirement to play
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, no requirement to play
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, no requirement to play

					// Cards for the Free Peoples player's hand, which sets who has initiative.
					put("card1", "1_177");      // Goblin Patrol Troop: a minion with no text
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
					put("morgai", "8_104");

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc minion; his text answers a weapon played on him

					// Cards for the Free Peoples player's hand, which sets who has initiative.
					put("card1", "1_177");      // Goblin Patrol Troop: a minion with no text
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
	public void MorgaiStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Morgai
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: While you can spot a [sauron] minion at site 5[K], the Shadow has initiative, regardless of the Free Peoples player's hand.
		 * <b>Response:</b> If a companion is played, discard a [sauron] card from hand to take control of a site. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("morgai");

		assertEquals("Morgai", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ASauronOrcPlayedAtKingSite5GivesTheShadowInitiativeWithAFullFreePeoplesHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var morgai = scn.GetShadowCard("morgai");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToSupportArea(morgai);
		scn.SetShadowHand(chieftain);
		scn.SetFreepsHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"));
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, morgai);
		assertInHand(chieftain);
		scn.FreepsPass();

		// The fellowship is at Pelennor Flat, site 5 of the King block.
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());

		scn.ShadowPlayCard(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AWraithOrcAndASauronConditionAtKingSite5LeaveTheInitiativeUntilASauronUrukHaiIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var morgai = scn.GetShadowCard("morgai");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		var teeth = scn.GetShadowCard("teeth");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToSupportArea(morgai, teeth);
		scn.SetShadowHand(guard);
		scn.SetFreepsHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"));
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, morgai, teeth);
		assertInHand(guard);
		scn.FreepsPass();

		scn.MoveMinionsToTable(hound);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, hound);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());

		// The Guard is a [sauron] minion who is no Orc.
		scn.ShadowPlayCard(guard);
		assertInZone(Zone.SHADOW_CHARACTERS, guard);

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASauronMinionAtKingSite4LeavesTheFreePeoplesPlayerTheInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var morgai = scn.GetShadowCard("morgai");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(morgai);
		scn.SetFreepsHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"));
		assertInZone(Zone.SUPPORT, morgai);
		scn.FreepsPass();

		scn.MoveMinionsToTable(chieftain);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);

		// The fellowship is at Osgiliath Fallen, site 4 of the King block.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASauronMinionAtKingSite6LeavesTheFreePeoplesPlayerTheInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var morgai = scn.GetShadowCard("morgai");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToSupportArea(morgai);
		scn.SetFreepsHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"));
		assertInZone(Zone.SUPPORT, morgai);
		scn.FreepsPass();

		scn.MoveMinionsToTable(chieftain);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);

		// The fellowship is at Minas Tirith Seventh Circle, site 6 of the King block, the site after the one named.
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASauronMinionAtSite5OfTheFellowshipBlockLeavesTheFreePeoplesPlayerTheInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var morgai = scn.GetShadowCard("morgai");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToSupportArea(morgai);
		scn.SetFreepsHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"));
		assertInZone(Zone.SUPPORT, morgai);
		scn.FreepsPass();

		scn.MoveMinionsToTable(chieftain);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);

		// The fellowship is at The Bridge of Khazad-dum, site 5 of the Fellowship block.
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ResponseToACompanionPlayedDiscardsASauronCardFromHandToTakeControlOfASiteAndDiscardsMorgai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");

		var morgai = scn.GetShadowCard("morgai");
		var teeth = scn.GetShadowCard("teeth");
		var chieftain = scn.GetShadowCard("chieftain");
		var teeth2 = scn.GetShadowCard("teeth2");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCardsToHand(gimli);
		scn.MoveCardsToSupportArea(morgai, teeth);
		scn.SetShadowHand(chieftain, teeth2, hound);
		assertInZone(Zone.SUPPORT, morgai, teeth);
		assertInHand(gimli, chieftain, teeth2, hound);
		assertEquals(3, scn.GetShadowHandCount());
		assertFalse(scn.IsSiteControlled(site1));

		scn.FreepsPlayCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(morgai));

		scn.ShadowChooseOptionalTrigger(morgai);
		// A [sauron] minion and a [sauron] condition may pay; the [wraith] Orc may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, teeth2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(teeth2);

		assertInDiscard(teeth2);
		assertInHand(chieftain, hound);
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertInDiscard(morgai);
		// The other [sauron] condition stays.
		assertInZone(Zone.SUPPORT, teeth);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhenAnAllyIsPlayedAndIsWhenAnotherCompanionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bounder = scn.GetFreepsCard("bounder");
		var boromir = scn.GetFreepsCard("boromir");

		var morgai = scn.GetShadowCard("morgai");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCardsToHand(bounder, boromir);
		scn.MoveCardsToSupportArea(morgai);
		scn.SetShadowHand(chieftain);
		assertInZone(Zone.SUPPORT, morgai);
		assertInHand(bounder, boromir, chieftain);

		scn.FreepsPlayCard(bounder);
		assertInZone(Zone.SUPPORT, bounder);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(morgai));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(boromir);
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(morgai));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, morgai);
		assertInHand(chieftain);
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWithNoSauronCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");

		var morgai = scn.GetShadowCard("morgai");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCardsToHand(gimli);
		scn.MoveCardsToSupportArea(morgai);
		scn.SetShadowHand(hound);
		assertInZone(Zone.SUPPORT, morgai);
		assertInHand(gimli, hound);

		scn.FreepsPlayCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(morgai));

		assertInZone(Zone.SUPPORT, morgai);
		assertInHand(hound);
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
