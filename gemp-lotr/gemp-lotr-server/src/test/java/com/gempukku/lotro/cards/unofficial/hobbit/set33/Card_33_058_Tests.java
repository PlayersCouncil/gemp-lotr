package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_058_Tests
{

	// The Hobbit table, with Front Gate of Erebor as site 7 in place of Erebor 30_55. SkipToSite(6) and the
	// fellowship phase's move reach it; SkipToSite(7) and the move reach Smaug's Den 30_56, site 8.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Bombur, Chief Cook: [dwarven] Dwarf follower, "Aid - Exert a [dwarven] companion"; bearer is damage +1
					put("bombur", "30_7");
					// Dori, Really a decent fellow: [dwarven] Dwarf follower, "Aid - Exert Bilbo"; a bar on overwhelming
					put("dori", "30_8");
					// Roäc, Son of Carc: [dwarven] follower who is a Bird, "Aid - Exert Thorin"; a maneuver ability never used
					put("roac", "32_8");
					// Old Thrush, Messenger Bird: a [shire] follower, "Aid - Exert Bilbo"; a maneuver ability never used
					put("thrush", "31_42");
					// Thorin, Oakenshield: [dwarven] Dwarf companion, vitality 4; +1 for each [dwarven] follower he bears
					put("thorin", "30_19");
					// Balin, Brother of Dwalin: [dwarven] Dwarf companion; a fellowship ability only
					put("balin", "30_2");
					// Wicked Spider: [spider] minion, so that there is a maneuver phase; a skirmish ability only
					put("spider", "31_55");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.HobbitSites);
					put("site7", "33_58");
				}},
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void FrontGateofEreborStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Front Gate of Erebor
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 7
		 * Shadow Number: 8
		 * Direction: none
		 * Game Text: <b>Mountain</b>. <b>River</b>. [dwarven] followers may be attached to [dwarven] companions without paying the aid cost.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(7);

		assertEquals("Front Gate of Erebor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(7, card.getBlueprint().getSiteNumber());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void AtFrontGateDwarvenFollowersGoToDwarvenCompanionsWithoutPayingTheirAid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var bombur = scn.GetFreepsCard("bombur");
		var dori = scn.GetFreepsCard("dori");
		var roac = scn.GetFreepsCard("roac");
		var thrush = scn.GetFreepsCard("thrush");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveCardsToSupportArea(bombur, dori, roac, thrush);
		// Vitality 4 with 3 wounds: Bilbo cannot exert, so neither Dori's Aid nor Old Thrush's can be paid.
		scn.AddWoundsToChar(bilbo, 3);
		assertInZone(Zone.SUPPORT, bombur, dori, roac, thrush);
		assertEquals(3, scn.GetWoundsOn(bilbo));

		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable("spider");
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsDecisionAvailable("for free"));
		scn.FreepsChooseYes();
		// [dwarven] followers, a Dwarf and a Bird; not the [shire] follower.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bombur, dori, roac));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thrush));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(dori);
		// Two [dwarven] companions; not Bilbo, a [shire] companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(thorin, balin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(thorin);
		assertAttachedTo(dori, thorin);

		// Roäc's Aid is an exertion of Thorin, and he goes to Balin with Thorin unwounded.
		assertTrue(scn.FreepsDecisionAvailable("another"));
		scn.FreepsChooseYes();
		scn.FreepsChooseCard(roac);
		scn.FreepsChooseCard(balin);
		assertAttachedTo(roac, balin);
		scn.FreepsChooseNo();

		assertEquals(3, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertInZone(Zone.SUPPORT, bombur, thrush);
		// Bombur's own Aid can still be paid and is offered; Old Thrush's cannot, and is not.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bombur, "Aid"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(thrush, "Aid"));
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AtFrontGateAnotherFollowerOrAnotherCompanionStillPaysTheAid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var bombur = scn.GetFreepsCard("bombur");
		var thrush = scn.GetFreepsCard("thrush");
		var thorin = scn.GetFreepsCard("thorin");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToSupportArea(bombur, thrush);
		assertInZone(Zone.SUPPORT, bombur, thrush);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(thorin));

		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable("spider");
		scn.SkipToPhase(Phase.MANEUVER);

		// The free attaching declined: nothing moves and nobody exerts.
		assertTrue(scn.FreepsDecisionAvailable("for free"));
		scn.FreepsChooseNo();
		assertInZone(Zone.SUPPORT, bombur, thrush);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(thorin));

		// A [shire] follower to a [dwarven] companion: Bilbo exerts for Old Thrush's Aid.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(thrush, "Aid"));
		scn.FreepsChooseOptionalTrigger(thrush);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		scn.FreepsChooseCard(thorin);
		assertAttachedTo(thrush, thorin);

		// A [dwarven] follower to a [shire] companion: Thorin, the one [dwarven] companion, exerts for Bombur's Aid.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bombur, "Aid"));
		scn.FreepsChooseOptionalTrigger(bombur);
		assertEquals(1, scn.GetWoundsOn(thorin));
		scn.FreepsChooseCard(bilbo);
		assertAttachedTo(bombur, bilbo);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoDwarvenFollowerNothingIsAskedAndAnotherFollowersAidIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var thrush = scn.GetFreepsCard("thrush");
		var thorin = scn.GetFreepsCard("thorin");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToSupportArea(thrush);
		assertInZone(Zone.SUPPORT, thrush);
		assertEquals(0, scn.GetWoundsOn(bilbo));

		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable("spider");
		scn.SkipToPhase(Phase.MANEUVER);

		// Only a [shire] follower: yes would do nothing, so nothing is asked (F-0379), and its Aid is offered.
		assertFalse(scn.FreepsDecisionAvailable("for free"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(thrush, "Aid"));
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, thrush);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoDwarvenCompanionNothingIsAskedAndTheFollowersAidIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var dori = scn.GetFreepsCard("dori");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToSupportArea(dori);
		assertInZone(Zone.SUPPORT, dori);
		assertEquals(0, scn.GetWoundsOn(bilbo));

		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable("spider");
		scn.SkipToPhase(Phase.MANEUVER);

		// A [dwarven] follower, but Bilbo, a [shire] companion, is the only companion: yes would do nothing, so nothing
		// is asked (F-0379), and the follower's own Aid (exert Bilbo) is offered.
		assertFalse(scn.FreepsDecisionAvailable("for free"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dori, "Aid"));
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, dori);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AtTheNextSiteTheAidCostIsPaidAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var bombur = scn.GetFreepsCard("bombur");
		var dori = scn.GetFreepsCard("dori");
		var thorin = scn.GetFreepsCard("thorin");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToSupportArea(bombur, dori);
		scn.AddWoundsToChar(bilbo, 3);
		assertInZone(Zone.SUPPORT, bombur, dori);
		assertEquals(3, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(thorin));

		// Front Gate left behind for Smaug's Den.
		scn.FreepsPass();
		assertEquals(8, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable("spider");
		scn.SkipToPhase(Phase.MANEUVER);

		// No free attaching, and Dori's Aid (Bilbo exhausted) is not offered.
		assertFalse(scn.FreepsDecisionAvailable("for free"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dori, "Aid"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bombur, "Aid"));
		scn.FreepsAcceptOptionalTrigger();
		// Thorin, the one [dwarven] companion, exerts for Bombur, who goes to him.
		assertEquals(1, scn.GetWoundsOn(thorin));
		scn.FreepsChooseCard(thorin);
		assertAttachedTo(bombur, thorin);
		assertInZone(Zone.SUPPORT, dori);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
