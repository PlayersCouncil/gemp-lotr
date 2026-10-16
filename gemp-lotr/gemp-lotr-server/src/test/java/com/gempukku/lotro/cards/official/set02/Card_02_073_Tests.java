package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_073_Tests
{

	// The usual Fellowship sites.  Site 2, Ettenmoors, is plains and not a marsh.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("watcher", "2_73");
					// Vile Tentacle: a tentacle that is not discarded away from a marsh (strength 7, -4 while not at
					// one); its Shadow ability is never used.
					put("vile", "101_36");
					// Evil-smelling Fens: a [gollum] condition, "Each site on the adventure path is a marsh"; its Shadow
					// ability (exert Gollum twice) is never used.
					put("fens", "5_22");
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
					put("watcher", "2_73");

					// Two tentacles: Vile Tentacle (strength 7 at a marsh) and Reaching Tentacle (strength 4, +1 for
					// each tentacle you can spot).
					put("vile", "101_36");
					put("reaching", "19_19");

					// Other minions, of three cultures and two races
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc with no text
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability only
					put("raiding", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai; damage +1 and nothing else
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, cost 1; "When you play this minion, you may add (2)"

					put("fens", "5_22");        // Evil-smelling Fens: a Shadow card that is no minion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a character who is no minion

					// Goblin Spear: a possession, "Bearer must be a [moria] minion."  Its other text is about killing
					// a companion.
					put("spear", "1_182");
					// Let Her Deal With Them: a condition, "To play, spot Gollum or Smeagol. Bearer must be a minion."
					// Its damage bonus and its play from the discard pile reach no reading here.
					put("deal", "7_63");
					// Smeagol, Poor Creature: a [gollum] companion to spot for the condition (a minion Gollum would be
					// discarded); his skirmish ability is never used.
					put("smeagol", "6_45");
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
		 * Set: 2
		 * Name: Watcher in the Water, Keeper of Westgate
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Creature
		 * Strength: 11
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: <b>Damage +1</b>.<br>While you can spot Watcher in the Water, discard all other minions (except tentacles).<br>Each tentacle is strength +2 and <b>damage</b> <b>+1</b>. This minion may not bear possessions and is discarded if not at a marsh.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("watcher");

		assertEquals("Watcher in the Water", card.getBlueprint().getTitle());
		assertEquals("Keeper of Westgate", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.CREATURE, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedAtAMarshHeDiscardsEveryOtherMinionButTheTentacles() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var watcher = scn.GetShadowCard("watcher");
		var vile = scn.GetShadowCard("vile");
		var reaching = scn.GetShadowCard("reaching");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		var raiding = scn.GetShadowCard("raiding");
		var fens = scn.GetShadowCard("fens");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(watcher);

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(fens);
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, fens);
		scn.FreepsPass();
		scn.MoveMinionsToTable(vile, reaching, troop, soldier, raiding);
		scn.ShadowRefreshDecision();

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.SHADOW_CHARACTERS, vile, reaching, troop, soldier, raiding);
		assertInZone(Zone.SUPPORT, fens);

		scn.ShadowPlayCard(watcher);

		assertInZone(Zone.SHADOW_CHARACTERS, watcher, vile, reaching);
		assertInDiscard(troop, soldier, raiding);
		// No minion: the companion and the Shadow condition stay.
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertInZone(Zone.SUPPORT, fens);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AGoblinRunnerPlayedWhileHeIsInPlayIsDiscardedAndAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var watcher = scn.GetShadowCard("watcher");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(watcher);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.SHADOW_CHARACTERS, watcher);
		int twilight = scn.GetTwilight();

		scn.ShadowPlayCard(runner);

		// CR 4.2 "discard": the Runner is discarded as it comes into play, and its "When you play this minion, you
		// may add (2)" is ignored.  What it cost stays paid: 1 to play + 2 roaming, and nothing added.
		assertInDiscard(runner);
		assertEquals(twilight - 3, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, watcher);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachTentacleIsStrengthPlusTwoAndDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var watcher = scn.GetShadowCard("watcher");
		var vile = scn.GetShadowCard("vile");
		var reaching = scn.GetShadowCard("reaching");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(vile, reaching);
		scn.ShadowRefreshDecision();

		assertEquals(7, scn.GetStrength(vile));
		// 4 + 1 for each of the two tentacles
		assertEquals(6, scn.GetStrength(reaching));
		assertEquals(0, scn.GetKeywordCount(vile, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(reaching, Keyword.DAMAGE));

		scn.MoveMinionsToTable(watcher);
		scn.ShadowRefreshDecision();

		assertEquals(9, scn.GetStrength(vile));
		assertEquals(8, scn.GetStrength(reaching));
		assertEquals(1, scn.GetKeywordCount(vile, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(reaching, Keyword.DAMAGE));
		// The Watcher is no tentacle: his printed strength and damage +1 only.
		assertEquals(11, scn.GetStrength(watcher));
		assertEquals(1, scn.GetKeywordCount(watcher, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MayNotBearAPossessionThatAnotherMoriaMinionMayBearButMayBearACondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var watcher = scn.GetShadowCard("watcher");
		var runner = scn.GetShadowCard("runner");
		var spear = scn.GetShadowCard("spear");
		var deal = scn.GetShadowCard("deal");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(watcher, spear, deal);

		scn.StartGame();
		scn.MoveCompanionsToTable(smeagol);
		scn.SetTwilight(15);
		assertInHand(watcher, spear, deal);
		scn.FreepsPass();
		scn.MoveMinionsToTable(runner);
		scn.ShadowRefreshDecision();

		// Without the Watcher, the Spear may go on a [moria] minion.
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.ShadowPlayAvailable(spear));

		scn.ShadowPlayCard(watcher);
		assertInDiscard(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, watcher);

		// The Watcher, a [moria] minion, is now the only minion, and may not bear it.
		assertFalse(scn.ShadowPlayAvailable(spear));

		// A condition is no possession: he may bear it (Smeagol spotted for its requirement).
		assertTrue(scn.ShadowPlayAvailable(deal));
		scn.ShadowPlayCard(deal);

		assertAttachedTo(deal, watcher);
		assertInHand(spear);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtASiteThatIsNotAMarshHeIsDiscardedAsHeComesIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher = scn.GetShadowCard("watcher");
		var vile = scn.GetShadowCard("vile");
		scn.MoveCardsToHand(watcher);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(vile);
		scn.ShadowRefreshDecision();

		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.SHADOW_CHARACTERS, vile);
		// 7 - 4 away from a marsh
		assertEquals(3, scn.GetStrength(vile));
		int twilight = scn.GetTwilight();

		scn.ShadowPlayCard(watcher);

		// What he cost stays paid: 4 to play + 2 roaming.
		assertInDiscard(watcher);
		assertEquals(twilight - 6, scn.GetTwilight());
		// The tentacle that stays away from a marsh is still there, and has nothing from him.
		assertInZone(Zone.SHADOW_CHARACTERS, vile);
		assertEquals(3, scn.GetStrength(vile));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StaysAtAMarshAndIsDiscardedWhenTheFellowshipMovesToASiteThatIsNotOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var watcher = scn.GetShadowCard("watcher");
		var vile = scn.GetShadowCard("vile");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(watcher, vile);

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.SHADOW_CHARACTERS, watcher, vile);

		// Nobody is assigned, so the minions are all still there when the Free Peoples player decides whether to move.
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SHADOW_CHARACTERS, watcher, vile);

		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInDiscard(watcher);
		assertInZone(Zone.SHADOW_CHARACTERS, vile);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StaysAtAPlainsSiteThatEvilSmellingFensMakesAMarsh() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher = scn.GetShadowCard("watcher");
		var fens = scn.GetShadowCard("fens");
		scn.MoveCardsToHand(watcher);

		scn.StartGame();
		scn.MoveCardsToSupportArea(fens);
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, fens);
		scn.FreepsPass();

		// Ettenmoors is plains, and a marsh while the Fens are in play.
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));

		scn.ShadowPlayCard(watcher);

		assertInZone(Zone.SHADOW_CHARACTERS, watcher);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
