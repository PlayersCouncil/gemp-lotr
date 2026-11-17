package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_063_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("watcher1", "103_63");
					put("watcher2", "103_63");
					put("warden", "103_92");    // Cirith Ungol Watcher, Hideous Warden: the [sauron] card of the same title; it draws as this one does, and its own response adds 2 threats
					put("servants", "1_219");   // The Nine Servants of Sauron: a [ringwraith] condition, cost 1; its only text answers the play of a Nazgul
					put("oosas", "7_204");      // Out of Sight and Shot: a [ringwraith] condition: "Each time a companion is played, the Free Peoples player must exert a companion."
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a Nazgul who is [raider], not [ringwraith]
					put("lemenya", "1_232");    // Ulaire Lemenya: a [ringwraith] Nazgul; his text needs a [ringwraith] condition on a companion
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase and a skirmish
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; Maneuver: exert Aragorn to make him defender +1
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, vitality 4; Fellowship: exert Sam to remove a burden
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an ally; Fellowship: exert Elrond to draw a card
					put("friends", "2_98");     // Dear Friends: Fellowship event, cost 0: exert a companion to place a Free Peoples card from the discard pile beneath the draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same path with Midgewater Marshes as site 2, where a Hobbit exerts as the fellowship arrives.
	protected VirtualTableScenario GetMidgewaterScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("watcher1", "103_63");
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: no Hobbit; Maneuver: exert Aragorn to make him defender +1
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf to bear Dwarven Heart; no Hobbit
					put("heart", "1_10");       // Dwarven Heart: "At the start of each of your turns, exert bearer."
					put("friends", "2_98");     // Dear Friends: Fellowship event, cost 0: exert a companion to place a Free Peoples card from the discard pile beneath the draw deck
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_332");      // Midgewater Marshes: "Each time a Hobbit moves to Midgewater Marshes, that Hobbit must exert."
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

	// The same path with Silverlode Banks as site 7, where every companion exerts as the fellowship arrives.
	protected VirtualTableScenario GetSilverlodeScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("watcher1", "103_63");
					put("gimli", "1_13");       // Gimli, Son of Gloin: vitality 3, no ranger; a skirmish ability never used here
					put("poison", "8_28");      // Spider Poison: a Shadow possession a companion bears: "Each time bearer is assigned to a skirmish, bearer must exert."
					put("runner", "1_178");     // Goblin Runner: a minion to be assigned to
					put("troop1", "1_177");     // Goblin Patrol Troop: cards for the Shadow player's draw deck
					put("troop2", "1_177");
					put("troop3", "1_177");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_355");      // Silverlode Banks: "When the fellowship moves to Silverlode Banks without a ranger, every companion must exert."
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CirithUngolWatcherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Cirith Ungol Watcher, Spirit of Vigilance
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Support Area
		 * Game Text: To play, spot a [ringwraith] card.
		 * 	Each time a companion exerts, draw a card (limit once per site).
		 * 	Response: If a companion is exerted by a Free Peoples card, discard another Cirith Ungol Watcher
		 * 	to add (6) and hinder that companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("watcher1");

		assertEquals("Cirith Ungol Watcher", card.getBlueprint().getTitle());
		assertEquals("Spirit of Vigilance", card.getBlueprint().getSubtitle());
		// Two dots: no more than two copies in play
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilARingwraithCardIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher = scn.GetShadowCard("watcher1");
		var servants = scn.GetShadowCard("servants");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(watcher, servants);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		scn.SetTwilight(8);
		scn.FreepsPass();

		// 8 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(11, scn.GetTwilight());
		// A Nazgul is in play, but he is a [raider] card.
		assertFalse(scn.ShadowPlayAvailable(watcher));

		scn.ShadowPlayCard(servants);
		// 11 - 1 for the condition
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(watcher));
		assertEquals(0, scn.GetBurdens());

		scn.ShadowPlayCard(watcher);

		// 10 - 2 to play
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, watcher);
		// The card as first released added a burden when played; this revision does not.
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARingwraithMinionIsEnoughToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher = scn.GetShadowCard("watcher1");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(watcher);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(8);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(watcher));
		scn.ShadowPlayCard(watcher);

		// 8 set + 1 for Frodo + 2 for the site - 2 to play
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, watcher);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACompanionExertingDrawsACardOnceAtEachSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher = scn.GetShadowCard("watcher1");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(watcher);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetCurrentSiteNumber());
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsUseCardAction(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// A second exertion at the same site draws nothing.
		scn.ShadowPass();
		scn.FreepsUseCardAction(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());

		// The fellowship moves on (the Shadow player reconciles on the way, so the counts are read afresh).
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetCurrentSiteNumber());
		hand = scn.GetShadowHandCount();
		deck = scn.GetShadowDeckCount();

		scn.FreepsUseCardAction(aragorn);

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnExertionBeforeTheMoveDoesNotUseUpTheDrawOfTheSiteMovedTo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher = scn.GetShadowCard("watcher1");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveCardsToSupportArea(watcher);
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.FreepsRefreshDecision();

		// Site 1, the fellowship phase
		assertEquals(1, scn.GetCurrentSiteNumber());
		int hand = scn.GetShadowHandCount();
		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(hand + 1, scn.GetShadowHandCount());

		// Site 2 is another site, with a draw of its own.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsUseCardAction(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheDrawTakenAtASiteIsThereToTakeAgainOnTheNextTurnAtThatSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher = scn.GetShadowCard("watcher1");
		var runner = scn.GetShadowCard("runner");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveCardsToSupportArea(watcher);
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// Turn 1, site 2: the draw for this site.
		assertEquals(2, scn.GetCurrentSiteNumber());
		int hand = scn.GetShadowHandCount();
		scn.FreepsUseCardAction(aragorn);
		assertEquals(hand + 1, scn.GetShadowHandCount());

		// The turn ends at site 2; the other player's turn passes; the fellowship phase of the next turn finds
		// the fellowship at site 2 still.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, watcher);
		// The reconciles on the way may have emptied the draw deck.
		scn.MoveCardsToTopOfDeck(lemenya, khamul);
		scn.FreepsRefreshDecision();

		// Once for each site in a turn: on a new turn the site's draw is there again.
		hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		scn.FreepsUseCardAction(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());

		// And once only.
		scn.FreepsUseCardAction(sam);

		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASitesDrawRunsFromTheStartOfTheTurnOrTheArrivalThereUntilTheFellowshipLeaves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMidgewaterScenario();

		var frodo = scn.GetRingBearer();
		var watcher = scn.GetShadowCard("watcher1");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var heart = scn.GetFreepsCard("heart");
		var friends = scn.GetFreepsCard("friends");

		scn.MoveCardsToSupportArea(watcher);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(gimli, heart);
		scn.MoveCardsToHand(friends);

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		// The start of the turn, at site 1: Dwarven Heart exerts Gimli.
		scn.StartGame();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());

		// The minion leaves the draw deck or the hand, so the counts are read afresh.
		scn.MoveMinionsToTable(runner);
		scn.FreepsRefreshDecision();
		hand = scn.GetShadowHandCount();
		deck = scn.GetShadowDeckCount();

		// The fellowship phase is spent at the same site: its draw has been taken.
		scn.FreepsPlayCard(friends);
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());

		// The move to Midgewater Marshes exerts Frodo as he arrives: that is site 2's draw,
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());

		// and there is no second one for an exertion later at that site.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(aragorn);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnExertionOnArrivingByTheRegroupMoveIsTheDrawOfTheSiteArrivedAt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSilverlodeScenario();

		var frodo = scn.GetRingBearer();
		var watcher = scn.GetShadowCard("watcher1");
		var poison = scn.GetShadowCard("poison");
		var runner = scn.GetShadowCard("runner");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToSupportArea(watcher);

		// The first move of the turn, to site 6.  Nobody exerts there: no card is drawn at that site.
		scn.FreepsPass();
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.SkipToMovementDecision();
		// The reconcile on the way has drawn from the Shadow player's deck.
		scn.MoveCardsToTopOfDeck(troop1, troop2, troop3);
		assertEquals(0, scn.GetWoundsOn(frodo));
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		// The regroup move, to Silverlode Banks: Frodo exerts as he arrives, and that is site 7's draw,
		scn.FreepsChooseToMove();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// so there is no second one for an exertion later at that site.
		scn.AttachCardsTo(frodo, poison);
		scn.MoveMinionsToTable(runner);
		hand = scn.GetShadowHandCount();
		deck = scn.GetShadowDeckCount();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, runner);

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void TwoCompanionsExertingAtOnceDrawOneCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSilverlodeScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var watcher = scn.GetShadowCard("watcher1");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");

		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToSupportArea(watcher);
		// The turns played on the way drew from the Shadow player's deck.
		scn.MoveCardsToTopOfDeck(troop1, troop2, troop3);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		// The move to Silverlode Banks exerts Frodo and Gimli together: two exertions, and one draw for the site.
		scn.FreepsPass();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		// Nobody was asked to put the two triggers in order.
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCopyDrawsItsOwnCardOnceAtASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher1 = scn.GetShadowCard("watcher1");
		var watcher2 = scn.GetShadowCard("watcher2");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(watcher1, watcher2);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		scn.FreepsUseCardAction(aragorn);

		// One card for each copy
		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		// Each offers its response as well, with the other to discard.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(watcher1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(watcher2));
		scn.ShadowDeclineOptionalTrigger();
		scn.ShadowPass();

		// Both have had their draw for this site.
		scn.FreepsUseCardAction(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(watcher1));
	}

	@Test
	public void AnAllyExertingIsNeitherDrawnForNorAnsweredAndAResponseDeclinedChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher1 = scn.GetShadowCard("watcher1");
		var watcher2 = scn.GetShadowCard("watcher2");
		var elrond = scn.GetFreepsCard("elrond");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveCardsToSupportArea(watcher1, watcher2);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetWoundsOn(elrond));

		// An ally exerted by a Free Peoples card: his own ability.
		scn.FreepsUseCardAction(elrond);

		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A companion doing the same on the same board is seen by both copies.
		scn.FreepsUseCardAction(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(watcher1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(watcher2));
		int twilight = scn.GetTwilight();

		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, watcher1);
		assertInZone(Zone.SUPPORT, watcher2);
		assertEquals(twilight, scn.GetTwilight());
		assertFalse(scn.IsHindered(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionExertedByAShadowCardIsDrawnForAndNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var watcher1 = scn.GetShadowCard("watcher1");
		var watcher2 = scn.GetShadowCard("watcher2");
		var oosas = scn.GetShadowCard("oosas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveCardsToSupportArea(watcher1, watcher2, oosas);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(sam);

		scn.StartGame();

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		// Playing a companion sets off the Shadow condition, and it is the Free Peoples player who chooses and
		// exerts.
		scn.FreepsPlayCard(sam);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, sam));
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.IsHindered(aragorn));
		assertInZone(Zone.SUPPORT, watcher1);
		assertInZone(Zone.SUPPORT, watcher2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionExertedByASiteIsDrawnForAndNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher1 = scn.GetShadowCard("watcher1");
		var watcher2 = scn.GetShadowCard("watcher2");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(watcher1, watcher2);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		// Ettenmoors, site 2: "Skirmish: Exert your companion or minion to make that character strength +2."
		// The Free Peoples player uses it, but a site is no Free Peoples card.
		var ettenmoors = scn.GetCurrentSite();
		int hand = scn.GetShadowHandCount();
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsActionAvailable(ettenmoors));

		scn.FreepsUseCardAction(ettenmoors);
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.IsHindered(aragorn));
		assertInZone(Zone.SUPPORT, watcher1);
		assertInZone(Zone.SUPPORT, watcher2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ResponseDiscardsTheOtherWatcherAddsSixAndHindersTheCompanionThatExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var watcher1 = scn.GetShadowCard("watcher1");
		var watcher2 = scn.GetShadowCard("watcher2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var friends = scn.GetFreepsCard("friends");

		scn.MoveCardsToSupportArea(watcher1, watcher2);
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(friends);

		scn.StartGame();

		// A Free Peoples event whose cost exerts a companion of the player's choice.
		scn.FreepsPlayCard(friends);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, sam));
		scn.FreepsChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(watcher1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(watcher2));
		int twilight = scn.GetTwilight();
		assertFalse(scn.IsHindered(sam));

		scn.ShadowChooseOptionalTrigger(watcher2);

		// The copy that answers stays; the other is the one discarded.
		assertInZone(Zone.SUPPORT, watcher2);
		assertInDiscard(watcher1);
		assertEquals(twilight + 6, scn.GetTwilight());
		// The companion that exerted, and no other.
		assertTrue(scn.IsHindered(sam));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ResponseNeedsAnotherWatcherToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher1 = scn.GetShadowCard("watcher1");
		var warden = scn.GetShadowCard("warden");
		var servants = scn.GetShadowCard("servants");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");

		// With the Watcher, a [ringwraith] card in the support area that is no Watcher.
		scn.MoveCardsToSupportArea(watcher1, servants);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		int hand = scn.GetShadowHandCount();
		scn.FreepsUseCardAction(aragorn);

		// The Watcher saw the exertion, and has nothing to pay with.
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Another Cirith Ungol Watcher arrives: the [sauron] card of that title, not a copy of this one.
		scn.MoveCardsToSupportArea(warden);
		scn.ShadowPass();
		int twilight = scn.GetTwilight();
		assertFalse(scn.IsHindered(aragorn));

		// A companion exerted by a Free Peoples card: the cost of his own ability.
		scn.FreepsUseCardAction(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(watcher1));

		scn.ShadowChooseOptionalTrigger(watcher1);

		assertInDiscard(warden);
		assertInZone(Zone.SUPPORT, watcher1);
		assertInZone(Zone.SUPPORT, servants);
		assertEquals(twilight + 6, scn.GetTwilight());
		assertTrue(scn.IsHindered(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
