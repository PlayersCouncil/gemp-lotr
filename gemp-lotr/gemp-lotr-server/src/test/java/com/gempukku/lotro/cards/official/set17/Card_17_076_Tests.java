package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_076_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fiend", "17_76");
					put("denizen", "11_115");    // Denizen of Khazad-dum: [orc] Orc; +2 only at an underground site
					put("scurrying", "11_137");  // Scurrying Goblin: [orc] Orc; its play trigger is never set off (cheated in)
					put("halftroll", "14_12");   // Half-troll of Far Harad: [orc] minion who is no Orc; its trigger needs more companions than minions
					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc minion, no text
					put("runner", "1_178");      // Goblin Runner: [moria] Orc minion; its play trigger is never set off (cheated in)
					put("nelya", "1_233");       // Úlairë Nelya, Lieutenant of Morgul: Shadow: exert her and spot an opponent's site to replace it with your site of the same number

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: the ranger Pathfinder spots; a maneuver ability only used on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The King-block path: site 3 is The Dimholt, underground and a sanctuary.
	protected VirtualTableScenario GetKingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fiend", "17_76");
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: the ranger Pathfinder spots; a maneuver ability only used on purpose
					put("pathfinder", "1_110");  // Pathfinder: Fellowship or Regroup: spot a ranger to play the fellowship's next site (replacing opponent's site if necessary)
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A Shadows-block path: each site is laid from an adventure deck as the game goes.
	protected VirtualTableScenario GetSitesLaidFromAdventureDecksScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fiend", "17_76");
					// Sméagol, Old Noser: the companion the two cards below spot; a regroup ability that exerts
					// him twice, used only on purpose
					put("smeagol", "5_28");
					// One Good Turn Deserves Another: Fellowship or Regroup event, cost 0. "Spot Sméagol to play
					// the fellowship's next site. Then you may add a burden to take this card back into hand."
					put("turn1", "11_49");
					put("turn2", "11_49");
					// There's Another Way: support-area condition; Sméagol is strength +1. "Regroup: Discard this
					// condition to replace the fellowship's current site with a site from your adventure deck."
					put("way1", "12_40");
					put("way2", "12_40");
				}},
				// site2 is Abandoned Mine Shaft (underground; its burden comes only on a regroup move from it
				// outside region 1), site5 Sirannon Ruins (river, no text), site4 Nurn, site8 The Angle
				VirtualTableScenario.ShadowsSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void OrkishFiendStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Orkish Fiend
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 8
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 18
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: To play, spot an [orc] Orc.<br>While at an underground site, this minion is <b>fierce</b> and the Free Peoples player may not replace sites or play a site from his or her adventure deck.
		*/

		var scn = GetScenario();

		var fiend = scn.GetFreepsCard("fiend");

		assertEquals("Orkish Fiend", fiend.getBlueprint().getTitle());
		assertNull(fiend.getBlueprint().getSubtitle());
		assertFalse(fiend.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, fiend.getBlueprint().getSide());
		assertEquals(Culture.ORC, fiend.getBlueprint().getCulture());
		assertEquals(CardType.MINION, fiend.getBlueprint().getCardType());
		assertEquals(Race.ORC, fiend.getBlueprint().getRace());
		assertEquals(8, fiend.getBlueprint().getTwilightCost());
		assertEquals(18, fiend.getBlueprint().getStrength());
		assertEquals(4, fiend.getBlueprint().getVitality());
		assertEquals(4, fiend.getBlueprint().getSiteNumber());
	}

	// This board does not tell "may not replace sites" from "may not play a site from
	// his or her adventure deck".  The game replaces a site only by playing one from an adventure deck, so the
	// second bar alone stops both cards below; the first alone would let the event play site 3.
	@Test
	public void AtAnUndergroundSiteTheFreePeoplesPlayerCanNeitherPlayNorReplaceASiteAndElsewhereHeCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSitesLaidFromAdventureDecksScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var turn1 = scn.GetFreepsCard("turn1");
		var turn2 = scn.GetFreepsCard("turn2");
		var way1 = scn.GetFreepsCard("way1");
		var way2 = scn.GetFreepsCard("way2");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(way1, way2);
		scn.MoveCardsToHand(turn1, turn2);

		var fiend = scn.GetShadowCard("fiend");

		var shaft = scn.GetShadowSite("site2");
		var ruins = scn.GetShadowSite("site5");
		var nurn = scn.GetFreepsSite("site4");
		var angle = scn.GetFreepsSite("site8");

		// The fellowship moves to Abandoned Mine Shaft, an underground site, where the Fiend stands.
		scn.StartGame(scn.GetFreepsSite("site1"));
		assertInZone(Zone.SUPPORT, way1, way2);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shaft);
		scn.MoveMinionsToTable(fiend);
		scn.SkipToPhase(Phase.REGROUP);

		assertSame(shaft, scn.GetCurrentSite());
		assertNull(scn.GetSite(3));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(turn1));

		// The event is played and plays no site: the Free Peoples player is not asked for one, and goes
		// straight on to its second sentence.
		scn.FreepsPlayCard(turn1);
		assertTrue(scn.FreepsDecisionAvailable("Do you want to add a burden"));
		scn.FreepsChooseNo();
		assertNull(scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, nurn, angle);
		assertInDiscard(turn1);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// The condition is discarded, which is its cost, and the site under the fellowship is not replaced.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(way1));
		scn.FreepsUseCardAction(way1);
		assertInDiscard(way1);
		assertSame(shaft, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, nurn, angle);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// The fellowship moves on with the Fiend still in play.  The site it moves to is the Shadow player's
		// to lay, and he lays it: Sirannon Ruins, which is not underground.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(ruins);
		scn.SkipToPhase(Phase.REGROUP);

		assertSame(ruins, scn.GetCurrentSite());
		assertInZone(Zone.SHADOW_CHARACTERS, fiend);
		assertNull(scn.GetSite(4));

		// Here the same event plays the fellowship's next site from his adventure deck...
		scn.FreepsPlayCard(turn2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(nurn, angle));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(nurn);
		assertSame(nurn, scn.GetSite(4));
		scn.FreepsChooseNo();
		assertInDiscard(turn2);

		// ...and the other copy of the condition replaces the site under the fellowship with one of his.
		scn.ShadowPass();
		scn.FreepsUseCardAction(way2);
		assertInDiscard(way2);
		scn.FreepsChooseCardBPFromSelection(angle);
		assertSame(angle, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, ruins);
		assertSame(nurn, scn.GetSite(4));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAnOrcOrcIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var fiend = scn.GetShadowCard("fiend");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(fiend);

		scn.StartGame();
		assertInZone(Zone.HAND, fiend);
		scn.FreepsPass();
		// An [orc] minion who is no Orc, and an Orc who is not [orc].
		scn.MoveMinionsToTable(halftroll, troop);
		// 8 to play + 2 roaming at site 2.
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.HAND, fiend);
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(fiend));

		scn.MoveMinionsToTable(denizen);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(fiend));

		scn.ShadowPlayCard(fiend);
		assertInZone(Zone.SHADOW_CHARACTERS, fiend);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedWithAnotherOrcOrcToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var fiend = scn.GetShadowCard("fiend");
		var scurrying = scn.GetShadowCard("scurrying");
		scn.MoveCardsToHand(fiend);

		scn.StartGame();
		assertInZone(Zone.HAND, fiend);
		scn.FreepsPass();
		scn.MoveMinionsToTable(scurrying);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.HAND, fiend);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(fiend));
	}

	@Test
	public void AtAnUndergroundSiteTheFiendIsFierceAndAnotherOrcIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var fiend = scn.GetShadowCard("fiend");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(3);
		// Laid at Rivendell Valley, site 3, to be read before and after the move to Balin's Tomb (Q-0228).
		scn.MoveMinionsToTable(fiend, runner);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(fiend, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));

		scn.FreepsPass();

		// Balin's Tomb, site 4, is underground.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(fiend, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtTheDimholtAnUndergroundSanctuaryTheFiendIsFierceAndNoSiteIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var site4 = scn.GetFreepsSite(4);
		scn.MoveCompanionsToTable(aragorn);

		var fiend = scn.GetShadowCard("fiend");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsHand(pathfinder);
		scn.MoveMinionsToTable(fiend);
		scn.FreepsRefreshDecision();

		assertEquals("The Dimholt", scn.GetCurrentSite().getBlueprint().getTitle());
		assertNull(scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, site4);
		assertInZone(Zone.SHADOW_CHARACTERS, fiend);
		assertTrue(scn.HasKeyword(fiend, Keyword.FIERCE));
		assertTrue(scn.FreepsPlayAvailable(pathfinder));

		scn.FreepsPlayCard(pathfinder);

		assertNull(scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, site4);
		assertInDiscard(pathfinder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtAnUndergroundSiteTheShadowPlayerMayStillReplaceASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var site1 = scn.GetFreepsSite(1);
		var shadowSite1 = scn.GetShadowSite(1);

		var fiend = scn.GetShadowCard("fiend");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();
		scn.MoveMinionsToTable(fiend, nelya);
		scn.ShadowRefreshDecision();

		// The fellowship is at Balin's Tomb, and site 1 is the Free Peoples player's own.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(fiend, Keyword.FIERCE));
		assertSame(site1, scn.GetSite(1));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite1);
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.ShadowActionAvailable(nelya));

		scn.ShadowUseCardAction(nelya);

		assertEquals(1, scn.GetWoundsOn(nelya));
		assertSame(shadowSite1, scn.GetSite(1));
		assertInZone(Zone.ADVENTURE_DECK, site1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
