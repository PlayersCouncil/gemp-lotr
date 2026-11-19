package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_065_Tests
{

	// The Hobbit table, on the Hobbit path with the Free Peoples Pack's Ravenhill in place of Battle of the Five
	// Armies as site 9.  Site 8 is Smaug's Den 30_56 (a Shadow ability to play Smaug, not used).  Bilbo, Reliable
	// Companion 30_46 is the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Minions in play, of two cultures and two races.
					put("aggressor", "30_39");   // Orkish Aggressor: [gundabad] Orc, cost 2; an Assignment ability, not used
					put("troll", "32_31");       // Demolition Troll: [moria] Troll, cost 5; a Skirmish ability, not used
					// Minions in the discard pile, of two cultures and two races.
					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc, cost 6, no text
					put("ferny", "33_22");       // Bill Ferny Sr.: [gundabad] Man, cost 2; bars on assigning, a regroup ability, not used
					put("watchful", "30_40");    // Watchful Orc: [gundabad] Orc, cost 4, kept in hand
					// Yazneg, Orkish Assassin: [gundabad] Orc; "Maneuver: Remove (3) and discard this minion from
					// play to discard a follower", which takes the Shadow player's only minion away.
					put("yazneg", "30_41");
					put("pinned", "1_192");      // Pinned Down: a [moria] condition
					put("scimitar", "1_180");    // Goblin Scimitar: a [moria] possession
					put("balin", "30_2");        // Balin, Brother of Dwalin: [dwarven] companion; a fellowship ability, not used
				}},
				new HashMap<>()
				{{
					put("site1", "30_49");
					put("site2", "30_50");
					put("site3", "30_51");
					put("site4", "30_52");
					put("site5", "30_53");
					put("site6", "30_54");
					put("site7", "30_55");
					put("site8", "30_56");
					put("site9", "30_65");
				}},
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void RavenhillStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Ravenhill
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 9
		 * Shadow Number: 8
		 * Direction: none
		 * Game Text: <b>Mountain</b>. <b>Maneuver:</b> Remove your minion from the game to play a minion from your
		 * discard pile. Its twilight cost is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Ravenhill", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
	}

	@Test
	public void ManeuverActionRemovesYourMinionFromTheGameToPlayAMinionFromYourDiscardPileForTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var aggressor = scn.GetShadowCard("aggressor");
		var troll = scn.GetShadowCard("troll");
		var troop = scn.GetShadowCard("troop");
		var ferny = scn.GetShadowCard("ferny");
		var watchful = scn.GetShadowCard("watchful");
		var pinned = scn.GetShadowCard("pinned");
		var scimitar = scn.GetShadowCard("scimitar");
		var freepsWatchful = scn.GetFreepsCard("watchful");

		scn.StartGame();
		scn.SkipToSite(8);
		// The turns skipped have drawn cards: every pile is laid now.  A pile's choice is read by blueprint, so the
		// other player's minion in his own discard pile is of a title that lies in no discard pile of this player.
		scn.MoveCompanionsToTable(balin);
		scn.SetShadowHand(watchful);
		scn.MoveCardsToDiscard(troop, ferny, pinned, scimitar, freepsWatchful);
		scn.MoveMinionsToTable(aggressor, troll);
		assertInZone(Zone.FREE_CHARACTERS, balin);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor, troll);
		assertInZone(Zone.DISCARD, troop, ferny, pinned, scimitar, freepsWatchful);
		assertInHand(watchful);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.MANEUVER);

		var ravenhill = scn.GetCurrentSite();
		assertEquals("30_65", ravenhill.getBlueprintId());
		// The Free Peoples player, asked first, has no minion of his own (and one in his discard pile).
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ravenhill));
		scn.FreepsPass();

		// Twilight for the Troop only with the discount: 6 - 2.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(ravenhill));

		scn.ShadowUseCardAction(ravenhill);
		// Either minion of his; no companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, troll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(balin, scn.GetRingBearer()));
		scn.ShadowChooseCard(troll);
		assertInZone(Zone.REMOVED, troll);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);

		// Minions of two cultures and races; not a Shadow condition or possession in the same pile, a minion in
		// hand, or the other player's minion in his own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, ferny));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(pinned, scimitar, watchful, freepsWatchful));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(troop);

		assertInZone(Zone.SHADOW_CHARACTERS, troop, aggressor);
		assertInZone(Zone.REMOVED, troll);
		assertInZone(Zone.DISCARD, ferny, pinned, scimitar, freepsWatchful);
		// 4 - (6 for the Troop - 2)
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOnlyAConditionAPossessionOrAMinionThePoolCannotPayForEvenAtTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aggressor = scn.GetShadowCard("aggressor");
		var troop = scn.GetShadowCard("troop");
		var pinned = scn.GetShadowCard("pinned");
		var scimitar = scn.GetShadowCard("scimitar");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.SetShadowHand();
		scn.MoveCardsToDiscard(pinned, scimitar);
		scn.MoveMinionsToTable(aggressor);
		assertInZone(Zone.DISCARD, pinned, scimitar);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		var ravenhill = scn.GetCurrentSite();
		assertEquals("30_65", ravenhill.getBlueprintId());
		// A condition and a possession in the pile, and twilight enough for either.
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ravenhill));

		// The Troop, cost 6, in the pile with (3): one short of 6 - 2.
		scn.MoveCardsToDiscard(troop);
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.DISCARD, troop);
		assertFalse(scn.ShadowActionAvailable(ravenhill));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(ravenhill));
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
	}

	@Test
	public void IsNotOfferedOnceTheShadowPlayerHasNoMinionLeftToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var yazneg = scn.GetShadowCard("yazneg");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.SetShadowHand();
		scn.MoveCardsToDiscard(troop);
		scn.MoveMinionsToTable(yazneg);
		assertInZone(Zone.DISCARD, troop);
		assertInZone(Zone.SHADOW_CHARACTERS, yazneg);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		var ravenhill = scn.GetCurrentSite();
		assertEquals("30_65", ravenhill.getBlueprintId());
		// (3) for Yazneg's ability and (4) left for the Troop at 6 - 2.
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(ravenhill));
		assertTrue(scn.ShadowActionAvailable(yazneg));

		scn.ShadowUseCardAction(yazneg);
		assertInZone(Zone.DISCARD, yazneg, troop);
		assertEquals(4, scn.GetTwilight());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ravenhill));
	}
}
