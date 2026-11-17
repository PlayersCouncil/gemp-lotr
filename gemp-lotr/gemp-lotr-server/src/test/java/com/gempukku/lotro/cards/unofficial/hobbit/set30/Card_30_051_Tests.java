package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_051_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Tom, Troll of Ettenmoors: [troll] Troll minion, cost 5, site 2, fierce.  "To play, discard
					// an Orc."  Each Troll costs 2 less while he is in play; a trigger on a companion losing.
					put("tom", "31_58");
					// Demolition Troll: a [moria] Troll minion, cost 5, site 4; a skirmish ability only.
					put("troll", "32_31");
					// Goblin Runner: a [moria] Orc minion, cost 1, site 4.  "When you play this minion, you may
					// spot another [moria] card to add (2)."
					put("runner", "31_30");
					// Goblin Sneak: a [moria] Orc minion in play, for Tom's "discard an Orc".  Its text answers
					// its own play.
					put("sneak", "31_32");
					// Azog, Commander of the Wolves Army: "Maneuver: Exert Azog at a battleground to play a
					// [moria] minion from your discard pile.  Its twilight cost is -2."
					put("azog", "32_28");
					// Watchful Orc: "Shadow: Exert this minion twice to make each site on the adventure path gain
					// battleground, mountain, forest or underground until the end of the turn."
					// It is not unique: one copy for each site, as a site laid later does not gain the keyword.
					put("watchful", "30_40");
					put("watchful2", "30_40");
					// Two [moria] Orc minions, neither a Troll.  Goblin Footman: cost 3, site 4; when played, a
					// possession may be played from the discard pile, and none is there.  Assaulting Goblin:
					// cost 2, site 4; a Response to another Orc's wound.
					put("footman", "30_42");
					put("assaulting", "32_27");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void RivendellStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Rivendell
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 3
		 * Shadow Number: 1
		 * Direction: none
		 * Game Text: <b>Sanctuary</b>. Trolls cannot be played at Rivendell.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("Rivendell", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(3, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void TrollsOfEitherCultureCannotBePlayedFromHandAtRivendell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tom = scn.GetShadowCard("tom");
		var troll = scn.GetShadowCard("troll");
		var runner = scn.GetShadowCard("runner");
		var sneak = scn.GetShadowCard("sneak");

		scn.StartGame();
		scn.SkipToSite(2);
		// The Troll Hoard's own offer, at the start of this turn.
		scn.FreepsDeclineOptionalTrigger();
		scn.SkipToPhase(Phase.SHADOW);

		scn.MoveMinionsToTable(sneak);
		scn.SetShadowHand(tom, troll, runner);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertEquals("30_51", scn.GetCurrentSite().getBlueprintId());
		assertInZone(Zone.SHADOW_CHARACTERS, sneak);
		assertInZone(Zone.HAND, tom, troll, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(tom));
		assertFalse(scn.ShadowPlayAvailable(troll));
		// A [moria] minion who is no Troll may be.
		assertTrue(scn.ShadowPlayAvailable(runner));
	}

	@Test
	public void OnceTheFellowshipHasLeftRivendellTrollsMayBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tom = scn.GetShadowCard("tom");
		var troll = scn.GetShadowCard("troll");
		var runner = scn.GetShadowCard("runner");
		var sneak = scn.GetShadowCard("sneak");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SkipToPhase(Phase.SHADOW);

		scn.MoveMinionsToTable(sneak);
		scn.SetShadowHand(tom, troll, runner);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		// Goblin Town, site 4.
		assertEquals("30_52", scn.GetCurrentSite().getBlueprintId());
		assertInZone(Zone.SHADOW_CHARACTERS, sneak);
		assertInZone(Zone.HAND, tom, troll, runner);
		assertTrue(scn.ShadowPlayAvailable(tom));
		assertTrue(scn.ShadowPlayAvailable(troll));

		scn.ShadowPlayCard(tom);

		assertInZone(Zone.SHADOW_CHARACTERS, tom);
		assertInZone(Zone.DISCARD, sneak);
		assertTrue(scn.ShadowPlayAvailable(troll));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// No Troll may be played while the fellowship is at Rivendell, from the discard pile either.  Azog's ability
	// plays a card from the discard pile, and a play that an action is used for is a cost of it (Ketura on
	// Q-0062): with a Troll the only card he could bring back, the ability is not offered.  At the site before,
	// on the same turn, it is.
	@Test
	public void ATrollCannotBePlayedFromTheDiscardPileWhileTheFellowshipIsAtRivendell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var azog = scn.GetShadowCard("azog");
		var watchful = scn.GetShadowCard("watchful");
		var watchful2 = scn.GetShadowCard("watchful2");
		var troll = scn.GetShadowCard("troll");
		var footman = scn.GetShadowCard("footman");
		var assaulting = scn.GetShadowCard("assaulting");

		scn.StartGame();
		scn.MoveMinionsToTable(azog, watchful, watchful2);
		scn.MoveCardsToDiscard(troll);
		scn.SetTwilight(20);
		assertInZone(Zone.SHADOW_CHARACTERS, azog, watchful, watchful2);
		assertInZone(Zone.DISCARD, troll);

		// Azog's ability needs a battleground, and a Watchful Orc makes every site on the path one for the turn.
		scn.FreepsPass();
		assertEquals("30_50", scn.GetCurrentSite().getBlueprintId());
		assertEquals(0, scn.GetWoundsOn(watchful));
		scn.ShadowUseCardAction(watchful);
		scn.ShadowChoose("Battleground");
		assertEquals(2, scn.GetWoundsOn(watchful));

		// The control.  At The Troll Hoard, with the Troll alone in the discard pile, Azog may bring it back.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(azog));

		// He does not, and nobody is assigned: the fellowship moves on to Rivendell in the regroup phase.
		scn.ShadowPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.SHADOW);
		assertEquals("30_51", scn.GetCurrentSite().getBlueprintId());
		assertEquals(0, scn.GetWoundsOn(watchful2));
		scn.ShadowUseCardAction(watchful2);
		scn.ShadowChoose("Battleground");
		assertEquals(2, scn.GetWoundsOn(watchful2));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetWoundsOn(azog));
		assertInZone(Zone.DISCARD, troll);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(azog));

		// Two Orcs beside the Troll in the discard pile, and the ability is there again: for them.
		scn.MoveCardsToDiscard(footman, assaulting);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.DISCARD, footman, assaulting);

		assertTrue(scn.ShadowActionAvailable(azog));

		scn.ShadowUseCardAction(azog);

		assertEquals(1, scn.GetWoundsOn(azog));
		assertTrue(scn.ShadowHasCardChoiceAvailable(footman, assaulting));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCards(footman);

		assertInZone(Zone.SHADOW_CHARACTERS, footman);
		assertInZone(Zone.DISCARD, troll, assaulting);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
