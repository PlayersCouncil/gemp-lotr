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

public class Card_04_171_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rest1", "4_171");
					put("rest2", "4_171");
					put("rest3", "4_171");
					put("rest4", "4_171");

					// Uruk Foot Soldier: an [isengard] tracker, cost 1, site 5; stronger only in a fierce skirmish.
					put("footsoldier", "4_187");
					// Uruk Seeker: an [isengard] tracker of another title; stronger only against a search card's bearer
					// in a skirmish.
					put("seeker", "4_195");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai who is no tracker; Damage +1 is its whole text
					// Orc Runner: a [sauron] tracker; lowers [sauron] Orcs' site numbers, and his play trigger is never
					// met: he is cheated in.
					put("runner", "5_109");
					// Many Riddles: another [isengard] search condition played on a companion (not the Ring-bearer); its
					// assignment ability is used only on purpose.
					put("riddles", "4_159");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					// Fierce in Despair: a [raider] Regroup event, "Spot 2 [raider] Men to discard a companion (except the
					// Ring-bearer)".
					put("fierce", "7_148");
					put("axeman1", "4_224");    // Easterling Axeman: a [raider] Man; stronger and fierce only at 2 burdens
					put("axeman2", "4_224");
					put("child", "4_148");      // Down to the Last Child: an [isengard] condition (in a draw deck only)
					put("worker", "3_62");      // Isengard Worker: an [isengard] Orc (in a draw deck only)

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] Dwarf, vitality 3; a skirmish ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a companion (in a draw deck only)
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a companion (in a draw deck only)
					put("axe", "1_9");          // Dwarven Axe: a possession (in a draw deck only)
					put("defiance", "1_37");    // Defiance: a Free Peoples event (in a draw deck only)
					put("pipe", "1_292");       // The Gaffer's Pipe: a possession (in a draw deck only)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Sméagol bearing The Dead City: a companion who is about to be killed in a skirmish and is discarded instead.
	protected VirtualTableScenario GetDeadCityScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rest", "4_171");
					// Sméagol, Poor Creature: a [gollum] companion, strength 3, vitality 4; a skirmish ability used
					// only on purpose.
					put("smeagol", "6_45");
					// The Dead City: "Bearer must be Sméagol.  If Sméagol is about to be killed in a skirmish, he is
					// discarded instead."
					put("city", "7_56");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("savage", "1_151");     // Uruk Savage (in a draw deck only)
					put("boromir", "1_97");     // Boromir, Son of Denethor (in a draw deck only)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RestWhileYouCanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Rest While You Can
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Game Text: <b>Search</b>. To play, spot an [isengard] tracker. Plays on a companion. Limit 1 per companion.<br>If bearer is killed, reveal the top 10 cards of opponent's draw deck and discard 1 Shadow card and 1 Free Peoples card. Your opponent reshuffles that deck.
		*/

		var scn = GetScenario();

		var rest = scn.GetFreepsCard("rest1");

		assertEquals("Rest While You Can", rest.getBlueprint().getTitle());
		assertNull(rest.getBlueprint().getSubtitle());
		assertFalse(rest.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, rest.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, rest.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, rest.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(rest, Keyword.SEARCH));
		assertEquals(1, rest.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnIsengardTrackerIsSpottedAndThenPlaysOnACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);

		var rest = scn.GetShadowCard("rest1");
		var footsoldier = scn.GetShadowCard("footsoldier");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.MoveMinionsToTable(savage, runner);
		scn.MoveCardsToHand(rest, footsoldier);
		scn.ShadowRefreshDecision();
		// An [isengard] minion who is no tracker, and a tracker of another culture.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(rest));

		scn.ShadowPlayCard(footsoldier);
		assertInZone(Zone.SHADOW_CHARACTERS, footsoldier);
		assertTrue(scn.ShadowPlayAvailable(rest));

		scn.ShadowPlayCard(rest);
		// Each companion, the Ring-bearer among them; not the ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(thrarin));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		assertAttachedTo(rest, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACompanionWhoBearsOneIsNotOfferedForAnotherAndWithEveryCompanionBearingOneItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var rest1 = scn.GetShadowCard("rest1");
		var rest2 = scn.GetShadowCard("rest2");
		var rest3 = scn.GetShadowCard("rest3");
		var rest4 = scn.GetShadowCard("rest4");
		var riddles = scn.GetShadowCard("riddles");
		var seeker = scn.GetShadowCard("seeker");
		scn.AttachCardsTo(aragorn, rest1);
		// Another [isengard] search condition on Gimli does not count against the limit.
		scn.AttachCardsTo(gimli, riddles);

		scn.StartGame();
		assertAttachedTo(rest1, aragorn);
		assertAttachedTo(riddles, gimli);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.MoveMinionsToTable(seeker);
		scn.MoveCardsToHand(rest2, rest3, rest4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(rest2));

		scn.ShadowPlayCard(rest2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		scn.ShadowChooseCard(gimli);
		assertAttachedTo(rest2, gimli);

		// Frodo is the one companion left without one: it goes on him unasked.
		scn.ShadowPlayCard(rest3);
		assertAttachedTo(rest3, frodo);

		// The tracker is still spotted and the pool can pay, but no companion may take a fourth.
		assertInZone(Zone.SHADOW_CHARACTERS, seeker);
		assertTrue(scn.GetTwilight() >= 1);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(rest4));
	}

	@Test
	public void WhenBearerIsKilledTheTopTenCardsOfTheOtherDeckAreRevealedAndOneOfEachSideIsDiscardedAndTheDeckIsReshuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var rest1 = scn.GetShadowCard("rest1");
		var rest2 = scn.GetShadowCard("rest2");
		var troop = scn.GetShadowCard("troop");
		scn.AttachCardsTo(aragorn, rest1);
		// A second copy, on a companion who lives: it reveals nothing.
		scn.AttachCardsTo(gimli, rest2);

		// The Free Peoples player's deck: Shadow cards of three types and Free Peoples cards of three types in the
		// top ten, an eleventh and a twelfth card of titles found nowhere above them.
		var dSavage = scn.GetFreepsCard("savage");
		var dChild = scn.GetFreepsCard("child");
		var dFierce = scn.GetFreepsCard("fierce");
		var dBoromir = scn.GetFreepsCard("boromir");
		var dAxe = scn.GetFreepsCard("axe");
		var dDefiance = scn.GetFreepsCard("defiance");
		var dRunner = scn.GetFreepsCard("runner");
		var dWorker = scn.GetFreepsCard("worker");
		var dMerry = scn.GetFreepsCard("merry");
		var dTroop = scn.GetFreepsCard("troop");
		var dFootsoldier = scn.GetFreepsCard("footsoldier");
		var dPipe = scn.GetFreepsCard("pipe");

		scn.StartGame();
		assertAttachedTo(rest1, aragorn);
		assertAttachedTo(rest2, gimli);
		scn.SetDrawDeck(VirtualTableScenario.P1, dSavage, dChild, dFierce, dBoromir, dAxe, dDefiance, dRunner, dWorker,
				dMerry, dTroop, dFootsoldier, dPipe);
		// Vitality 4: the next wound kills him.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));

		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, troop);

		assertEquals(12, scn.GetFreepsDeckCount());
		assertSame(dSavage, scn.GetFreepsTopOfDeck());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		// Aragorn loses, 8 against 13, and the wound kills him.
		scn.PassSkirmishActions();

		// The top ten are shown to both players; the eleventh and twelfth are not.
		assertEquals(10, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(10, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(dSavage, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(dTroop, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(dTroop, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(dFootsoldier, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(dPipe, false));
		scn.DismissRevealedCards();

		// The Shadow card: a minion, a condition and an event among the ten, and no Free Peoples card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(dSavage, dChild, dFierce, dRunner, dWorker, dTroop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(dBoromir, dAxe, dDefiance, dMerry, dFootsoldier, dPipe));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(dChild);

		// The Free Peoples card: a companion, a possession and an event among the ten, and no Shadow card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(dBoromir, dAxe, dDefiance, dMerry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(dSavage, dFierce, dRunner, dWorker, dTroop, dFootsoldier, dPipe));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(dAxe);

		assertInZone(Zone.DEAD, aragorn);
		assertInDiscard(rest1, dChild, dAxe);
		assertEquals(10, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, dSavage, dFierce, dBoromir, dDefiance, dRunner, dWorker, dMerry, dTroop, dFootsoldier, dPipe);
		// The deck is reshuffled once, and the Shadow player's is not.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertAttachedTo(rest2, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NeitherAnotherCompanionKilledNorBearerDiscardedRevealsAnything() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var rest1 = scn.GetShadowCard("rest1");
		var troop = scn.GetShadowCard("troop");
		var fierce = scn.GetShadowCard("fierce");
		var axeman1 = scn.GetShadowCard("axeman1");
		var axeman2 = scn.GetShadowCard("axeman2");
		scn.AttachCardsTo(aragorn, rest1);

		scn.StartGame();
		assertAttachedTo(rest1, aragorn);
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop, axeman1, axeman2);
		scn.MoveCardsToHand(fierce);
		scn.SkipToAssignments();

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		// Gimli, who does not bear it, is killed: 6 against 13.
		scn.FreepsAssignToMinions(gimli, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());

		// Bearer discarded from play, not killed.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(7);
		scn.FreepsPass();
		assertEquals(7, scn.GetTwilight());
		scn.ShadowPlayCard(fierce);

		assertInDiscard(aragorn, rest1);
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// F-0691: B (Ketura, 2026-11-03: "let it be a foot-gun"): "If bearer is killed" is built as an aboutToBeKilled
	// trigger, so a kill that The Dead City turns into a discard sets it off as well, and the Free Peoples player
	// orders the two.  Taken second, it finds its bearer gone and reveals nothing.
	@Test
	public void ABearerAboutToBeKilledAndDiscardedInsteadAsksForTheOrderAndWithTheDeadCityFirstRevealsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDeadCityScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var city = scn.GetFreepsCard("city");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, city);

		var rest = scn.GetShadowCard("rest");
		var troop = scn.GetShadowCard("troop");
		scn.AttachCardsTo(smeagol, rest);

		var dSavage = scn.GetFreepsCard("savage");
		var dBoromir = scn.GetFreepsCard("boromir");

		scn.StartGame();
		assertAttachedTo(city, smeagol);
		assertAttachedTo(rest, smeagol);
		scn.SetDrawDeck(VirtualTableScenario.P1, dSavage, dBoromir);
		// Vitality 4: the next wound would kill him.
		scn.AddWoundsToChar(smeagol, 3);
		assertEquals(3, scn.GetWoundsOn(smeagol));

		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(smeagol, troop);
		assertEquals(2, scn.GetFreepsDeckCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();

		// Sméagol loses, 3 against 13, and is about to be killed: both "is about to" triggers answer it.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveActionOrder("The Dead City");

		// Discarded, not killed, and nothing is revealed.
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertInDiscard(smeagol, rest, city);
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// F-0691: B (Ketura, 2026-11-03: "let it be a foot-gun"): taken first, this card reveals and discards for a
	// bearer who is then discarded, not killed.
	@Test
	public void ABearerAboutToBeKilledAndDiscardedInsteadWithThisCardFirstRevealsAndDiscardsAndHeIsThenDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDeadCityScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var city = scn.GetFreepsCard("city");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, city);

		var rest = scn.GetShadowCard("rest");
		var troop = scn.GetShadowCard("troop");
		scn.AttachCardsTo(smeagol, rest);

		var dSavage = scn.GetFreepsCard("savage");
		var dBoromir = scn.GetFreepsCard("boromir");

		scn.StartGame();
		assertAttachedTo(city, smeagol);
		assertAttachedTo(rest, smeagol);
		scn.SetDrawDeck(VirtualTableScenario.P1, dSavage, dBoromir);
		// Vitality 4: the next wound would kill him.
		scn.AddWoundsToChar(smeagol, 3);
		assertEquals(3, scn.GetWoundsOn(smeagol));

		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(smeagol, troop);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveActionOrder("Rest While You Can");

		// The deck's two cards are shown; each is the one card of its side, so each is discarded unasked, and the
		// deck is reshuffled.
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(2, scn.ShadowGetTotalRevealedCardCount());
		scn.DismissRevealedCards();
		assertInDiscard(dSavage, dBoromir);
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());

		// Then The Dead City: Sméagol is discarded, not killed.
		assertInDiscard(smeagol, rest, city);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
