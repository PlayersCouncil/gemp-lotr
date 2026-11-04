package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_188_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tracking", "15_171");  // Tracking Uruk: an [uruk-hai] Uruk-hai, (5), site 5; Hunter 1 is its whole text
					put("herdsman", "15_83");   // Hunting Herdsman: a [men] Man, (3), site 4; Hunter 1, strength +3 for each follower
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, (6), site 4; no text, and no hunter
					// Elven Guardian: a hunter who is a companion, not a minion, (2); a maneuver heal used only on purpose
					put("guardian", "17_7");
				}},
				new HashMap<>()
				{{
					// The tests lay the Shadow player's copy of the pit as the next site of a Fellowship-block path.
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("pit", "15_188");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A Shadows path: the Shadow player lays Breeding Pit of Isengard as site 2.
	protected VirtualTableScenario GetShadowsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("herdsman1", "15_83");  // Hunting Herdsman: a [men] hunter minion, (3), site 4; strength +3 for each follower
					put("herdsman2", "15_83");
					// Bold Easterling: site 4, strength 8, vitality 1. "Skirmish: If this minion is skirmishing,
					// discard this minion to play a [men] minion from your discard pile."
					put("easterling", "15_73");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");
					put("site2", "15_188");
					put("site3", "11_234");
					put("site4", "17_148");
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

	@Test
	public void BreedingpitofIsengardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Breeding pit of Isengard
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Underground</b>. The twilight cost of the first hunter minion played each Shadow phase is -2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("pit");

		assertEquals("Breeding pit of Isengard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void OnlyTheFirstHunterMinionOfTheShadowPhaseIsTwoCheaper() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pit = scn.GetShadowSite("pit");
		var tracking = scn.GetShadowCard("tracking");
		var herdsman = scn.GetShadowCard("herdsman");
		var troop = scn.GetShadowCard("troop");
		var guardian = scn.GetFreepsCard("guardian");

		scn.StartGame();
		scn.MoveCardToAdventurePath(pit);
		scn.SetShadowHand(troop, herdsman, tracking);
		scn.SetFreepsHand(guardian);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertSame(pit, scn.GetCurrentSite());
		// 20 set + 1 companion + 0 for the pit
		assertEquals(21, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// The minion who is no hunter its printed 6; each hunter minion 2 less while none has been played: 5 - 2 and 3 - 2
		assertEquals(6, scn.GetTwilightCost(troop));
		assertEquals(3, scn.GetTwilightCost(tracking));
		assertEquals(1, scn.GetTwilightCost(herdsman));
		// A hunter companion is no hunter minion: its printed 2.
		assertEquals(2, scn.GetTwilightCost(guardian));

		scn.ShadowPlayCard(troop);
		// 6 to play + 2 roaming (site 4 at site 2)
		assertEquals(13, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		// A minion who is no hunter was not the first hunter minion of the phase.
		assertEquals(3, scn.GetTwilightCost(tracking));
		assertEquals(1, scn.GetTwilightCost(herdsman));

		scn.ShadowPlayCard(herdsman);
		// 3 - 2 to play + 2 roaming
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, herdsman);

		// The second hunter minion of the phase costs its printed 5.
		assertEquals(5, scn.GetTwilightCost(tracking));
		scn.ShadowPlayCard(tracking);
		// 5 to play + 2 roaming
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, tracking);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFirstHunterMinionPlayedInTheNextShadowPhaseAtThePitIsTwoCheaperAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pit = scn.GetShadowSite("pit");
		// Each player's own minions, for the turn on which that player is the Shadow player
		var secondPlayersHerdsman = scn.GetShadowCard("herdsman");
		var secondPlayersTracking = scn.GetShadowCard("tracking");
		var firstPlayersTracking = scn.GetFreepsCard("tracking");
		var firstPlayersHerdsman = scn.GetFreepsCard("herdsman");

		scn.StartGame();
		scn.MoveCardToAdventurePath(pit);
		scn.SetShadowHand(secondPlayersHerdsman, secondPlayersTracking);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertSame(pit, scn.GetCurrentSite());
		// 10 set + 1 companion + 0 for the pit
		assertEquals(11, scn.GetTwilight());
		assertEquals(1, scn.GetTwilightCost(secondPlayersHerdsman));
		scn.ShadowPlayCard(secondPlayersHerdsman);
		// 3 - 2 to play + 2 roaming
		assertEquals(8, scn.GetTwilight());
		assertEquals(5, scn.GetTwilightCost(secondPlayersTracking));

		// The second player's turn.  His fellowship moves from site 1 to the same pit, and the first player has the
		// Shadow phase there.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		scn.SetFreepsHand(firstPlayersTracking, firstPlayersHerdsman);
		scn.SetTwilight(10);
		scn.ShadowPass();

		assertSame(pit, scn.GetCurrentSite());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
		// 10 set + 1 companion + 0 for the pit
		assertEquals(11, scn.GetTwilight());
		// A new Shadow phase: 5 - 2 and 3 - 2 once more.
		assertEquals(3, scn.GetTwilightCost(firstPlayersTracking));
		assertEquals(1, scn.GetTwilightCost(firstPlayersHerdsman));

		scn.FreepsPlayCard(firstPlayersTracking);
		// 5 - 2 to play + 2 roaming
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, firstPlayersTracking);
		assertEquals(3, scn.GetTwilightCost(firstPlayersHerdsman));
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void OnceTheFellowshipHasLeftThePitTheFirstHunterMinionCostsItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pit = scn.GetShadowSite("pit");
		var tracking = scn.GetShadowCard("tracking");

		scn.StartGame();
		scn.MoveCardToAdventurePath(pit);
		scn.SetShadowHand(tracking);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertSame(pit, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// At the pit, with no hunter minion played yet: 5 - 2
		assertEquals(3, scn.GetTwilightCost(tracking));

		// Nothing is played there.  The fellowship moves on to site 3 and the pit lies behind it.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertSame(pit, scn.GetSite(2));
		assertTrue(scn.AwaitingShadowPhaseActions());
		// The text of a site binds only while the fellowship is at it: the Uruk costs its printed 5.
		assertEquals(5, scn.GetTwilightCost(tracking));
	}

	@Test
	public void AHunterMinionPlayedInAnotherPhaseDoesNotTakeTheDiscount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var herdsman1 = scn.GetShadowCard("herdsman1");
		var herdsman2 = scn.GetShadowCard("herdsman2");
		var easterling = scn.GetShadowCard("easterling");
		scn.MoveCardsToHand(herdsman1);
		scn.MoveCardsToDiscard(herdsman2);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertInZone(Zone.DISCARD, herdsman2);
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));

		assertEquals(2, scn.GetCurrentSiteNumber());

		// The Shadow phase: 3 to play + 2 roaming - 2 for the first hunter minion.
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(herdsman1);
		assertEquals(twilight - 3, scn.GetTwilight());

		scn.MoveMinionsToTable(easterling);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, easterling);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		twilight = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(easterling));
		scn.ShadowUseCardAction(easterling);
		// The Easterling is in the discard pile now beside the other Herdsman, which is played unasked: the Easterling
		// discarded for the cost is not offered (F-0712).

		assertInZone(Zone.DISCARD, easterling);
		assertInZone(Zone.SHADOW_CHARACTERS, herdsman2);
		// 3 to play + 2 roaming, and no discount: no hunter minion was played before it in this phase, which is
		// not a Shadow phase.
		assertEquals(twilight - 5, scn.GetTwilight());
		// The skirmish is over with its only minion gone; the Herdsman played is fierce until the regroup phase.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
