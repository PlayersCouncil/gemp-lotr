package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_047_Tests
{
	// The Hobbit table, with Thror's Throne as site 9 in place of Battle of the Five Armies 30_57.  Site 9 ends the
	// game once its skirmishes are survived, so the tests skip to site 8 and move on: the Shadow phase at site 9 is
	// where they look.  No minion in play at site 8, so the move there goes straight on to the regroup phase.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("assaulting", "32_27"); // Assaulting Goblin: [moria] Orc, cost 2; a response only at a battleground
					put("pursuer", "32_42");    // Orkish Pursuer: [sauron] Orc, cost 3; a win trigger and a Regroup ability, not reached
					put("troll", "32_31");      // Demolition Troll: [moria] Troll, cost 5; a Skirmish ability, not reached
					put("warg", "32_32");       // Vicious Warg: a [moria] possession (no minion), cost 3; its (4) is not at site 9
					put("dissension", "32_50"); // Dissension: a [smaug] Shadow event (no minion), cost 2

					put("arkenstone", "31_7");  // The Arkenstone: the [dwarven] artifact of that title, in the support area; abilities not used
					put("jewel", "31_48");      // The Arkenstone, King's Jewel: the [smaug] artifact of that title, on Bilbo; an end-of-maneuver trigger not reached
					put("crown", "32_1");       // Crown of Erebor: a [dwarven] artifact of another title, in the support area; its texts not reached
					put("thorin", "30_19");     // Thorin, Oakenshield: a [dwarven] companion, bearer for Thror's Map
					// Thror's Map: "Fellowship or Regroup: Exert bearer or discard this possession to play the
					// fellowship's next site ... Heal a [dwarven] companion if you play a mountain site."
					put("map", "32_12");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.HobbitSites);
					put("site9", "31_47");
				}},
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void ThrorsThroneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Thrór's Throne
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 9
		 * Shadow Number: 9
		 * Direction: none
		 * Game Text: <b>Mountain</b>. <b>Underground</b>. If you cannot spot the [dwarven] Arkenstone, the twilight cost of each minion is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Thrór's Throne", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void AtTheThroneWithNoArkenstoneEachMinionCostsOneLessAndNothingElseDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assaulting = scn.GetShadowCard("assaulting");
		var pursuer = scn.GetShadowCard("pursuer");
		var troll = scn.GetShadowCard("troll");
		var warg = scn.GetShadowCard("warg");
		var dissension = scn.GetShadowCard("dissension");
		var freepsPursuer = scn.GetFreepsCard("pursuer");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCardsToHand(assaulting, pursuer, troll, warg, dissension);
		// The Free Peoples player's own minion, in his hand: the text names no player.
		scn.MoveCardsToHand(freepsPursuer);

		// At site 8 the Throne is not on the path: every card at its printed cost.
		assertEquals(2, scn.GetTwilightCost(assaulting));
		assertEquals(3, scn.GetTwilightCost(pursuer));
		assertEquals(5, scn.GetTwilightCost(troll));
		assertEquals(3, scn.GetTwilightCost(freepsPursuer));

		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		// Site 9 is played from the Shadow player's adventure deck: his copy of this card.
		assertEquals("31_47", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Minions of three cultures and two races, either player's, cost 1 less; a Shadow possession and a Shadow event
		// cost what they print.
		assertEquals(1, scn.GetTwilightCost(assaulting));
		assertEquals(2, scn.GetTwilightCost(pursuer));
		assertEquals(4, scn.GetTwilightCost(troll));
		assertEquals(2, scn.GetTwilightCost(freepsPursuer));
		assertEquals(3, scn.GetTwilightCost(warg));
		assertEquals(2, scn.GetTwilightCost(dissension));

		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(troll);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		// 5 - 1
		assertEquals(twilight - 4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheSmaugArkenstoneOrAnotherDwarvenArtifactDoesNotStopTheDiscount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assaulting = scn.GetShadowCard("assaulting");
		var pursuer = scn.GetShadowCard("pursuer");
		var jewel = scn.GetShadowCard("jewel");
		var crown = scn.GetFreepsCard("crown");
		var bilbo = scn.GetRingBearer();
		scn.AttachCardsTo(bilbo, jewel);
		scn.MoveCardsToSupportArea(crown);

		scn.StartGame();
		assertAttachedTo(jewel, bilbo);
		assertInZone(Zone.SUPPORT, crown);
		scn.SkipToSite(8);
		scn.MoveCardsToHand(assaulting, pursuer);
		assertAttachedTo(jewel, bilbo);
		assertInZone(Zone.SUPPORT, crown);

		scn.FreepsPass();
		assertEquals("31_47", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A [smaug] card titled The Arkenstone, and a [dwarven] artifact of another title, are in play: 1 less still.
		assertEquals(1, scn.GetTwilightCost(assaulting));
		assertEquals(2, scn.GetTwilightCost(pursuer));
	}

	@Test
	public void WhileTheDwarvenArkenstoneIsSpottedEachMinionCostsItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assaulting = scn.GetShadowCard("assaulting");
		var pursuer = scn.GetShadowCard("pursuer");
		var arkenstone = scn.GetFreepsCard("arkenstone");
		scn.MoveCardsToSupportArea(arkenstone);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, arkenstone);
		scn.SkipToSite(8);
		scn.MoveCardsToHand(assaulting, pursuer);
		assertInZone(Zone.SUPPORT, arkenstone);

		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertEquals("31_47", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());

		assertEquals(2, scn.GetTwilightCost(assaulting));
		assertEquals(3, scn.GetTwilightCost(pursuer));

		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(pursuer);
		assertInZone(Zone.SHADOW_CHARACTERS, pursuer);
		assertEquals(twilight - 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheThroneLaidAheadOfTheFellowshipAMinionCostsItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var map = scn.GetFreepsCard("map");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, map);

		scn.StartGame();
		assertAttachedTo(map, thorin);
		scn.SkipToSite(7);

		// To site 8, and with no minion about straight to the regroup phase.  No fellowship ever leaves site 9, so the
		// Throne is on the path without the fellowship only when a card has laid it ahead: Thror's Map does.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsActionAvailable(map));
		scn.FreepsUseCardAction(map);
		scn.FreepsChoose("Discard this possession");
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals("31_47", scn.GetSite(9).getBlueprintId());

		// The text of a site binds only while the fellowship is at it: the Pursuer costs its printed 3.
		scn.MoveCardsToHand(pursuer);
		assertEquals(3, scn.GetTwilightCost(pursuer));

		// The fellowship moves on to the Throne: 3 - 1.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals("31_47", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetTwilightCost(pursuer));
	}
}
