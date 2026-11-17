package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_107_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("windows", "4_107");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetEntsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("windows", "4_107");
					// Treebeard, Earthborn: Ent ally, vitality 4; a response and a fellowship ability about
					// unbound Hobbits stacked on him
					put("treebeard", "4_103");
					put("avenger", "6_27");     // Ent Avenger: Ent companion, vitality 4; stronger only beside 3 Ents
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] companion who is no Ent
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetResponseScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("windows", "4_107");
					put("windows2", "4_107");
					put("treebeard", "4_103");  // Treebeard, Earthborn: Ent ally; his texts are about unbound Hobbits
					put("avenger", "6_27");     // Ent Avenger: Ent companion; stronger only beside 3 Ents
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man and a ranger
					// Defend It and Hope: Maneuver, exert a [gondor] Man to wound a minion once (twice if an Uruk-hai)
					put("defend", "4_115");
					put("whatarethey", "1_119"); // What Are They?: Maneuver, spot a ranger to discard a roaming minion
					put("messenger", "1_149");  // Uruk Messenger: [isengard] Uruk-hai, vitality 1, site 5
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, vitality 2; a Shadow ability only
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, vitality 2; Damage +1 only

					// Cards for the Shadow player's draw deck.
					put("troop", "1_177");      // Goblin Patrol Troop: Shadow minion, no text
					put("scimitar", "1_180");   // Goblin Scimitar: Shadow possession
					put("ambition", "1_133");   // Saruman's Ambition: Shadow condition
					put("goblinman", "2_42");   // Goblin Man: Shadow minion, laid below the top 10
					put("sam", "1_311");        // Sam, Son of Hamfast: Free Peoples companion
					put("bow", "4_131");        // Ranger's Bow: Free Peoples possession
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: Free Peoples companion
					put("boromir", "1_97");     // Boromir, Son of Denethor: Free Peoples companion, laid below the top 10
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WindowsinaStoneWallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Windows in a Stone Wall
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: To play, exert an Ent. Plays on that Ent.<br><b>Response:</b> If an [isengard] minion is killed, discard this condition to reveal the top 10 cards of an opponent's draw deck. Discard 1 Shadow card and 1 Free Peoples card revealed. Your opponent reshuffles that deck.
		*/

		var scn = GetScenario();

		var windows = scn.GetFreepsCard("windows");

		assertEquals("Windows in a Stone Wall", windows.getBlueprint().getTitle());
		assertNull(windows.getBlueprint().getSubtitle());
		assertFalse(windows.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, windows.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, windows.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, windows.getBlueprint().getCardType());
		assertEquals(1, windows.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedOnTheEntExertedToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEntsScenario();

		var windows = scn.GetFreepsCard("windows");
		var treebeard = scn.GetFreepsCard("treebeard");
		var avenger = scn.GetFreepsCard("avenger");
		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetRingBearer();
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCompanionsToTable(avenger, gandalf);
		scn.MoveCardsToHand(windows);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(treebeard));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.FreepsPlayAvailable(windows));

		scn.FreepsPlayCard(windows);
		// The two Ents, an ally and a companion, and neither companion who is no Ent.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(treebeard, avenger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, frodo));

		scn.FreepsChooseCard(treebeard);
		// The Ent who exerted is the one who bears it; nothing more is asked.
		assertEquals(1, scn.GetWoundsOn(treebeard));
		assertAttachedTo(windows, treebeard);
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertNotAttachedTo(windows, avenger);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheOnlyEntIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEntsScenario();

		var windows = scn.GetFreepsCard("windows");
		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveAlliesToTable(treebeard);
		// Gandalf and Frodo could exert: it is an Ent that is wanting, not anybody.
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(windows);

		scn.StartGame();
		scn.AddWoundsToChar(treebeard, 3);
		scn.FreepsRefreshDecision();

		// Vitality 4 with 3 wounds: exhausted.
		assertEquals(1, scn.GetVitality(treebeard));
		assertInZone(Zone.HAND, windows);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(windows));

		// One wound fewer and he can pay.
		scn.RemoveWoundsFromChar(treebeard, 1);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetVitality(treebeard));
		assertTrue(scn.FreepsPlayAvailable(windows));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void KillingAnIsengardUrukHaiLetsItDiscardItselfToRevealTheTopTenAndDiscardOneCardOfEachSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var windows = scn.GetFreepsCard("windows");
		var windows2 = scn.GetFreepsCard("windows2");
		var avenger = scn.GetFreepsCard("avenger");
		var treebeard = scn.GetFreepsCard("treebeard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		var messenger = scn.GetShadowCard("messenger");
		scn.MoveCompanionsToTable(avenger, aragorn);
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCardsToHand(defend);

		var troop = scn.GetShadowCard("troop");
		var sam = scn.GetShadowCard("sam");
		var scimitar = scn.GetShadowCard("scimitar");
		var bow = scn.GetShadowCard("bow");
		var ambition = scn.GetShadowCard("ambition");
		var hisDefend = scn.GetShadowCard("defend");
		var forger = scn.GetShadowCard("forger");
		var gandalf = scn.GetShadowCard("gandalf");
		var sentry = scn.GetShadowCard("sentry");
		var hisWhat = scn.GetShadowCard("whatarethey");
		var goblinman = scn.GetShadowCard("goblinman");
		var boromir = scn.GetShadowCard("boromir");

		scn.StartGame();
		scn.AttachCardsTo(avenger, windows);
		scn.AttachCardsTo(treebeard, windows2);
		// The top ten hold Shadow and Free Peoples cards of each kind; the eleventh and twelfth are one of each side.
		scn.SetShadowDrawDeck(troop, sam, scimitar, bow, ambition, hisDefend, forger, gandalf, sentry, hisWhat,
				goblinman, boromir);
		scn.MoveMinionsToTable(messenger);
		assertAttachedTo(windows, avenger);
		assertAttachedTo(windows2, treebeard);
		assertEquals(12, scn.GetShadowDeckCount());

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		// The Uruk-hai takes two wounds at vitality 1: killed.
		scn.FreepsPlayCard(defend);
		assertInDiscard(messenger);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(windows));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(windows2));

		scn.FreepsChooseOptionalTrigger(windows);
		// The condition used pays for it; the other copy stays on its Ent.
		assertInDiscard(windows);
		assertAttachedTo(windows2, treebeard);

		// The top ten are shown to both players, the eleventh is not.
		assertEquals(10, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(10, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(hisWhat, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(goblinman, false));
		scn.DismissRevealedCards();

		// He discards exactly one Shadow card among the ten, of any kind.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, scimitar, ambition, forger, sentry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, bow, hisDefend, goblinman));
		scn.FreepsChooseCards(scimitar);

		// Then exactly one Free Peoples card among the ten, of any kind.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, bow, hisDefend, gandalf, hisWhat));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop, ambition, boromir));
		scn.FreepsChooseCards(bow);

		assertInDiscard(scimitar, bow);
		assertEquals(10, scn.GetShadowDeckCount());
		assertInZone(Zone.DECK, troop, sam, ambition, hisDefend, forger, gandalf, sentry, hisWhat, goblinman, boromir);
		// The Shadow player's deck is reshuffled, and the Free Peoples player's is not.
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		// The second copy may answer the same kill as well (CR 4.2 "response"), and is declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(windows2));
		scn.FreepsDeclineOptionalTrigger();
		assertAttachedTo(windows2, treebeard);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void KillingAnIsengardOrcOffersItToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var windows = scn.GetFreepsCard("windows");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCompanionsToTable(avenger, aragorn);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.AttachCardsTo(avenger, windows);
		scn.MoveMinionsToTable(forger);
		scn.AddWoundsToChar(forger, 1);
		assertAttachedTo(windows, avenger);
		assertEquals(1, scn.GetWoundsOn(forger));

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// One wound on an Orc of vitality 2 with a wound already: killed.
		scn.FreepsPlayCard(defend);
		assertInDiscard(forger);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(windows));

		scn.FreepsDeclineOptionalTrigger();
		assertAttachedTo(windows, avenger);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedForAnIsengardMinionDiscardedNorForAMinionOfAnotherCultureKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var windows = scn.GetFreepsCard("windows");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		var whatarethey = scn.GetFreepsCard("whatarethey");
		var messenger = scn.GetShadowCard("messenger");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCompanionsToTable(avenger, aragorn);
		scn.MoveCardsToHand(defend, whatarethey);

		scn.StartGame();
		scn.AttachCardsTo(avenger, windows);
		scn.MoveMinionsToTable(messenger, sentry);
		assertAttachedTo(windows, avenger);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Both roaming at site 2; the [isengard] Uruk-hai is discarded, not killed.
		scn.FreepsPlayCard(whatarethey);
		scn.FreepsChooseCard(messenger);
		assertInDiscard(messenger);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(windows));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();
		// The [sauron] Uruk-hai takes two wounds at vitality 2: killed.
		scn.FreepsPlayCard(defend);
		assertInDiscard(sentry);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(windows));
		assertAttachedTo(windows, avenger);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoShadowCardAmongAShortDeckOnlyAFreePeoplesCardIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var windows = scn.GetFreepsCard("windows");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		var messenger = scn.GetShadowCard("messenger");
		var sam = scn.GetShadowCard("sam");
		var bow = scn.GetShadowCard("bow");
		var gandalf = scn.GetShadowCard("gandalf");
		scn.MoveCompanionsToTable(avenger, aragorn);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.AttachCardsTo(avenger, windows);
		scn.SetShadowDrawDeck(sam, bow, gandalf);
		scn.MoveMinionsToTable(messenger);
		assertAttachedTo(windows, avenger);
		assertEquals(3, scn.GetShadowDeckCount());

		scn.SkipToPhase(Phase.MANEUVER);
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsPlayCard(defend);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(windows));
		scn.FreepsAcceptOptionalTrigger();
		assertInDiscard(windows);

		// The three there are are shown; no Shadow card among them, so only the Free Peoples one is asked.
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		scn.DismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, bow, gandalf));
		scn.FreepsChooseCards(sam);

		assertInDiscard(sam);
		assertInZone(Zone.DECK, bow, gandalf);
		assertEquals(2, scn.GetShadowDeckCount());
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
