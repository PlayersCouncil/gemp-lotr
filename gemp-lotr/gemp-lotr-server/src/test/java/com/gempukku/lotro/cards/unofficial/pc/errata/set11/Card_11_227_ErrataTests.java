package com.gempukku.lotro.cards.unofficial.pc.errata.set11;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_227_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Companions of three cultures. None has an archery ability.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor]; a maneuver ability never used here
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor], cost 3; a skirmish ability
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven]; a skirmish ability
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire]; a fellowship ability never used here
					put("merry", "1_302");      // Merry, Friend to Sam: [shire]; a skirmish ability
					put("pippin", "1_306");     // Pippin, Friend to Frodo: [shire], cost 1; protects [shire] tales
					put("bounder", "1_286");    // Bounder: a [shire] ally with a skirmish ability: a Free Peoples character who is no companion

					put("savage", "1_151");     // Uruk Savage: a minion with damage +1 and nothing else: no archer
					put("marksman", "1_176");   // Goblin Marksman: Archer is its whole text
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");     // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to; there are none
					put("site2", "13_185");     // Abandoned Mine Shaft: adds a burden when left in the regroup phase outside region 1; here it is in region 1
					put("site3", "11_234");     // Crags of Emyn Muil: wounds each companion when left in the regroup phase in region 2; here it is in region 1
					put("anduin", "61_227");
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

	protected VirtualTableScenario GetFellowshipPathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");
					put("boromir", "1_97");
					put("gimli", "1_13");
					put("sam", "1_311");
					put("merry", "1_302");
					put("bounder", "1_286");
					put("savage", "1_151");
				}},
				// The turn-skippers walk a Fellowship-block path, which they cannot do on a Shadows-block one.
				// Each adventure deck has Anduin Banks in place of site 7, and the test puts the Shadow player's
				// copy on the path as the site the fellowship moves to from site 6.
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site7", "61_227");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AnduinBanksStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Anduin Banks
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>River.</b> The minion archery total is +X for each companion you can spot over 5, where
		 * 	X is the current region number.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("anduin");

		assertEquals("Anduin Banks", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void TheSixthAndSeventhCompanionsEachAddTheRegionNumberAndAnAllyAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		// Frodo and four more are five companions; the ally is a sixth Free Peoples character.
		scn.MoveCompanionsToTable(aragorn, gimli, sam, merry);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(boromir, pippin);

		// Anduin Banks is laid as site 1: region 1.
		scn.StartGame(scn.GetFreepsSite("anduin"));

		assertEquals("Anduin Banks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetShadowArcheryTotal());

		scn.FreepsPlayCard(boromir);

		// 1 companion over 5, in region 1
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.FreepsPlayCard(pippin);

		// 2 companions over 5, in region 1
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHinderedCompanionCannotBeSpottedAndSoAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		// Frodo and six more are seven companions on the table.
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, sam, merry, pippin);

		// Anduin Banks is laid as site 1: region 1.
		scn.StartGame(scn.GetFreepsSite("anduin"));

		assertFalse(scn.IsHindered(pippin));
		// 2 companions over 5, in region 1
		assertEquals(2, scn.GetShadowArcheryTotal());

		scn.HinderCard(pippin);

		// Still on the table, but a hindered card cannot be spotted as a companion: 6 can be, 1 over 5
		assertTrue(scn.IsHindered(pippin));
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.HinderCard(boromir);

		// 5 can be spotted: none over 5
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSiteThreeSixCompanionsAddOneWoundOfMinionArcheryFire() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		var savage = scn.GetShadowCard("savage");
		// Six companions with Frodo, and an ally.
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, sam, merry);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));
		// With no minion in play the game goes straight to the regroup phase; the fellowship moves again and
		// Anduin Banks is laid as site 3, the last site of region 1.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("anduin"));
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals("Anduin Banks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(3, scn.GetCurrentSiteNumber());
		// No minion is an archer: 1 companion over 5, in region 1
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, boromir, gimli, sam, merry));
		scn.FreepsChooseCard(aragorn);

		// One wound was all there was to place.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AtSiteFourEachCompanionOverFiveAddsTwoToTheMinionArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var marksman = scn.GetShadowCard("marksman");

		// The first turn takes the fellowship to site 3 in two moves, with no minion to fight on the way.
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site3"));
		// The rest of that turn, and the other player's turn.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		assertEquals(3, scn.GetCurrentSiteNumber());
		// Seven companions with Frodo.
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, sam, merry, pippin);

		// Anduin Banks is laid as site 4, the first site of region 2.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("anduin"));
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals("Anduin Banks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(4, scn.GetCurrentSiteNumber());
		// 1 for the archer + 2 companions over 5 x 2 for region 2
		assertEquals(5, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
	@Test
	public void AtSiteSevenTheSixthCompanionAddsThreeToTheMinionArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipPathScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(6);
		// Six companions with Frodo, and an ally.
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, sam, merry);
		scn.MoveAlliesToTable(bounder);
		// Anduin Banks becomes site 7, the first site of region 3.
		scn.MoveCardToAdventurePath(scn.GetShadowSite("site7"));
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals("Anduin Banks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(7, scn.GetCurrentSiteNumber());
		// No minion is an archer: 1 companion over 5, in region 3
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
