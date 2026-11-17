package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_066_Tests
{

	// The usual Fellowship sites.  Site 2, Ettenmoors, is plains and not a marsh.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("huge", "2_66");
					// Goblin Patrol Troop: a [moria] Orc with no text.
					put("troop", "1_177");
					// Watcher in the Water, Keeper of Westgate: a Watcher for the draw deck; it is never played here.
					put("keeper", "2_73");
					// Watcher in the Water, Many-tentacled Creature: "The fellowship's current site gains marsh."  It
					// also discards every other minion that is no tentacle, and none is in play beside it.
					put("watcher", "19_21");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same path with Midgewater Marshes as site 2, so that the first Shadow phase is at a marsh and a second
	// move in the regroup phase leaves it for Rivendell Valley, which is not one.
	protected VirtualTableScenario GetMarshScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("huge", "2_66");
					put("huge2", "2_66");

					// The two cards titled Watcher in the Water, for the draw deck: Keeper of Westgate (set 2, damage +1,
					// "each tentacle is strength +2 and damage +1") and Many-tentacled Creature (set 19).  Each costs 4
					// and discards every other minion that is no tentacle.
					put("keeper", "2_73");
					put("watcher", "19_21");
					// Vile Tentacle: a tentacle of another title (cost 2); not discarded away from a marsh.  Its
					// Shadow ability is never used.
					put("vile", "101_36");
					// Goblin Runner: a [moria] minion of another title (cost 1).  "When you play this minion, you may
					// add (2)", which it only does if played.
					put("runner", "1_178");

					// Goblin Patrol Troop: a [moria] Orc with no text.
					put("troop", "1_177");
					// Goblin Spear: a possession, "Bearer must be a [moria] minion."  Its other text is about killing
					// a companion.
					put("spear", "1_182");
					// Let Her Deal With Them: a condition, "To play, spot Gollum or Smeagol. Bearer must be a minion."
					// Its damage bonus and its play from the discard pile reach no reading here.
					put("deal", "7_63");
					// Gollum, Nasty Treacherous Creature: a [gollum] minion to spot for the condition; his regroup
					// ability is never used.
					put("gollum", "5_24");
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
	public void HugeTentacleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Huge Tentacle
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Creature
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Tentacle</b>. <b>Damage +1</b>.<br>When you play this minion, you may play Watcher in the Water from your draw deck.<br>This minion may not bear possessions and is discarded if not at a marsh.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("huge");

		assertEquals("Huge Tentacle", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.CREATURE, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TENTACLE));
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedAtAMarshItMayPlayEitherWatcherInTheWaterFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var huge = scn.GetShadowCard("huge");
		var keeper = scn.GetShadowCard("keeper");
		var watcher = scn.GetShadowCard("watcher");
		var vile = scn.GetShadowCard("vile");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(huge);

		scn.StartGame();
		scn.SetShadowDrawDeck(keeper, watcher, vile, runner);
		// 10 set + 1 for Frodo + 1 for Midgewater Marshes.  After the tentacle's own 2 and 2 for roaming, 8 is left:
		// enough for any of the four cards in the deck (a Watcher, 4 and 2 for roaming, is the dearest).
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertEquals(12, scn.GetTwilight());
		assertInZone(Zone.DECK, keeper, watcher, vile, runner);
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.ShadowPlayCard(huge);
		assertInZone(Zone.SHADOW_CHARACTERS, huge);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(huge));
		scn.ShadowAcceptOptionalTrigger();

		// The deck is shown first, every card in it.
		assertTrue(scn.ShadowHasCardChoiceAvailable(keeper, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, false));
		scn.ShadowDismissRevealedCards();

		// Both cards titled Watcher in the Water; not a tentacle of another title nor a [moria] minion of another
		// title, though the pool could pay for either.  A play from a draw deck is a choice of 0 to 1 (Q-0019).
		assertTrue(scn.ShadowHasCardChoiceAvailable(keeper, watcher));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(vile, runner));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(keeper);

		// The Watcher discards every other minion but a tentacle, so the tentacle stays beside it.
		assertInZone(Zone.SHADOW_CHARACTERS, huge, keeper);
		assertInZone(Zone.DECK, watcher, vile, runner);
		// The deck looked through is shuffled as the Watcher leaves it, and again once it is played.
		assertEquals(shadowShuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferPlaysNothingAndLeavesTheDeckUnshuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var huge = scn.GetShadowCard("huge");
		var keeper = scn.GetShadowCard("keeper");
		var watcher = scn.GetShadowCard("watcher");
		scn.MoveCardsToHand(huge);

		scn.StartGame();
		scn.SetShadowDrawDeck(keeper, watcher);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInZone(Zone.DECK, keeper, watcher);
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(huge);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(huge));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, huge);
		assertInZone(Zone.DECK, keeper, watcher);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherTentaclePlayedLaterGetsNoOfferAndASecondCopyOffersOnlyItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var huge = scn.GetShadowCard("huge");
		var huge2 = scn.GetShadowCard("huge2");
		var vile = scn.GetShadowCard("vile");
		var keeper = scn.GetShadowCard("keeper");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(vile, huge2);

		scn.StartGame();
		scn.SetShadowDrawDeck(keeper, runner);
		// 12 set + 1 for Frodo + 1 for Midgewater Marshes: 4 for the Vile Tentacle, 4 for the second Huge Tentacle,
		// and 6 left, enough for the Watcher in the deck (4 and 2 for roaming).
		scn.SetTwilight(12);
		scn.FreepsPass();
		scn.MoveMinionsToTable(huge);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.SHADOW_CHARACTERS, huge);
		assertInZone(Zone.DECK, keeper, runner);
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		// A tentacle played while this one is in play does not set its trigger off.
		scn.ShadowPlayCard(vile);
		assertInZone(Zone.SHADOW_CHARACTERS, vile);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second copy played makes its own offer, and the copy in play makes none.
		scn.ShadowPlayCard(huge2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(huge2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(huge));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowDismissRevealedCards();

		// Looked through, with nothing chosen: nothing is played and the deck is shuffled once.
		assertTrue(scn.ShadowHasCardChoiceAvailable(keeper));
		scn.ShadowDeclineChoosing();

		assertInZone(Zone.SHADOW_CHARACTERS, huge, huge2, vile);
		assertInZone(Zone.DECK, keeper, runner);
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtASiteThatIsNotAMarshItIsDiscardedAndItsWhenPlayedTextIsIgnored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var huge = scn.GetShadowCard("huge");
		var troop = scn.GetShadowCard("troop");
		var keeper = scn.GetShadowCard("keeper");
		scn.MoveCardsToHand(huge);

		scn.StartGame();
		scn.SetShadowDrawDeck(keeper);
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.DECK, keeper);
		assertEquals(0, scn.GetShadowDiscardCount());
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(huge);

		// Discarded as it comes into play, so its "when you play" text is not offered, with a Watcher in the deck
		// for it to play.  The Orc beside it is no more at a marsh than it was, and stays.
		assertInZone(Zone.DISCARD, huge);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DECK, keeper);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCopyStaysAtAMarshAndIsDiscardedWhenTheFellowshipMovesToASiteThatIsNotOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var huge = scn.GetShadowCard("huge");
		var huge2 = scn.GetShadowCard("huge2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(huge, huge2, troop);

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.SHADOW_CHARACTERS, huge, huge2, troop);

		// Nobody is assigned, so the minions are all still there when the Free Peoples player decides whether to move.
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SHADOW_CHARACTERS, huge, huge2, troop);

		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.DISCARD, huge, huge2);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StaysAtAPlainsSiteThatHasGainedMarsh() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var huge = scn.GetShadowCard("huge");
		var keeper = scn.GetShadowCard("keeper");
		var watcher = scn.GetShadowCard("watcher");
		scn.MoveCardsToHand(huge);

		scn.StartGame();
		scn.SetShadowDrawDeck(keeper);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		scn.MoveMinionsToTable(watcher);
		scn.ShadowRefreshDecision();

		// Ettenmoors is plains, and a marsh while the Watcher is in play.
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));

		scn.ShadowPlayCard(huge);

		assertInZone(Zone.SHADOW_CHARACTERS, huge, watcher);
		// It stayed, so it was played, and its "when you play" text is offered.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(huge));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, huge, watcher);
		assertInZone(Zone.DECK, keeper);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedAsBearerOfAPossessionThatOtherMoriaMinionsMayBearButMayBearACondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var huge = scn.GetShadowCard("huge");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var gollum = scn.GetShadowCard("gollum");
		var spear = scn.GetShadowCard("spear");
		var deal = scn.GetShadowCard("deal");
		scn.MoveCardsToHand(spear, deal);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPass();
		scn.MoveMinionsToTable(huge, troop, runner, gollum);
		scn.ShadowRefreshDecision();

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.SHADOW_CHARACTERS, huge, troop, runner, gollum);

		// The Spear may go on a [moria] minion: the two Orcs are offered, and the tentacle, a [moria] minion
		// too, is not.  Gollum is no [moria] minion.
		assertTrue(scn.ShadowPlayAvailable(spear));
		scn.ShadowPlayCard(spear);
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(huge, gollum));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);
		assertAttachedTo(spear, troop);

		// A condition is no possession: the tentacle may bear it.
		assertTrue(scn.ShadowPlayAvailable(deal));
		scn.ShadowPlayCard(deal);
		assertTrue(scn.ShadowHasCardChoiceAvailable(huge, troop, runner));
		scn.ShadowChooseCard(huge);

		assertAttachedTo(deal, huge);
		assertAttachedTo(spear, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
