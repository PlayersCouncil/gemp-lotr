package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_050_Tests
{
	// The Hobbit table: the Hobbit path (Mirkwood 30_53 as site 5), Bilbo, Reliable Companion 30_46 as the
	// Ring-bearer and no card in the Ring slot. The River is the Shadow player's, in his support area.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("river", "31_50");

					// [dwarven] companions to exert.
					put("balin", "30_2");       // Balin, Brother of Dwalin: vitality 3; a Fellowship ability, not used
					put("nori", "30_15");       // Nori, Of the Blue Mountains: vitality 3; a Maneuver ability, not used
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: vitality 3; a Maneuver ability, not used
					put("kili", "30_12");       // Kili, Brother of Fili: vitality 3, laid exhausted; an Archery ability, not used
					put("dain", "32_2");        // Dain Ironfoot: [dwarven] ally; his regroup offer wants a [dwarven] follower

					// Cards the Free Peoples player plays.
					put("gandalf", "30_25");    // Gandalf, Leader of Dwarves: [gandalf] companion, cost 2; no text on his play
					put("gloin", "30_11");      // Glóin, Father of Gimli: [dwarven] companion, cost 2; a Skirmish ability
					put("dori", "30_8");        // Dori: [dwarven] follower, cost 1; Aid - exert Bilbo
					put("radagast", "31_17");   // Radagast, The Brown: [gandalf] ally, cost 4; a Fellowship ability, not used

					// Spiders, and a minion that is none.
					put("wicked", "31_55");     // Wicked Spider: [spider] Spider, vitality 2; a Skirmish ability, not used
					put("wicked2", "31_55");
					put("fat", "31_51");        // Fat Spider: [spider] Spider, vitality 2; a Regroup ability, not used
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: [gollum] Spider, vitality 8; no text that is reached
					put("tomnoddy", "31_53");   // Old Tomnoddy: [spider] Spider, vitality 3, laid exhausted; a text on its own play only
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, vitality 2; its Assignment ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	// Forest River 31_46 as site 5 in place of Mirkwood.
	protected VirtualTableScenario GetForestRiverScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("river", "31_50");

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, vitality 3
					put("nori", "30_15");       // Nori, Of the Blue Mountains: [dwarven] companion, vitality 3
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: [dwarven] companion, vitality 3
					put("dori", "30_8");        // Dori: [dwarven] follower, cost 1; Aid - exert Bilbo
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, so that there is a maneuver phase
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.HobbitSites);
					put("site5", "31_46");
				}},
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void EnchantedRiverStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Enchanted River
		 * Unique: True
		 * Side: Shadow
		 * Culture: Spider
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time a companion or a follower is played at site 5, the Free Peoples player must exert 2
		 * 	[dwarven] companions.<br><b>Shadow:</b> Discard this condition or exert 3 Spiders to draw 3 cards.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("river");

		assertEquals("Enchanted River", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SPIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachCompanionPlayedAtSite5MakesTheFreePeoplesPlayerExertTwoDwarvenCompanionsAndAnAllyNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var river = scn.GetShadowCard("river");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var dwalin = scn.GetFreepsCard("dwalin");
		var kili = scn.GetFreepsCard("kili");
		var dain = scn.GetFreepsCard("dain");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gloin = scn.GetFreepsCard("gloin");
		var radagast = scn.GetFreepsCard("radagast");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin, nori, dwalin, kili);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(river);

		scn.StartGame();
		scn.SkipToSite(5);
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.MoveCardsToHand(gandalf, gloin, radagast);
		scn.RemoveWoundsFromChar(balin, 3);
		scn.RemoveWoundsFromChar(nori, 3);
		scn.RemoveWoundsFromChar(dwalin, 3);
		// Kili, vitality 3, exhausted.
		scn.AddWoundsToChar(kili, 2);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, river);
		assertInHand(gandalf, gloin, radagast);
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(nori));
		assertEquals(0, scn.GetWoundsOn(dwalin));
		assertEquals(2, scn.GetWoundsOn(kili));

		// An ally played at site 5: nobody exerts.
		scn.FreepsPlayCard(radagast);
		assertInZone(Zone.SUPPORT, radagast);
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(nori));
		assertEquals(0, scn.GetWoundsOn(dwalin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A [gandalf] companion played: two [dwarven] companions of the Free Peoples player's choice exert. Not
		// Bilbo ([shire]), Gandalf, Dain (an ally) nor Kili (exhausted).
		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, nori, dwalin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, gandalf, dain, kili));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(balin, nori);
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(nori));
		assertEquals(0, scn.GetWoundsOn(dwalin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertEquals(2, scn.GetWoundsOn(kili));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Each time: a second companion played, a [dwarven] one, makes two more exert.
		scn.FreepsPlayCard(gloin);
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, nori, dwalin, gloin));
		scn.FreepsChooseCards(dwalin, gloin);
		assertEquals(1, scn.GetWoundsOn(dwalin));
		assertEquals(1, scn.GetWoundsOn(gloin));
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(nori));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AFollowerPlayedAtForestRiverMakesTwoDwarvesExertAndItsAidDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetForestRiverScenario();

		var river = scn.GetShadowCard("river");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var dwalin = scn.GetFreepsCard("dwalin");
		var dori = scn.GetFreepsCard("dori");
		var aggressor = scn.GetShadowCard("aggressor");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin, nori, dwalin);
		scn.MoveCardsToSupportArea(river);

		scn.StartGame();
		scn.SkipToSite(5);
		// Forest River, another site 5.
		assertEquals("Forest River", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveCardsToHand(dori);
		scn.RemoveWoundsFromChar(balin, 3);
		scn.RemoveWoundsFromChar(nori, 3);
		scn.RemoveWoundsFromChar(dwalin, 3);
		scn.RemoveWoundsFromChar(bilbo, 4);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, river);
		assertInHand(dori);
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(nori));
		assertEquals(0, scn.GetWoundsOn(dwalin));
		assertEquals(0, scn.GetWoundsOn(bilbo));

		scn.FreepsPlayCard(dori);
		assertInZone(Zone.SUPPORT, dori);
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, nori, dwalin));
		scn.FreepsChooseCards(nori, dwalin);
		assertEquals(1, scn.GetWoundsOn(nori));
		assertEquals(1, scn.GetWoundsOn(dwalin));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Dori's Aid at the start of the maneuver phase transfers him: he is not played, and nobody exerts for it.
		scn.FreepsPass();
		scn.MoveMinionsToTable(aggressor);
		scn.ShadowRefreshDecision();
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dori));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(balin);
		assertAttachedTo(dori, balin);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(nori));
		assertEquals(1, scn.GetWoundsOn(dwalin));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ACompanionPlayedAtSite4MakesNobodyExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var river = scn.GetShadowCard("river");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var dwalin = scn.GetFreepsCard("dwalin");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(balin, nori, dwalin);
		scn.MoveCardsToSupportArea(river);

		scn.StartGame();
		scn.SkipToSite(4);
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.MoveCardsToHand(gandalf);
		scn.RemoveWoundsFromChar(balin, 3);
		scn.RemoveWoundsFromChar(nori, 3);
		scn.RemoveWoundsFromChar(dwalin, 3);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, river);
		assertInHand(gandalf);

		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(nori));
		assertEquals(0, scn.GetWoundsOn(dwalin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AFollowerPlayedAtSite6MakesNobodyExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var river = scn.GetShadowCard("river");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var dwalin = scn.GetFreepsCard("dwalin");
		var dori = scn.GetFreepsCard("dori");
		scn.MoveCompanionsToTable(balin, nori, dwalin);
		scn.MoveCardsToSupportArea(river);

		scn.StartGame();
		scn.SkipToSite(6);
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.MoveCardsToHand(dori);
		scn.RemoveWoundsFromChar(balin, 3);
		scn.RemoveWoundsFromChar(nori, 3);
		scn.RemoveWoundsFromChar(dwalin, 3);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, river);
		assertInHand(dori);

		scn.FreepsPlayCard(dori);
		assertInZone(Zone.SUPPORT, dori);
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(nori));
		assertEquals(0, scn.GetWoundsOn(dwalin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowAbilityDiscardsTheRiverToDrawThreeCardsWithNoSpiders() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var river = scn.GetShadowCard("river");
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.MoveCardsToSupportArea(river);
		assertInZone(Zone.SUPPORT, river);
		scn.FreepsPass();
		scn.MoveMinionsToTable(aggressor);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetShadowHandCount());

		// No Spider: the discard is the only way to pay, and nothing is asked.
		assertTrue(scn.ShadowActionAvailable(river));
		scn.ShadowUseCardAction(river);
		assertInDiscard(river);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityExertsThreeSpidersOfTheShadowPlayersChoiceToDrawThreeCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var river = scn.GetShadowCard("river");
		var wicked = scn.GetShadowCard("wicked");
		var wicked2 = scn.GetShadowCard("wicked2");
		var fat = scn.GetShadowCard("fat");
		var shelob = scn.GetShadowCard("shelob");
		var tomnoddy = scn.GetShadowCard("tomnoddy");
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.MoveCardsToSupportArea(river);
		assertInZone(Zone.SUPPORT, river);
		scn.FreepsPass();
		scn.MoveMinionsToTable(wicked, wicked2, fat, shelob, tomnoddy, aggressor);
		// Old Tomnoddy, vitality 3, exhausted.
		scn.AddWoundsToChar(tomnoddy, 2);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, river);
		assertEquals(2, scn.GetWoundsOn(tomnoddy));
		assertEquals(0, scn.GetShadowHandCount());

		// Both ways can be paid: the Shadow player is asked which.
		scn.ShadowUseCardAction(river);
		assertEquals(List.of("Discard this condition", "Exert 3 Spiders"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Exert 3 Spiders");

		// Spiders of two cultures; not the Orc, nor Old Tomnoddy, exhausted.
		assertTrue(scn.ShadowHasCardChoiceAvailable(wicked, wicked2, fat, shelob));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aggressor, tomnoddy));
		assertEquals(3, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(wicked, fat, shelob);
		assertEquals(1, scn.GetWoundsOn(wicked));
		assertEquals(1, scn.GetWoundsOn(fat));
		assertEquals(1, scn.GetWoundsOn(shelob));
		assertEquals(0, scn.GetWoundsOn(wicked2));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(2, scn.GetWoundsOn(tomnoddy));
		assertInZone(Zone.SUPPORT, river);
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var river = scn.GetShadowCard("river");
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.MoveCardsToSupportArea(river);
		assertInZone(Zone.SUPPORT, river);
		scn.FreepsPass();
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(river));
	}
}
