package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_058_Tests
{

	// The usual Fellowship sites.  Site 2, Ettenmoors, is plains and not a marsh.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foul", "2_58");
					// Goblin Patrol Troop: a [moria] Orc with no text.
					put("troop", "1_177");
					// Thrashing Tentacle: a tentacle for the draw deck; it is never played here.
					put("thrashing", "101_33");
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
					put("foul", "2_58");
					put("foul2", "2_58");

					// Two tentacles of different cost and set, for the draw deck: Thrashing Tentacle (2, fierce)
					// and Strong Tentacle (3, strength +1 for each tentacle).
					put("thrashing", "101_33");
					put("strong", "19_20");
					// Goblin Runner: a [moria] minion that is no tentacle (cost 1).  "When you play this minion, you
					// may add (2)", which it only does if played.
					put("runner", "1_178");
					// Watcher in the Water, Keeper of Westgate: a [moria] Creature that is no tentacle (cost 4).
					put("keeper", "2_73");

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
	public void FoulTentacleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Foul Tentacle
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Creature
		 * Strength: 4
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: <b>Tentacle</b>.<br>When you play this minion, you may play a tentacle from your draw deck. This minion may not bear possessions and is discarded if not at a marsh.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("foul");

		assertEquals("Foul Tentacle", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.CREATURE, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TENTACLE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedAtAMarshItMayPlayAChosenTentacleFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var foul = scn.GetShadowCard("foul");
		var thrashing = scn.GetShadowCard("thrashing");
		var strong = scn.GetShadowCard("strong");
		var runner = scn.GetShadowCard("runner");
		var keeper = scn.GetShadowCard("keeper");
		scn.MoveCardsToHand(foul);

		scn.StartGame();
		scn.SetShadowDrawDeck(thrashing, strong, runner, keeper);
		// 8 set + 1 for Frodo + 1 for Midgewater Marshes.  After the tentacle's own 2 and 2 for roaming, 6 is left:
		// enough for any of the four cards in the deck (the Watcher, 4 and 2 for roaming, is the dearest).
		scn.SetTwilight(8);
		scn.FreepsPass();

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.DECK, thrashing, strong, runner, keeper);
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.ShadowPlayCard(foul);
		assertInZone(Zone.SHADOW_CHARACTERS, foul);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(foul));
		scn.ShadowAcceptOptionalTrigger();

		// The deck is shown first, every card in it.
		assertTrue(scn.ShadowHasCardChoiceAvailable(thrashing, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(keeper, false));
		scn.ShadowDismissRevealedCards();

		// Both tentacles; not the [moria] Orc nor the [moria] Creature who is no tentacle, though the pool could pay
		// for either.  A play from a draw deck is a choice of 0 to 1 (Q-0019).
		assertTrue(scn.ShadowHasCardChoiceAvailable(thrashing, strong));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, keeper));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(strong);

		assertInZone(Zone.SHADOW_CHARACTERS, foul, strong);
		assertInZone(Zone.DECK, thrashing, runner, keeper);
		// The deck looked through is shuffled as the tentacle leaves it, and again once it is played.
		assertEquals(shadowShuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferPlaysNothingAndLeavesTheDeckUnshuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var foul = scn.GetShadowCard("foul");
		var thrashing = scn.GetShadowCard("thrashing");
		var strong = scn.GetShadowCard("strong");
		scn.MoveCardsToHand(foul);

		scn.StartGame();
		scn.SetShadowDrawDeck(thrashing, strong);
		scn.SetTwilight(8);
		scn.FreepsPass();

		assertInZone(Zone.DECK, thrashing, strong);
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(foul);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(foul));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, foul);
		assertInZone(Zone.DECK, thrashing, strong);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherTentaclePlayedLaterGetsNoOfferAndASecondCopyOffersOnlyItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var foul = scn.GetShadowCard("foul");
		var foul2 = scn.GetShadowCard("foul2");
		var thrashing = scn.GetShadowCard("thrashing");
		var strong = scn.GetShadowCard("strong");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(thrashing, foul2);

		scn.StartGame();
		scn.SetShadowDrawDeck(strong, runner);
		// 12 set + 1 for Frodo + 1 for Midgewater Marshes: 4 for the Thrashing Tentacle, 4 for the second Foul
		// Tentacle, and 6 left, enough for the Strong Tentacle in the deck (3 and 2 for roaming).
		scn.SetTwilight(12);
		scn.FreepsPass();
		scn.MoveMinionsToTable(foul);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.SHADOW_CHARACTERS, foul);
		assertInZone(Zone.DECK, strong, runner);
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		// A tentacle played while this one is in play does not set its trigger off.
		scn.ShadowPlayCard(thrashing);
		assertInZone(Zone.SHADOW_CHARACTERS, thrashing);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second copy played makes its own offer, and the copy in play makes none.
		scn.ShadowPlayCard(foul2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(foul2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(foul));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowDismissRevealedCards();

		// Looked through, with nothing chosen: nothing is played and the deck is shuffled once.
		assertTrue(scn.ShadowHasCardChoiceAvailable(strong));
		scn.ShadowDeclineChoosing();

		assertInZone(Zone.SHADOW_CHARACTERS, foul, foul2, thrashing);
		assertInZone(Zone.DECK, strong, runner);
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtASiteThatIsNotAMarshItIsDiscardedAndItsWhenPlayedTextIsIgnored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foul = scn.GetShadowCard("foul");
		var troop = scn.GetShadowCard("troop");
		var thrashing = scn.GetShadowCard("thrashing");
		scn.MoveCardsToHand(foul);

		scn.StartGame();
		scn.SetShadowDrawDeck(thrashing);
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.DECK, thrashing);
		assertEquals(0, scn.GetShadowDiscardCount());
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(foul);

		// Discarded as it comes into play, so its "when you play" text is not offered, with a tentacle in the deck
		// for it to play.  The Orc beside it is no more at a marsh than it was, and stays.
		assertInZone(Zone.DISCARD, foul);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DECK, thrashing);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCopyStaysAtAMarshAndIsDiscardedWhenTheFellowshipMovesToASiteThatIsNotOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var foul = scn.GetShadowCard("foul");
		var foul2 = scn.GetShadowCard("foul2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(foul, foul2, troop);

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.SHADOW_CHARACTERS, foul, foul2, troop);

		// Nobody is assigned, so the minions are all still there when the Free Peoples player decides whether to move.
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SHADOW_CHARACTERS, foul, foul2, troop);

		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.DISCARD, foul, foul2);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StaysAtAPlainsSiteThatHasGainedMarsh() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foul = scn.GetShadowCard("foul");
		var thrashing = scn.GetShadowCard("thrashing");
		var watcher = scn.GetShadowCard("watcher");
		scn.MoveCardsToHand(foul);

		scn.StartGame();
		scn.SetShadowDrawDeck(thrashing);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		scn.MoveMinionsToTable(watcher);
		scn.ShadowRefreshDecision();

		// Ettenmoors is plains, and a marsh while the Watcher is in play.
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));

		scn.ShadowPlayCard(foul);

		assertInZone(Zone.SHADOW_CHARACTERS, foul, watcher);
		// It stayed, so it was played, and its "when you play" text is offered.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(foul));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, foul, watcher);
		assertInZone(Zone.DECK, thrashing);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedAsBearerOfAPossessionThatOtherMoriaMinionsMayBearButMayBearACondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var foul = scn.GetShadowCard("foul");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var gollum = scn.GetShadowCard("gollum");
		var spear = scn.GetShadowCard("spear");
		var deal = scn.GetShadowCard("deal");
		scn.MoveCardsToHand(spear, deal);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPass();
		scn.MoveMinionsToTable(foul, troop, runner, gollum);
		scn.ShadowRefreshDecision();

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.SHADOW_CHARACTERS, foul, troop, runner, gollum);

		// The Spear may go on a [moria] minion: the two Orcs are offered, and the tentacle, a [moria] minion
		// too, is not.  Gollum is no [moria] minion.
		assertTrue(scn.ShadowPlayAvailable(spear));
		scn.ShadowPlayCard(spear);
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(foul, gollum));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);
		assertAttachedTo(spear, troop);

		// A condition is no possession: the tentacle may bear it.
		assertTrue(scn.ShadowPlayAvailable(deal));
		scn.ShadowPlayCard(deal);
		assertTrue(scn.ShadowHasCardChoiceAvailable(foul, troop, runner));
		scn.ShadowChooseCard(foul);

		assertAttachedTo(deal, foul);
		assertAttachedTo(spear, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
