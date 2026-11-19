package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_063_Tests
{

	// The Hobbit table, with Lake-Town as site 6 in place of Esgaroth: the turn begins at Mirkwood (site 5) and
	// the Shadow phase after the fellowship's move is at Lake-Town, the Shadow player's own copy.  Site 7 is
	// Erebor, whose Shadow ability wants 6 companions.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Goblin Footman: [moria] Orc minion, cost 3, site 4.  "When you play this minion, you may play a
					// possession from your discard pile on your minion": no possession is there.
					put("footman1", "30_42");
					put("footman2", "30_42");
					// Orkish Pursuer: [sauron] Orc minion, cost 3, site 4.  A skirmish-win trigger and a regroup
					// ability, neither reached.
					put("pursuer", "32_42");
					// Demolition Troll: [moria] Troll minion, cost 5, site 4: of the Footman's culture and no Orc.
					// A skirmish ability never reached.
					put("troll", "32_31");
					// Goblin Runner: [moria] Orc minion, cost 1, strength 5, vitality 1.  "When you play this
					// minion, you may spot another [moria] card to add (2)": no other [moria] card is in play.
					put("runner", "31_30");
					// Bolg, Captain of the Orc Army: [sauron] Orc minion.  Each other Orc is strength +1;
					// "Regroup: If Bolg is not mounted, exert him to play an Orc from your discard pile.  Its
					// twilight cost is -2."
					put("bolg", "32_36");
					// The Orc Army: [sauron] Orc minion, cost 5.  A start-of-regroup heal and a regroup ability,
					// neither reached from the discard pile.
					put("army", "32_40");
					// Thorin, Oakenshield: [dwarven] Dwarf companion, strength 7, damage +1, who beats the Runner.
					put("thorin", "30_19");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.HobbitSites);
					put("site6", "30_63");
				}},
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void LakeTownStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Lake-Town
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 6
		 * Shadow Number: 3
		 * Direction: none
		 * Game Text: <b>River</b>. <b>Sanctuary</b>. The twilight cost of the first Orc played at Lake-Town each turn is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(6);

		assertEquals("Lake-Town", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(6, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void LakeTownIsARiver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsSite(6);

		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void OnlyTheFirstOrcPlayedAtLakeTownCostsTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var footman1 = scn.GetShadowCard("footman1");
		var footman2 = scn.GetShadowCard("footman2");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.SetShadowHand(footman1, footman2);
		scn.SetTwilight(10);
		assertInZone(Zone.HAND, footman1, footman2);
		scn.FreepsPass();

		assertEquals("30_63", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// 3 printed - 2; a discount of 1 or 3 would read 2 or 0.
		assertEquals(1, scn.GetTwilightCost(footman1));
		assertEquals(1, scn.GetTwilightCost(footman2));

		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(footman1);
		assertInZone(Zone.SHADOW_CHARACTERS, footman1);
		assertEquals(pool - 1, scn.GetTwilight());

		// The second Orc of the turn costs its printed number.
		assertEquals(3, scn.GetTwilightCost(footman2));
		scn.ShadowPlayCard(footman2);
		assertInZone(Zone.SHADOW_CHARACTERS, footman2);
		assertEquals(pool - 1 - 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcOfAnotherCultureIsDiscountedAndATrollIsNeitherDiscountedNorCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var footman1 = scn.GetShadowCard("footman1");
		var pursuer = scn.GetShadowCard("pursuer");
		var troll = scn.GetShadowCard("troll");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.SetShadowHand(footman1, pursuer, troll);
		scn.SetTwilight(10);
		assertInZone(Zone.HAND, footman1, pursuer, troll);
		scn.FreepsPass();

		assertEquals("30_63", scn.GetCurrentSite().getBlueprintId());
		assertEquals(5, scn.GetTwilightCost(troll));
		assertEquals(1, scn.GetTwilightCost(pursuer));
		assertEquals(1, scn.GetTwilightCost(footman1));

		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(troll);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertEquals(pool - 5, scn.GetTwilight());
		// The Troll was no Orc: the first Orc is still to come.
		assertEquals(1, scn.GetTwilightCost(pursuer));
		assertEquals(1, scn.GetTwilightCost(footman1));

		scn.ShadowPlayCard(pursuer);
		assertInZone(Zone.SHADOW_CHARACTERS, pursuer);
		assertEquals(pool - 5 - 1, scn.GetTwilight());
		assertEquals(3, scn.GetTwilightCost(footman1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcPlayedAtTheSiteBeforeInTheSameTurnIsNotTheFirstPlayedAtLakeTown() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var runner = scn.GetShadowCard("runner");
		var footman1 = scn.GetShadowCard("footman1");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(thorin);
		scn.SetShadowHand(runner, footman1);
		scn.SetTwilight(10);
		assertInZone(Zone.HAND, runner, footman1);
		scn.FreepsPass();

		// At Mirkwood, site 5: the Runner is played and is the turn's first Orc.
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetTwilightCost(footman1));
		scn.ShadowPlayCard(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);

		// Thorin beats the Runner, and the fellowship moves on to Lake-Town.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, runner);
		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals("30_63", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(1, scn.GetTwilightCost(footman1));
		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(footman1);
		assertInZone(Zone.SHADOW_CHARACTERS, footman1);
		assertEquals(pool - 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcPlayedInTheRegroupPhaseAfterTheFirstOrcOfTheTurnCostsItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var runner = scn.GetShadowCard("runner");
		var bolg = scn.GetShadowCard("bolg");
		var army = scn.GetShadowCard("army");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCompanionsToTable(thorin);
		scn.SetShadowHand(runner);
		scn.MoveCardsToDiscard(army);
		scn.SetTwilight(10);
		assertInZone(Zone.HAND, runner);
		assertInZone(Zone.DISCARD, army);
		scn.FreepsPass();

		// At Lake-Town the Runner is the turn's first Orc: 1 - 2, and a cost never reads below 0.
		assertEquals("30_63", scn.GetCurrentSite().getBlueprintId());
		assertEquals(0, scn.GetTwilightCost(runner));
		scn.ShadowPlayCard(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, runner);
		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());

		// Bolg stands where minions that lived through the skirmishes stand, and plays The Orc Army from the
		// discard pile in the regroup phase of the same turn at Lake-Town.
		scn.MoveMinionsToTable(bolg);
		scn.SetTwilight(3);
		assertEquals(0, scn.GetWoundsOn(bolg));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(bolg));
		scn.ShadowUseCardAction(bolg);
		assertEquals(1, scn.GetWoundsOn(bolg));
		scn.ShadowChooseCard(army);

		assertInZone(Zone.SHADOW_CHARACTERS, army);
		// 5 printed - 2 by Bolg's own text; Lake-Town's first Orc this turn was the Runner.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheFirstOrcPlayedAtLakeTownOnTheNextTurnIsTwoCheaperAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var footman1 = scn.GetShadowCard("footman1");
		var myFootman = scn.GetFreepsCard("footman1");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.SetShadowHand(runner, footman1);
		scn.SetTwilight(10);
		assertInZone(Zone.HAND, runner, footman1);
		scn.FreepsPass();

		var lakeTown = scn.GetCurrentSite();
		assertEquals("30_63", lakeTown.getBlueprintId());
		scn.ShadowPlayCard(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(3, scn.GetTwilightCost(footman1));

		// The Runner is taken off so that the rest of the turn has no skirmish for Bilbo to lose.
		scn.MoveCardsToDiscard(runner);
		scn.SkipToOtherPlayersTurn();

		// The second player's fellowship, at site 5, moves to the same Lake-Town; the first player is the Shadow
		// player there.
		scn.SetFreepsHand(myFootman);
		scn.SetTwilight(10);
		assertInZone(Zone.HAND, myFootman);
		scn.ShadowPass();

		assertSame(lakeTown, scn.GetCurrentSite());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
		assertEquals(1, scn.GetTwilightCost(myFootman));
		int pool = scn.GetTwilight();
		scn.FreepsPlayCard(myFootman);
		assertInZone(Zone.SHADOW_CHARACTERS, myFootman);
		assertEquals(pool - 1, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void OnceTheFellowshipHasMovedOnFromLakeTownTheFirstOrcCostsItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var footman1 = scn.GetShadowCard("footman1");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SetShadowHand(footman1);
		scn.SetTwilight(10);
		assertInZone(Zone.HAND, footman1);

		// The fellowship at Lake-Town: the Footman in the Shadow hand reads 1.
		assertEquals("30_63", scn.GetCurrentSite().getBlueprintId());
		assertEquals(1, scn.GetTwilightCost(footman1));

		scn.FreepsPass();

		// Moved on to Erebor (site 7), the first Orc of the turn costs its printed 3.
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetTwilightCost(footman1));
		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(footman1);
		assertInZone(Zone.SHADOW_CHARACTERS, footman1);
		assertEquals(pool - 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
