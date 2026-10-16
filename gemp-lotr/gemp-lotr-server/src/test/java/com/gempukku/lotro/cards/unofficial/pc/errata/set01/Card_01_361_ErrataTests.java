package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_361_ErrataTests
{
	// The Fellowship block path with this Slopes of Amon Hen as site 9.  Site 9 ends the game once the skirmishes are
	// survived, so the tests skip to site 8 and move on: the Shadow phase at site 9 is where they look.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] companion; a maneuver ability
					put("guard", "1_7");          // Dwarf Guard: [dwarven] companion; needs a Dwarf to be played, so cheated in
					put("gimli", "1_13");         // Gimli, Son of Gloin: [dwarven] companion; damage +1 and a skirmish ability
					put("elrond", "1_40");        // Elrond, Lord of Rivendell: [elven] ally; heals site 3 allies at the start of the turn
					// Pathfinder: "Fellowship or Regroup: Spot a ranger to play the fellowship's next site."  Aragorn is a ranger.
					put("pathfinder", "1_110");

					put("party1", "1_158");       // Uruk-hai Raiding Party: [isengard] minion, cost 4, site 5; damage +1 only
					put("party2", "1_158");
					put("savage1", "1_151");      // Uruk Savage: [isengard] minion, cost 2, site 5; damage +1 only
					put("savage2", "1_151");
					put("troop1", "1_177");       // Goblin Patrol Troop: [moria] minion, cost 6, site 4, no text
					put("troop2", "1_177");
					put("bloodlust1", "1_144");   // Uruk Bloodlust: [isengard] condition, cost 3, support area; a response when an Uruk-hai wins
					put("bloodlust2", "1_144");
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
					put("site9", "51_361");
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
		 * Game Text: While you can spot more companions than minions, [isengard] minions are twilight cost -1 for
		 * each [isengard] minion you can spot.
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
	public void IsengardMinionsCostOneLessForEachIsengardMinionUntilMinionsAreAsManyAsCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var elrond = scn.GetFreepsCard("elrond");
		var party1 = scn.GetShadowCard("party1");
		var party2 = scn.GetShadowCard("party2");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var bloodlust1 = scn.GetShadowCard("bloodlust1");
		var bloodlust2 = scn.GetShadowCard("bloodlust2");
		scn.MoveCompanionsToTable(aragorn, guard, gimli);

		scn.StartGame();
		scn.SkipToSite(8);
		// Elrond goes down after site 7, which discards every ally.
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(party1, party2, savage2, troop2, bloodlust2);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Site 9 is played from the Shadow player's adventure deck: his copy of this card.
		assertEquals("51_361", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// 4 companions (Frodo, Aragorn, the Guard, Gimli) and an ally.  One minion, of [moria], and an [isengard]
		// condition: no [isengard] minion to spot, so nothing is cheaper.
		scn.MoveMinionsToTable(troop1);
		scn.MoveCardsToSupportArea(bloodlust1);
		assertEquals(4, scn.GetTwilightCost(party1));
		assertEquals(2, scn.GetTwilightCost(savage2));

		// One [isengard] minion, 2 minions against 4 companions: [isengard] minions are -1, and nothing else is.
		scn.MoveMinionsToTable(savage1);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetTwilightCost(party1));
		assertEquals(3, scn.GetTwilightCost(party2));
		assertEquals(1, scn.GetTwilightCost(savage2));
		assertEquals(6, scn.GetTwilightCost(troop2));
		assertEquals(3, scn.GetTwilightCost(bloodlust2));

		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(party1);
		assertInZone(Zone.SHADOW_CHARACTERS, party1);
		assertEquals(twilight - 3, scn.GetTwilight());

		// Two [isengard] minions, 3 minions against 4 companions: -2.
		assertEquals(2, scn.GetTwilightCost(party2));
		assertEquals(0, scn.GetTwilightCost(savage2));
		assertEquals(6, scn.GetTwilightCost(troop2));
		assertEquals(3, scn.GetTwilightCost(bloodlust2));

		twilight = scn.GetTwilight();
		scn.ShadowPlayCard(savage2);
		assertInZone(Zone.SHADOW_CHARACTERS, savage2);
		assertEquals(twilight, scn.GetTwilight());

		// 4 minions against 4 companions (the ally is no companion): no longer more companions, so no discount,
		// though 3 [isengard] minions can be spotted.
		assertEquals(4, scn.GetTwilightCost(party2));
		assertEquals(6, scn.GetTwilightCost(troop2));

		twilight = scn.GetTwilight();
		scn.ShadowPlayCard(party2);
		assertEquals(twilight - 4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheSlopesLaidAheadOfTheFellowshipAnIsengardMinionCostsItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var party = scn.GetShadowCard("party1");
		var savage = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(aragorn, guard, gimli);

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
		assertEquals("51_361", scn.GetSite(9).getBlueprintId());

		// 4 companions and one minion, of [isengard].  The text of a site binds only while the fellowship is at
		// it: a Raiding Party costs its printed 4.
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToHand(party);
		assertEquals(4, scn.GetTwilightCost(party));

		// The fellowship moves on to the Slopes, the Savage still on the table: 4 - 1.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals("51_361", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(3, scn.GetTwilightCost(party));

		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(party);
		assertInZone(Zone.SHADOW_CHARACTERS, party);
		assertEquals(twilight - 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
