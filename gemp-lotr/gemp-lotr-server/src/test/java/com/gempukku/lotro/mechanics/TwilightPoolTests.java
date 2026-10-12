package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * The twilight pool: what playing a card adds to it or takes from it.
 *
 * Comprehensive Rules 4.2, "twilight cost": "When you play a Free Peoples card, you must add a number of twilight
 * tokens (from the reserve) to the twilight pool equal to that card's twilight cost.  When your opponent plays a
 * Shadow card, he must remove a number of twilight tokens from the twilight pool equal to that card's twilight
 * cost.  A Shadow card cannot be played if its twilight cost cannot be met by the tokens available in the twilight
 * pool.  All Shadow players pay for cards by using the same twilight pool."  "Start of turn": "When your turn
 * begins, remove all tokens from the twilight pool."  "Moving your fellowship": "Add twilight tokens equal to the
 * new site's Shadow number ...  Add 1 twilight token for each companion."
 *
 * This class is what lets a card's own tests take its printed twilight cost as read (Ketura on Q-0279,
 * 2026-10-12): a card whose text says nothing about its cost is not played in its own file to show the pool
 * moving.  So the rule is shown here on a card of every type there is on each side, the Shadow follower included
 * (Saruman, Servant of Sauron 17_38 is the only one printed).  The game pays for a card in three places (a card
 * played to the table, a card played on a bearer, an event), each of which adds for the player whose turn it is
 * and removes for the other.
 *
 * What is in another class and is not gone over again here:
 *   the roaming penalty, the minion withheld until the pool pays it, a minion past its site number, a card that
 *     says a minion is not roaming or changes a site number: RoamingTests;
 *   toil, with and without the penalty, on a minion and on a card played on a bearer: ToilTests;
 *   what is paid for a card whose cost a site, another card or its own text has changed, and that a card in play
 *     costs what is printed on it: TwilightCostInPlayTests, and for Free Peoples possessions TransferCostTests,
 *     which also has what a transfer costs.  Here are only whether a Shadow card with a changed cost is offered,
 *     and a discount larger than the cost;
 *   a play from the discard pile where nothing roams, and the event that makes it paid for with it:
 *     EventThatPlaysFromADiscardPileNeedsBothCostsTests;
 *   the starting fellowships, for which no token is placed: StartingFellowshipTests;
 *   a cost that goes by the phase an event is played in: EventPrintedWithSeveralPhasesTests;
 *   a play "ignoring all costs": IgnoringAllCostsTests;
 *   a follower's Aid cost paid: AidTests (the Shadow follower's printed Aid is read by
 *     virtualtable/AidCostReadingTests; its payment, a Shadow Aid removing (1), is no part of playing him);
 *   when in a move the tokens are added, and the 3 or 6 of a region where sites print no number:
 *     StepsOfAMoveTests, PlayingSitesTests.
 *
 * The table is the Fellowship block's: Bag End (site 1, Shadow number 0), Ettenmoors (site 2, Shadow number 2,
 * plains), Rivendell Valley (site 3, Shadow number 0).  Frodo, Son of Drogo is the Ring-bearer.  Every minion
 * here but Bill Ferny has a site number above 2, so at Ettenmoors it is roaming.
 */
public class TwilightPoolTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Free Peoples cards, one of each type.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, cost 4; a maneuver ability
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: ally, cost 2, home 6; a fellowship ability that exerts him
					put("sword", "1_299");      // Hobbit Sword: possession, cost 1.  "Bearer must be a Hobbit." is its whole text
					put("horn", "5_8");         // Horn of Helm: possession for the support area, cost 2; a maneuver ability
					put("mithril", "2_105");    // Mithril-coat: artifact, cost 2.  "Bearer must be Frodo."; the rest is about archery and skirmishes
					put("lord", "1_21");        // Lord of Moria: condition for the support area, cost 2.  "Each Dwarf is damage +1."
					put("tale", "1_317");       // There and Back Again: condition, cost 1.  "Bearer must be a Hobbit companion."; a maneuver ability
					put("pallando", "13_37");   // Pallando, Far-travelling One: follower, cost 2.  His texts answer a transfer and a skirmish won
					// Alliance Reforged: event, cost 3.  "Fellowship: Draw a card for each Elf companion you spot."
					put("alliance", "4_58");
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf companion; an archery ability
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion; a skirmish ability
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: companion, cost 4; his text is about his own strength
					put("map", "1_318");        // Thror's Map: possession for the support area, cost 0; its ability needs 2 Hobbits

					// Gandalf's Wisdom: "The twilight cost of each possession or artifact played on Gandalf or a
					// character who has the Gandalf signet is -1."  Glamdring (cost 2) and Gandalf's Pipe (cost 0)
					// both say "Bearer must be Gandalf"; their abilities are never used.
					put("wisdom", "2_23");
					put("glamdring", "1_75");
					put("pipe", "1_74");
					// Song of Durin: a [dwarven] condition.  "The twilight cost of each [moria] weapon is +2."
					put("song", "3_5");

					// A Wizard Is Never Late: event, cost 1.  "Fellowship: Play a [gandalf] character from your
					// draw deck."  Gandalf is the only [gandalf] character here (Pallando is a follower).
					put("late", "1_87");
					// Well Met Indeed: event, cost 5.  "Fellowship: Spot an Elf, a [gondor] Man, and a Dwarf to play
					// Gandalf from your dead pile."
					put("wellmet", "4_106");

					// Shadow cards, one of each type.
					// Bill Ferny, Swarthy Sneering Fellow: minion, cost 2, site number 2.  "Nazgul are not roaming"
					// (there is none here), nobody may be assigned to him, and he is discarded underground.
					put("bill", "2_75");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, site number 4, no text
					put("spear", "1_182");      // Goblin Spear: possession, cost 1.  "Bearer must be a [moria] minion."; the rest answers a kill
					// Ships of Great Draught: possession for the support area, cost 2.  Its Shadow ability needs a
					// threat or a [raider] card in hand, and there is neither.
					put("ships", "8_65");
					// Library of Orthanc: artifact for the support area, cost 2.  Its Shadow ability is a play of
					// an [isengard] minion from hand, and none is ever in hand with it.
					put("library", "9_39");
					// They Are Coming: condition for the support area, cost 3.  Its Shadow ability needs 3 cards
					// to discard from hand.
					put("coming", "1_196");
					put("weary", "4_212");      // Weary: condition, cost 1.  "Plays on a companion or ally."; the rest answers an [isengard] tracker
					// Saruman's Power: event, cost 2.  "Shadow: Exert an [isengard] minion to discard all conditions."
					put("power", "1_136");
					put("savage", "1_151");     // Uruk Savage: [isengard] minion, cost 2, site number 5; "Damage +1." is its whole text
					// Saruman, Servant of Sauron: follower, cost 1.  "Aid - (1)"; his other text answers a skirmish won.
					put("saruman", "17_38");
					put("armory", "1_173");     // Goblin Armory: condition for the support area, cost 0; it answers a [moria] weapon played

					// Saruman's Ambition: "The twilight cost of your [isengard] events is -1."  Not unique.
					put("ambition1", "1_133");
					put("ambition2", "1_133");
					put("ambition3", "1_133");

					put("host", "1_187");       // Host of Thousands: event, cost 0.  "Shadow: Play a [moria] Orc from your discard pile."
					// Goblin Swarms: "Shadow: Play an Orc stacked here as if played from hand."
					put("swarms", "1_183");
					// Orc Chieftain: [sauron] Orc, cost 2, site number 6.  His text answers a weapon played on him.
					put("chieftain", "1_266");

					// Southron Bow: possession, cost 3.  "Bearer must be a [raider] Man."  Desert Warrior is one
					// (cost 2, site number 4); his text is about his own skirmish.
					put("bow", "4_247");
					put("warrior", "4_222");
					put("fighter", "1_146");    // Uruk Fighter: minion, cost 3, site number 5; a maneuver ability that needs 5 companions
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// ---- A Free Peoples card of each type adds its twilight cost ----

	@Test
	public void ACompanionAddsItsTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();
		scn.SetTwilight(3);

		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(3 + 4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnAllyAddsItsTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCardsToHand(celeborn);

		scn.StartGame();
		scn.SetTwilight(3);

		scn.FreepsPlayCard(celeborn);
		assertInZone(Zone.SUPPORT, celeborn);
		assertEquals(3 + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void APossessionPlayedOnABearerAddsItsTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.SetTwilight(3);

		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, frodo);
		assertEquals(3 + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void APossessionPlayedToTheSupportAreaAddsItsTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		scn.MoveCardsToHand(horn);

		scn.StartGame();
		scn.SetTwilight(3);

		scn.FreepsPlayCard(horn);
		assertInZone(Zone.SUPPORT, horn);
		assertEquals(3 + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnArtifactAddsItsTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var mithril = scn.GetFreepsCard("mithril");
		scn.MoveCardsToHand(mithril);

		scn.StartGame();
		scn.SetTwilight(3);

		scn.FreepsPlayCard(mithril);
		assertAttachedTo(mithril, frodo);
		assertEquals(3 + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AConditionPlayedToTheSupportAreaAddsItsTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lord = scn.GetFreepsCard("lord");
		scn.MoveCardsToHand(lord);

		scn.StartGame();
		scn.SetTwilight(3);

		scn.FreepsPlayCard(lord);
		assertInZone(Zone.SUPPORT, lord);
		assertEquals(3 + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AConditionPlayedOnABearerAddsItsTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var tale = scn.GetFreepsCard("tale");
		scn.MoveCardsToHand(tale);

		scn.StartGame();
		scn.SetTwilight(3);

		scn.FreepsPlayCard(tale);
		assertAttachedTo(tale, frodo);
		assertEquals(3 + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AFollowerAddsItsTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pallando = scn.GetFreepsCard("pallando");
		scn.MoveCardsToHand(pallando);

		scn.StartGame();
		scn.SetTwilight(3);

		scn.FreepsPlayCard(pallando);
		assertInZone(Zone.SUPPORT, pallando);
		assertEquals(3 + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnEventAddsItsTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var alliance = scn.GetFreepsCard("alliance");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(alliance);

		scn.StartGame();
		scn.SetTwilight(3);

		scn.FreepsPlayCard(alliance);
		// One Elf companion: one card drawn.
		assertEquals(1, scn.GetFreepsHandCount());
		assertInDiscard(alliance);
		assertEquals(3 + 3, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// ---- A Shadow card of each type removes its twilight cost ----

	@Test
	public void AMinionAtItsOwnSiteNumberRemovesItsTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetShadowCard("bill");
		scn.MoveCardsToHand(bill);

		scn.StartGame();
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMinionSiteNumber(bill));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(bill);
		assertInZone(Zone.SHADOW_CHARACTERS, bill);
		assertEquals(5 - 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void APossessionPlayedOnAMinionRemovesItsTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(spear);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.FreepsPass();

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(spear);
		assertAttachedTo(spear, troop);
		assertEquals(5 - 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void APossessionPlayedToTheSupportAreaRemovesItsTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(ships);

		scn.StartGame();
		scn.FreepsPass();

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(ships);
		assertInZone(Zone.SUPPORT, ships);
		assertEquals(5 - 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnArtifactRemovesItsTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var library = scn.GetShadowCard("library");
		scn.MoveCardsToHand(library);

		scn.StartGame();
		scn.FreepsPass();

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(library);
		assertInZone(Zone.SUPPORT, library);
		assertEquals(5 - 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AConditionPlayedToTheSupportAreaRemovesItsTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coming = scn.GetShadowCard("coming");
		scn.MoveCardsToHand(coming);

		scn.StartGame();
		scn.FreepsPass();

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(coming);
		assertInZone(Zone.SUPPORT, coming);
		assertEquals(5 - 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AConditionPlayedOnACompanionRemovesItsTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var weary = scn.GetShadowCard("weary");
		scn.MoveCardsToHand(weary);

		scn.StartGame();
		scn.FreepsPass();

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(weary);
		assertAttachedTo(weary, frodo);
		assertEquals(5 - 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnEventRemovesItsTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var power = scn.GetShadowCard("power");
		scn.MoveCardsToHand(power);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(power);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(power);
		assertEquals(5 - 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AShadowFollowerRemovesItsTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(saruman);

		scn.StartGame();
		scn.FreepsPass();

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(saruman);
		assertInZone(Zone.SUPPORT, saruman);
		assertEquals(5 - 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// ---- The edges of the plain rule ----

	@Test
	public void AFreePeoplesCardThatCostsNothingAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var map = scn.GetFreepsCard("map");
		scn.MoveCardsToHand(map);

		scn.StartGame();
		scn.SetTwilight(3);

		scn.FreepsPlayCard(map);
		assertInZone(Zone.SUPPORT, map);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShadowCardThatCostsNothingIsPlayedFromAnEmptyPool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToHand(armory);

		scn.StartGame();
		scn.FreepsPass();

		scn.SetTwilight(0);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(armory));
		scn.ShadowPlayCard(armory);
		assertInZone(Zone.SUPPORT, armory);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AShadowCardIsWithheldOneTokenShortOfItsCostAndOfferedAtExactlyItsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		// One card for each place the game pays: on a bearer (1), an event (2), to the table (3).
		var spear = scn.GetShadowCard("spear");
		var power = scn.GetShadowCard("power");
		var coming = scn.GetShadowCard("coming");
		scn.MoveCardsToHand(spear, power, coming);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.FreepsPass();

		scn.SetTwilight(0);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(spear));
		assertFalse(scn.ShadowPlayAvailable(power));
		assertFalse(scn.ShadowPlayAvailable(coming));

		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(spear));
		assertFalse(scn.ShadowPlayAvailable(power));
		assertFalse(scn.ShadowPlayAvailable(coming));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(spear));
		assertTrue(scn.ShadowPlayAvailable(power));
		assertFalse(scn.ShadowPlayAvailable(coming));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(spear));
		assertTrue(scn.ShadowPlayAvailable(power));
		assertTrue(scn.ShadowPlayAvailable(coming));
		assertInHand(spear, power, coming);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwoShadowCardsArePaidForFromOnePoolAndTheSecondFromWhatTheFirstLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var library = scn.GetShadowCard("library");
		var ships = scn.GetShadowCard("ships");
		var coming = scn.GetShadowCard("coming");
		scn.MoveCardsToHand(library, ships, coming);

		scn.StartGame();
		scn.FreepsPass();

		// 4 tokens: enough for any one of the three (2, 2 and 3), and for the two that cost 2 together.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(library));
		assertTrue(scn.ShadowPlayAvailable(ships));
		assertTrue(scn.ShadowPlayAvailable(coming));

		scn.ShadowPlayCard(library);
		assertEquals(4 - 2, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(ships));
		assertFalse(scn.ShadowPlayAvailable(coming));

		scn.ShadowPlayCard(ships);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, library, ships);
		assertFalse(scn.ShadowPlayAvailable(coming));
		assertInHand(coming);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// ---- A cost that another card has changed ----

	@Test
	public void AShadowCardWhoseCostAnotherCardRaisesIsWithheldUntilThePoolPaysTheRaisedCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToSupportArea(song);
		scn.MoveCardsToHand(spear);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.FreepsPass();

		// The Spear is a [moria] weapon: its 1 is 3 under Song of Durin.  2 tokens would pay what is printed.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(spear));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(spear));
		scn.ShadowPlayCard(spear);
		assertAttachedTo(spear, troop);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AShadowCardWhoseCostAnotherCardLowersIsOfferedFromAPoolThatCouldNotPayWhatIsPrintedOnIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ambition = scn.GetShadowCard("ambition1");
		var savage = scn.GetShadowCard("savage");
		var power = scn.GetShadowCard("power");
		scn.MoveCardsToSupportArea(ambition);
		scn.MoveCardsToHand(power);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();

		// Saruman's Power is an [isengard] event: its 2 is 1 under Saruman's Ambition.
		scn.SetTwilight(0);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(power));

		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(power));
		scn.ShadowPlayCard(power);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(power);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ADiscountLargerThanAFreePeoplesCardsCostAddsNothingAndTakesNothingAway() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var wisdom = scn.GetFreepsCard("wisdom");
		var glamdring = scn.GetFreepsCard("glamdring");
		var pipe = scn.GetFreepsCard("pipe");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(wisdom);
		scn.MoveCardsToHand(glamdring, pipe);

		scn.StartGame();
		scn.SetTwilight(3);

		// The discount is in force: Glamdring's 2 is 1 on Gandalf.
		scn.FreepsPlayCard(glamdring);
		assertAttachedTo(glamdring, gandalf);
		assertEquals(3 + 1, scn.GetTwilight());

		// The Pipe costs 0: 1 less than that is still nothing, and no token is taken back out.
		scn.FreepsPlayCard(pipe);
		assertAttachedTo(pipe, gandalf);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ADiscountLargerThanAShadowCardsCostRemovesNothingAndAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var power = scn.GetShadowCard("power");
		scn.MoveCardsToSupportArea(scn.GetShadowCard("ambition1"), scn.GetShadowCard("ambition2"),
				scn.GetShadowCard("ambition3"));
		scn.MoveCardsToHand(power);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();

		// Saruman's Power costs 2, and three copies of Saruman's Ambition take 3 off it: it is played from an
		// empty pool ...
		scn.SetTwilight(0);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(power));

		// ... and from a pool of 3 it removes nothing and gives nothing back.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(power);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(power);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// ---- A card played from somewhere other than the hand ----

	@Test
	public void AMinionPlayedFromTheDiscardPileRemovesItsCostAndTheRoamingPenalty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(host);
		scn.MoveCardsToDiscard(troop);

		scn.StartGame();
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());

		// Host of Thousands costs nothing.  The Troop is 6 to play + 2 roaming, site number 4 at site 2.
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(host));

		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(host));
		scn.ShadowPlayCard(host);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionPlayedFromAStackRemovesItsCostAndTheRoamingPenalty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swarms = scn.GetShadowCard("swarms");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToSupportArea(swarms);
		scn.StackCardsOn(swarms, chieftain);

		scn.StartGame();
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());

		// The Chieftain is 2 to play + 2 roaming, site number 6 at site 2.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(swarms));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(swarms));
		scn.ShadowUseCardAction(swarms);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACompanionPlayedFromTheDrawDeckAddsItsCostBesideTheCostOfTheEventThatPlaysIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var late = scn.GetFreepsCard("late");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(late);

		scn.StartGame();
		scn.SetTwilight(3);

		scn.FreepsPlayCard(late);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		// 1 for A Wizard Is Never Late + 4 for Gandalf
		assertEquals(3 + 1 + 4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionPlayedFromTheDeadPileAddsItsCostBesideTheCostOfTheEventThatPlaysIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wellmet = scn.GetFreepsCard("wellmet");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("legolas"), scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"));
		scn.MoveCardsToDeadPile(gandalf);
		scn.MoveCardsToHand(wellmet);

		scn.StartGame();
		scn.SetTwilight(3);

		scn.FreepsPlayCard(wellmet);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		// 5 for Well Met Indeed + 4 for Gandalf
		assertEquals(3 + 5 + 4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// ---- Roaming is a minion's alone ----

	@Test
	public void OnlyAMinionPaysTheRoamingPenaltyAndACardPlayedOnARoamingMinionPaysNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		// Three cards that cost 3: a minion, a possession for a minion, a condition.
		var fighter = scn.GetShadowCard("fighter");
		var bow = scn.GetShadowCard("bow");
		var coming = scn.GetShadowCard("coming");
		scn.MoveCardsToHand(fighter, bow, coming);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(warrior, Keyword.ROAMING));

		// At site 2 the Fighter, site number 5, is 3 to play + 2 roaming.  The Bow, though its bearer is
		// roaming, and the condition are 3 and no more.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(fighter));
		assertTrue(scn.ShadowPlayAvailable(bow));
		assertTrue(scn.ShadowPlayAvailable(coming));

		scn.ShadowPlayCard(bow);
		assertAttachedTo(bow, warrior);
		assertEquals(0, scn.GetTwilight());

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(coming);
		assertInZone(Zone.SUPPORT, coming);
		assertEquals(0, scn.GetTwilight());

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(fighter));
		scn.ShadowPlayCard(fighter);
		assertInZone(Zone.SHADOW_CHARACTERS, fighter);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// ---- The pool itself ----

	@Test
	public void AMoveAddsTheShadowNumberOfTheSiteMovedToAndOneForEachCompanionAndNothingForAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"));
		scn.MoveAlliesToTable(scn.GetFreepsCard("celeborn"));

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 2 for Ettenmoors + 3 companions (Frodo, Aragorn, Gimli); Celeborn is no companion
		assertEquals(2 + 3, scn.GetTwilight());

		// Nothing is played, and the fellowship moves again.
		scn.SkipToMovementDecision();
		assertEquals(5, scn.GetTwilight());
		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		// Rivendell Valley's Shadow number is 0: the 3 companions' tokens and no more, on top of what was there
		assertEquals(5 + 0 + 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ThePoolIsEmptiedAsTheNextTurnBegins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SetTwilight(4);
		scn.FreepsPass();
		// 4 set + 2 for Ettenmoors + 1 companion
		assertEquals(7, scn.GetTwilight());

		// Nothing is spent, and the fellowship stays: the tokens are all still there as the turn ends.
		scn.SkipToMovementDecision();
		assertEquals(7, scn.GetTwilight());
		scn.FreepsChooseToStay();

		// The other player's turn has begun: his fellowship phase, with an empty pool.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(0, scn.GetTwilight());
	}
}
