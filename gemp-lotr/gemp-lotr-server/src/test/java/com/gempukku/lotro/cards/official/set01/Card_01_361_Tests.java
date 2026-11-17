package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_361_Tests
{
	// The Fellowship block path with this Slopes of Amon Hen as site 9.  Site 9 ends the game once the skirmishes are
	// survived, so the tests skip to site 8 and move on: the Shadow phase at site 9 is where they look.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a ranger; a maneuver ability used only on purpose
					// Pathfinder: "Fellowship or Regroup: Spot a ranger to play the fellowship's next site."
					put("pathfinder", "1_110");

					put("party", "1_158");        // Uruk-hai Raiding Party: [isengard] Uruk-hai, cost 4, site 5; damage +1 only
					put("worker", "3_62");        // Isengard Worker: [isengard] Orc, cost 2, site 4; a regroup ability
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, cost 6, site 4, no text
					put("bloodlust", "1_144");    // Uruk Bloodlust: [isengard] condition, cost 3; a response when an Uruk-hai wins
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_361");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SlopesofAmonHenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Slopes of Amon Hen
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 9
		 * Shadow Number: 9
		 * Direction: Left
		 * Game Text: The twilight cost of each [isengard] minion is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Slopes of Amon Hen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void AtTheSlopesEachIsengardMinionCostsOneLessAndNothingElseDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var freepsParty = scn.GetFreepsCard("party");
		var party = scn.GetShadowCard("party");
		var worker = scn.GetShadowCard("worker");
		var troop = scn.GetShadowCard("troop");
		var bloodlust = scn.GetShadowCard("bloodlust");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCardsToHand(party, worker, troop, bloodlust);
		// The Free Peoples player's own [isengard] minion, in his hand: the text names no player.
		scn.MoveCardsToHand(freepsParty);

		// At site 8 the Slopes are not on the path: every card at its printed cost.
		assertEquals(4, scn.GetTwilightCost(party));
		assertEquals(2, scn.GetTwilightCost(worker));
		assertEquals(4, scn.GetTwilightCost(freepsParty));

		scn.SetTwilight(20);
		scn.FreepsPass();

		// Site 9 is played from the Shadow player's adventure deck: his copy of this card.
		assertEquals("1_361", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// An [isengard] Uruk-hai and an [isengard] Orc, either player's, cost 1 less; a [moria] minion and an
		// [isengard] condition cost what they print.
		assertEquals(3, scn.GetTwilightCost(party));
		assertEquals(1, scn.GetTwilightCost(worker));
		assertEquals(3, scn.GetTwilightCost(freepsParty));
		assertEquals(6, scn.GetTwilightCost(troop));
		assertEquals(3, scn.GetTwilightCost(bloodlust));

		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(worker);
		assertInZone(Zone.SHADOW_CHARACTERS, worker);
		// 2 - 1
		assertEquals(twilight - 1, scn.GetTwilight());

		twilight = scn.GetTwilight();
		scn.ShadowPlayCard(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertEquals(twilight - 6, scn.GetTwilight());

		twilight = scn.GetTwilight();
		scn.ShadowPlayCard(party);
		assertInZone(Zone.SHADOW_CHARACTERS, party);
		// 4 - 1
		assertEquals(twilight - 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheSlopesLaidAheadOfTheFellowshipAnIsengardMinionCostsItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToHand(pathfinder);

		// To site 8, and with no minion about straight to the regroup phase.  No fellowship ever leaves site 9, so
		// the Slopes are on the path without the fellowship only when a card has laid them ahead of it: Aragorn is
		// a ranger, and Pathfinder plays the fellowship's next site.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPlayCard(pathfinder);
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals("1_361", scn.GetSite(9).getBlueprintId());

		// The text of a site binds only while the fellowship is at it: a Raiding Party costs its printed 4.
		scn.MoveCardsToHand(party);
		assertEquals(4, scn.GetTwilightCost(party));

		// The fellowship moves on to the Slopes: 4 - 1.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals("1_361", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetTwilightCost(party));

		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(party);
		assertInZone(Zone.SHADOW_CHARACTERS, party);
		assertEquals(twilight - 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
