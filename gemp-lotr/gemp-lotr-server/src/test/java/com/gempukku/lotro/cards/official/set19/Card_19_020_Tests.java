package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_020_Tests
{

	// The usual Fellowship sites.  Site 2, Ettenmoors, is plains and not a marsh.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strong", "19_20");
					// Goblin Patrol Troop: a [moria] Orc with no text.
					put("troop", "1_177");
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
					put("strong", "19_20");
					put("strong2", "19_20");

					// Foul Tentacle: a tentacle of another title, strength 4.  "When you play this minion, you may
					// play a tentacle from your draw deck", declined where it is played.
					put("foul", "2_58");
					// Watcher in the Water, Many-tentacled Creature: a [moria] Creature that is no tentacle.  It
					// discards every other minion that is no tentacle; its Shadow ability is never used.
					put("watcher", "19_21");

					// Goblin Patrol Troop: a [moria] Orc with no text.
					put("troop", "1_177");
					// Goblin Runner: a [moria] Orc.  "When you play this minion, you may add (2)", which it only
					// does if played.
					put("runner", "1_178");
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
	public void StrongTentacleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Strong Tentacle
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Creature
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Tentacle</b>.<br>This minion cannot bear possessions.<br>Discard this minion from play if not at a marsh.<br>This minion is strength +1 for each tentacle you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("strong");

		assertEquals("Strong Tentacle", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.CREATURE, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TENTACLE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsNotOfferedAsBearerOfAPossessionThatOtherMoriaMinionsMayBearButMayBearACondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var strong = scn.GetShadowCard("strong");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var gollum = scn.GetShadowCard("gollum");
		var spear = scn.GetShadowCard("spear");
		var deal = scn.GetShadowCard("deal");
		scn.MoveCardsToHand(spear, deal);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPass();
		scn.MoveMinionsToTable(strong, troop, runner, gollum);
		scn.ShadowRefreshDecision();

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.SHADOW_CHARACTERS, strong, troop, runner, gollum);

		// The Spear may go on a [moria] minion: the two Orcs are offered, and the tentacle, a [moria] minion
		// too, is not.  Gollum is no [moria] minion.
		assertTrue(scn.ShadowPlayAvailable(spear));
		scn.ShadowPlayCard(spear);
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(strong, gollum));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);
		assertAttachedTo(spear, troop);

		// A condition is no possession: the tentacle may bear it.
		assertTrue(scn.ShadowPlayAvailable(deal));
		scn.ShadowPlayCard(deal);
		assertTrue(scn.ShadowHasCardChoiceAvailable(strong, troop, runner));
		scn.ShadowChooseCard(strong);

		assertAttachedTo(deal, strong);
		assertAttachedTo(spear, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtASiteThatIsNotAMarshItIsDiscardedAsItComesIntoPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strong = scn.GetShadowCard("strong");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(strong);

		scn.StartGame();
		assertInZone(Zone.HAND, strong);
		scn.MoveMinionsToTable(troop);
		// 3 set + 1 for Frodo + 2 for Ettenmoors: enough for the tentacle's 3 and 2 for roaming.
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.HAND, strong);
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.ShadowPlayCard(strong);

		// Discarded as it comes into play.  The Orc beside it is no more at a marsh than it was, and stays.
		assertInZone(Zone.DISCARD, strong);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCopyStaysAtAMarshAndIsDiscardedWhenTheFellowshipMovesToASiteThatIsNotOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var strong = scn.GetShadowCard("strong");
		var strong2 = scn.GetShadowCard("strong2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(strong, strong2, troop);

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.SHADOW_CHARACTERS, strong, strong2, troop);

		// Nobody is assigned, so the minions are all still there when the Free Peoples player decides whether to move.
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SHADOW_CHARACTERS, strong, strong2, troop);

		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.DISCARD, strong, strong2);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StaysAtAPlainsSiteThatHasGainedMarsh() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strong = scn.GetShadowCard("strong");
		var watcher = scn.GetShadowCard("watcher");
		scn.MoveCardsToHand(strong);

		scn.StartGame();
		assertInZone(Zone.HAND, strong);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		scn.MoveMinionsToTable(watcher);
		scn.ShadowRefreshDecision();

		// Ettenmoors is plains, and a marsh while the Watcher is in play.
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.HAND, strong);

		scn.ShadowPlayCard(strong);

		assertInZone(Zone.SHADOW_CHARACTERS, strong, watcher);
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsStrengthPlusOneForEachTentacleItCanSpotItselfIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var strong = scn.GetShadowCard("strong");
		var strong2 = scn.GetShadowCard("strong2");
		var foul = scn.GetShadowCard("foul");
		var watcher = scn.GetShadowCard("watcher");
		scn.MoveCardsToHand(foul);

		scn.StartGame();
		// 3 set + 1 for Frodo + 1 for Midgewater Marshes: enough for the Foul Tentacle's 2 and 2 for roaming.
		scn.SetTwilight(3);
		scn.FreepsPass();
		scn.MoveMinionsToTable(strong);
		scn.ShadowRefreshDecision();

		// Alone: 7 printed + 1 for itself.
		assertEquals(8, scn.GetStrength(strong));

		// Another tentacle played: 7 + 2.  The other tentacle's own strength is its printed 4.
		scn.ShadowPlayCard(foul);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(foul));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SHADOW_CHARACTERS, foul);
		assertEquals(9, scn.GetStrength(strong));
		assertEquals(4, scn.GetStrength(foul));

		// The Watcher is a [moria] Creature and no tentacle: not counted.
		scn.MoveMinionsToTable(watcher);
		assertInZone(Zone.SHADOW_CHARACTERS, watcher);
		assertEquals(9, scn.GetStrength(strong));

		// A second copy: three tentacles, so each copy is 7 + 3.
		scn.MoveMinionsToTable(strong2);
		assertEquals(10, scn.GetStrength(strong));
		assertEquals(10, scn.GetStrength(strong2));
		assertEquals(4, scn.GetStrength(foul));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
