package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_021_Tests
{

	// The usual Fellowship sites.  Site 2, Ettenmoors, is plains and not a marsh.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("watcher", "19_21");

					// Huge Tentacle: a tentacle, discarded if not at a marsh.  Its "When you play this minion, you may
					// play Watcher in the Water from your draw deck" only answers its own play from hand.
					put("huge", "2_66");
					// Vile Tentacle: a tentacle that is not discarded away from a marsh (strength -4 there).  Its
					// Shadow ability is never used.
					put("vile", "101_36");
					// Thrashing Tentacle: a tentacle, discarded if not at a marsh; its other text answers a lost
					// skirmish only.
					put("thrashing", "101_33");
					// Goblin Runner: a [moria] Orc, no tentacle.  "When you play this minion, you may add (2)."
					put("runner", "1_178");
					// Goblin Spear: a possession, "Bearer must be a [moria] minion."  Its other text is about killing
					// a companion.
					put("spear", "1_182");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same path with Midgewater Marshes as site 2, so that the tentacles laid there before the Watcher stay.
	protected VirtualTableScenario GetMarshScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("watcher", "19_21");

					// Huge Tentacle and Vile Tentacle: tentacles of two titles (see above).
					put("huge", "2_66");
					put("vile", "101_36");
					// Goblin Runner: a [moria] Orc (see above).
					put("runner", "1_178");
					// Half-troll of Far Harad: an [orc] Half-troll.  Its only text is a start-of-assignment trigger.
					put("halftroll", "14_12");
					// Scurrying Goblin: an [orc] Orc, cost 1.  Its "When you play this minion at an underground
					// site" does nothing at Midgewater Marshes.
					put("scurrying", "11_137");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					// Midgewater Marshes: "Marsh. Each time a Hobbit moves to Midgewater Marshes, that Hobbit must
					// exert."  Frodo arrives with a wound, which nothing here turns on.
					put("site2", "1_332");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WatcherintheWaterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Watcher in the Water, Many-tentacled Creature
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Creature
		 * Strength: 11
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: Discard all other minions (except tentacles).<br>Watcher in the Water cannot bear possessions.<br>The fellowship's current site gains <b>marsh</b>.<br><b>Shadow:</b> Play a tentacle from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("watcher");

		assertEquals("Watcher in the Water", card.getBlueprint().getTitle());
		assertEquals("Many-tentacled Creature", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.CREATURE, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedItDiscardsEveryOtherMinionButTentaclesAndAMinionPlayedAfterItIsDiscardedAsItComesIntoPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var watcher = scn.GetShadowCard("watcher");
		var huge = scn.GetShadowCard("huge");
		var vile = scn.GetShadowCard("vile");
		var runner = scn.GetShadowCard("runner");
		var halftroll = scn.GetShadowCard("halftroll");
		var scurrying = scn.GetShadowCard("scurrying");
		scn.MoveCardsToHand(watcher, scurrying);
		assertInZone(Zone.HAND, watcher, scurrying);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(huge, vile, runner, halftroll);
		scn.ShadowRefreshDecision();

		// At the marsh, before the Watcher: every minion stands.
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.SHADOW_CHARACTERS, huge, vile, runner, halftroll);
		assertInZone(Zone.HAND, watcher, scurrying);
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.ShadowPlayCard(watcher);

		// A [moria] Orc and an [orc] Half-troll go; two tentacles of different titles and the Watcher stay.
		assertInZone(Zone.DISCARD, runner, halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, watcher, huge, vile);
		assertEquals(2, scn.GetShadowDiscardCount());

		// An [orc] Orc played with the Watcher in play goes as it comes into play.
		scn.ShadowPlayCard(scurrying);
		assertInZone(Zone.DISCARD, scurrying);
		assertEquals(3, scn.GetShadowDiscardCount());
		assertInZone(Zone.SHADOW_CHARACTERS, watcher, huge, vile);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBearAPossessionThatAMoriaOrcMayBear() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher = scn.GetShadowCard("watcher");
		var vile = scn.GetShadowCard("vile");
		var runner = scn.GetShadowCard("runner");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(watcher, spear);
		assertInZone(Zone.HAND, watcher, spear);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(vile, runner);
		scn.ShadowRefreshDecision();

		// Without the Watcher, the Spear may go on the Goblin Runner, a [moria] minion.  The Vile Tentacle may bear
		// no possession.
		assertInZone(Zone.SHADOW_CHARACTERS, vile, runner);
		assertTrue(scn.ShadowPlayAvailable(spear));

		scn.ShadowPlayCard(watcher);
		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.SHADOW_CHARACTERS, watcher, vile);

		// The Watcher is a [moria] minion and the Spear's only other bearer is gone: it cannot be played.
		assertInZone(Zone.HAND, spear);
		assertFalse(scn.ShadowPlayAvailable(spear));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFellowshipsCurrentSiteGainsMarshAndOnlyThatSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher = scn.GetShadowCard("watcher");
		var huge = scn.GetShadowCard("huge");

		scn.StartGame();
		scn.FreepsPass();

		var ettenmoors = scn.GetCurrentSite();
		var bagEnd = scn.GetFreepsSite(1);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(ettenmoors, Keyword.MARSH));
		assertTrue(scn.HasKeyword(ettenmoors, Keyword.PLAINS));

		scn.MoveMinionsToTable(watcher);
		scn.ShadowRefreshDecision();

		// Ettenmoors is a marsh while the Watcher is in play; Bag End, the site behind, is not.
		assertTrue(scn.HasKeyword(ettenmoors, Keyword.MARSH));
		assertTrue(scn.HasKeyword(ettenmoors, Keyword.PLAINS));
		assertFalse(scn.HasKeyword(bagEnd, Keyword.MARSH));

		// A tentacle discarded away from a marsh stays.
		scn.MoveMinionsToTable(huge);
		assertInZone(Zone.SHADOW_CHARACTERS, watcher, huge);

		// Nobody is assigned, so both minions are still there when the Free Peoples player decides whether to move.
		scn.SkipToMovementDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, watcher, huge);
		scn.FreepsChooseToMove();

		// Rivendell Valley is now the fellowship's current site, and the marsh has gone with it.
		var valley = scn.GetCurrentSite();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(valley, Keyword.MARSH));
		assertFalse(scn.HasKeyword(ettenmoors, Keyword.MARSH));
		assertInZone(Zone.SHADOW_CHARACTERS, watcher, huge);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityPlaysATentacleOfEitherTitleFromItsOwnersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher = scn.GetShadowCard("watcher");
		var vile = scn.GetShadowCard("vile");
		var thrashing = scn.GetShadowCard("thrashing");
		var runner = scn.GetShadowCard("runner");
		// The Free Peoples player's own tentacle, of a title the Shadow player's pile does not hold.
		var freepsHuge = scn.GetFreepsCard("huge");

		scn.StartGame();
		scn.MoveCardsToDiscard(vile, thrashing, runner, freepsHuge);
		assertInZone(Zone.DISCARD, vile, thrashing, runner, freepsHuge);
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(watcher);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.DISCARD, vile, thrashing, runner, freepsHuge);
		assertTrue(scn.ShadowActionAvailable(watcher));
		scn.ShadowUseCardAction(watcher);

		// Two tentacles are offered; a [moria] minion that is no tentacle, which the pool could pay for, is not,
		// nor is the other player's tentacle in his own pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(vile, thrashing));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, freepsHuge));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(thrashing);

		// It is played, and stays at Ettenmoors, which the Watcher makes a marsh.
		assertInZone(Zone.SHADOW_CHARACTERS, thrashing, watcher);
		assertInZone(Zone.DISCARD, vile, runner, freepsHuge);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoTentacleInTheDiscardPileOrNoTwilightToPlayOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher = scn.GetShadowCard("watcher");
		var thrashing = scn.GetShadowCard("thrashing");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveCardsToDiscard(runner);
		assertInZone(Zone.DISCARD, runner);
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(watcher);
		scn.ShadowRefreshDecision();

		// No tentacle in the discard pile: only a [moria] Orc.
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(watcher));

		// A tentacle there, and the pool one short of it: 2 to play + 2 roaming at site 2.
		scn.MoveCardsToDiscard(thrashing);
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.DISCARD, thrashing);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(watcher));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(watcher));
		scn.ShadowUseCardAction(watcher);

		// The only tentacle is played with no choice asked.
		assertInZone(Zone.SHADOW_CHARACTERS, thrashing);
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher = scn.GetShadowCard("watcher");
		var thrashing = scn.GetShadowCard("thrashing");

		scn.StartGame();
		scn.MoveCardsToDiscard(thrashing);
		assertInZone(Zone.DISCARD, thrashing);
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(watcher);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.DISCARD, thrashing);
		assertTrue(scn.ShadowActionAvailable(watcher));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.GetTwilight() >= 4);
		assertFalse(scn.ShadowActionAvailable(watcher));
		assertInZone(Zone.DISCARD, thrashing);
	}
}
