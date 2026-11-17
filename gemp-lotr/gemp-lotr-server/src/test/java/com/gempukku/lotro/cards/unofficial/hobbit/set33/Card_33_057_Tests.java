package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_057_Tests
{

	// The Hobbit table, with The Last Homely House as site 3 in place of Rivendell 30_51, and Great East Road 33_56
	// as site 2 in place of The Troll Hoard 30_50: no card here is a possession or artifact, so Great East Road makes
	// no start-of-turn offer on the way.  Bilbo, Reliable Companion 30_46 is the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Allies whose home is site 3 (3H), of two cultures and races.
					put("lindir", "33_14");     // Lindir, Elven Minstrel: [elven] Elf; a move trigger heals an [elven] ally
					put("saruman", "32_24");    // Saruman, The White: [gandalf] Wizard; Gandalf is defender +1, abilities unused
					// Allies whose home is not this site 3.
					put("worrywort", "33_53");  // Master Worrywort, Hobbit Farmer: [shire] Hobbit, home 1H
					put("radagast", "31_17");   // Radagast, The Brown: [gandalf] Wizard, home 5H
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: [elven] Elf, home 3F, a Fellowship block site 3
					// Goblin Patrol Troop: a minion with no text, for a maneuver phase
					put("troop", "1_177");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.HobbitSites);
					put("site2", "33_56");
					put("site3", "33_57");
				}},
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TheLastHomelyHouseStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: The Last Homely House
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 3
		 * Shadow Number: 1
		 * Direction: none
		 * Game Text: <b>Sanctuary</b>. <b>Fellowship:</b> Play an ally whose home is site 3 from your draw deck (limit once per turn).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("The Last Homely House", card.getBlueprint().getTitle());
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
	public void FellowshipActionPlaysAnAllyWhoseHomeIsSite3FromTheDrawDeckOnceThisTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lindir = scn.GetFreepsCard("lindir");
		var saruman = scn.GetFreepsCard("saruman");
		var worrywort = scn.GetFreepsCard("worrywort");
		var radagast = scn.GetFreepsCard("radagast");
		var silinde = scn.GetFreepsCard("silinde");

		scn.StartGame();
		// Player 1's third turn begins at The Last Homely House.
		scn.SkipToSite(3);
		scn.SetFreepsDrawDeck(lindir, saruman, worrywort, radagast, silinde);
		scn.FreepsRefreshDecision();

		var site = scn.GetCurrentSite();
		assertEquals("33_57", site.getBlueprintId());
		assertInZone(Zone.DECK, lindir, saruman, worrywort, radagast, silinde);
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(site));

		scn.FreepsUseCardAction(site);
		scn.FreepsDismissRevealedCards();
		// Allies of home 3H of two cultures and races; not allies of homes 1H and 5H, nor of the Fellowship block's
		// site 3.
		assertTrue(scn.FreepsHasCardChoiceAvailable(lindir, saruman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(worrywort, radagast, silinde));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(saruman);

		assertInZone(Zone.SUPPORT, saruman);
		assertInZone(Zone.DECK, lindir, worrywort, radagast, silinde);
		// Shuffled as the ally leaves the deck that was looked through, and again once the look is over; the other
		// player's deck is not.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());

		// Once this turn: Lindir is still in the deck.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(site));
	}

	@Test
	public void TheLimitIsSpentForTheTurnAndOnTheNextTurnTheOtherPlayerUsesItFromHisOwnDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lindir = scn.GetFreepsCard("lindir");
		var shadowSaruman = scn.GetShadowCard("saruman");
		var shadowWorrywort = scn.GetShadowCard("worrywort");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsDrawDeck(lindir);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.DECK, lindir);

		var site = scn.GetCurrentSite();
		scn.FreepsUseCardAction(site);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(lindir);
		assertInZone(Zone.SUPPORT, lindir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(site));

		// Player 2's turn begins at the same site.  His deck is laid as it begins.
		scn.SkipToOtherPlayersTurn();
		scn.SetShadowDrawDeck(shadowSaruman, shadowWorrywort);
		scn.ShadowRefreshDecision();

		assertSame(site, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertInZone(Zone.DECK, shadowSaruman, shadowWorrywort);
		int shuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertTrue(scn.ShadowActionAvailable(site));

		scn.ShadowUseCardAction(site);
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(shadowSaruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shadowWorrywort));
		scn.ShadowChooseCardBPFromSelection(shadowSaruman);

		assertInZone(Zone.SUPPORT, shadowSaruman);
		assertInZone(Zone.DECK, shadowWorrywort);
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertFalse(scn.ShadowActionAvailable(site));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void WithNoAllyWhoseHomeIsSite3InTheDrawDeckTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worrywort = scn.GetFreepsCard("worrywort");
		var radagast = scn.GetFreepsCard("radagast");
		var silinde = scn.GetFreepsCard("silinde");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsDrawDeck(worrywort, radagast, silinde);
		scn.FreepsRefreshDecision();

		assertEquals("33_57", scn.GetCurrentSite().getBlueprintId());
		assertInZone(Zone.DECK, worrywort, radagast, silinde);
		int shuffles = scn.GetFreepsDeckShuffleCount();
		// Nothing the site could play: a play from the draw deck on a Hobbit-set card, with nothing it could play, is
		// withheld (Q-0019; the orchestrator on Q-0406 and Q-0418).
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(scn.GetCurrentSite()));
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
	}

	@Test
	public void FellowshipActionIsNotOfferedInTheManeuverPhaseHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lindir = scn.GetFreepsCard("lindir");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Player 1's second turn begins at site 2; the fellowship then moves to The Last Homely House.
		scn.SkipToSite(2);
		scn.SetFreepsDrawDeck(lindir);
		scn.MoveMinionsToTable(troop);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.DECK, lindir);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals("33_57", scn.GetCurrentSite().getBlueprintId());
		assertInZone(Zone.DECK, lindir);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(scn.GetCurrentSite()));
	}
}
