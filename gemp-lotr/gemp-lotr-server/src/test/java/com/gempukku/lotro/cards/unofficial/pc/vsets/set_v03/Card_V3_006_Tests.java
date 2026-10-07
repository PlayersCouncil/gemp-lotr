package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_006_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haste", "103_6");
					put("haste2", "103_6");
					put("gandalf", "1_364");        // Gandalf, The Grey Wizard: vitality 4
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: the only ranger and the only [gondor] companion here
					put("sam", "1_311");            // Sam, Son of Hamfast: strength 3, vitality 4
					put("legolas", "1_50");         // Legolas, Greenleaf: an Elf archer companion, for Double Shot
					put("swiftest", "103_5");       // Shadowfax, Swiftest of All
					put("shadowfax", "4_100");      // Shadowfax (set 4): another card of that title
					put("horse", "4_283");          // Horse of Rohan: a mount that is not Shadowfax

					put("trails", "1_104");         // Eregion's Trails: [gondor] Maneuver event, cost 1: exert a ranger to make each roaming minion strength -3 until the regroup phase
					put("valor", "1_103");          // Elendil's Valor: [gondor] Maneuver event, cost 0: make a [gondor] companion defender +1 until the regroup phase
					put("valor2", "1_103");
					put("wielder", "2_28");         // Wielder of the Flame: [gandalf] Maneuver event, cost 1: spot Gandalf to make a companion defender +1; the Shadow player may remove (3) to prevent it
					put("terrible", "7_50");        // Terrible and Evil: [gandalf] Maneuver event, cost 3: exert Gandalf X times to wound a minion X times
					put("quick", "4_53");           // Quick As May Be: [dwarven] Maneuver event: spot a Dwarf companion and an Elf companion to wound a minion
					put("swordarm", "1_116");       // Swordarm of the White Tower: [gondor] Skirmish event, cost 0: make a [gondor] companion strength +2
					put("doubleshot", "1_38");      // Double Shot: [elven] Archery event: spot an Elf archer companion to make the fellowship archery total +1
					put("forfrodo", "103_107");     // For Frodo: [shire] Skirmish event, cost 2: exert a unique [gondor] or [shire] companion (except Frodo) to make him strength +1 for each burden on Frodo. Remove this from the game.
					put("hunt", "1_126");           // Hunt Them Down!: Shadow Maneuver event: make an Uruk-hai fierce
					put("pipe", "1_91");            // Aragorn's Pipe: a Free Peoples possession

					put("troop", "1_177");          // Goblin Patrol Troop: strength 13, vitality 3, site 4, no text
					put("party", "1_158");          // Uruk-hai Raiding Party: an Uruk-hai, strength 9, site 5
					put("runner", "1_178");         // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShowThemtheMeaningofHasteStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Show Them the Meaning of Haste
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Maneuver or Skirmish
		 * Game Text: <b>Maneuver</b> <i>or</i> <b>Skirmish</b>: Exert Gandalf thrice (or spot Shadowfax and exert
		 * 	Gandalf once) to reveal another Free Peoples event from your hand.  Play that event 3 times, then remove
		 * 	it from the game.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("haste");

		assertEquals("Show Them the Meaning of Haste", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsGandalfThriceToRevealAnotherFreePeoplesEventPlaysItThreeTimesAndRemovesItFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haste = scn.GetFreepsCard("haste");
		var trails = scn.GetFreepsCard("trails");
		var wielder = scn.GetFreepsCard("wielder");
		var terrible = scn.GetFreepsCard("terrible");
		var swordarm = scn.GetFreepsCard("swordarm");
		var quick = scn.GetFreepsCard("quick");
		var hunt = scn.GetFreepsCard("hunt");
		var pipe = scn.GetFreepsCard("pipe");
		var valor = scn.GetFreepsCard("valor");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		// The player's own copy of a Shadow event is in his hand, with an Uruk-hai on the table for it to name.
		scn.MoveCardsToHand(haste, trails, wielder, terrible, swordarm, quick, hunt, pipe);
		scn.MoveCardsToDiscard(valor);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, party);
		scn.SkipToPhase(Phase.MANEUVER);

		var twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(9, scn.GetStrength(party));
		assertTrue(scn.FreepsPlayAvailable(haste));

		scn.FreepsPlayCard(haste);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(twilight + 3, scn.GetTwilight());

		// Offered: a [gondor] event and a [gandalf] event that can be played now.
		assertTrue(scn.FreepsHasCardChoiceAvailable(trails, wielder));
		// Not offered: an event whose cost can no longer be paid (Gandalf is exhausted), a skirmish event, an event
		// whose requirement is not met, a Shadow event, a possession, and an event in the discard pile.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(terrible, swordarm, quick, hunt, pipe, valor));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(trails);
		// The other player is shown the event chosen and nothing else; the hand's owner is shown nothing.
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(trails, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(wielder, false));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertFalse(scn.FreepsAnyDecisionsAvailable());

		scn.ShadowDismissRevealedCards();
		// Eregion's Trails three times over: its cost three times...
		assertEquals(3, scn.GetWoundsOn(aragorn));
		// 3 for this event + 1 for each play of that one
		assertEquals(twilight + 3 + 3, scn.GetTwilight());
		// ...and each roaming minion strength -3 three times.
		assertEquals(4, scn.GetStrength(troop));
		assertEquals(0, scn.GetStrength(party));

		assertInZone(Zone.REMOVED, trails);
		assertInZone(Zone.DISCARD, haste);
		assertInZone(Zone.HAND, wielder);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void SpottingShadowfaxGandalfExertsOnceAndIsNotAskedToExertThrice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haste = scn.GetFreepsCard("haste");
		var haste2 = scn.GetFreepsCard("haste2");
		var wielder = scn.GetFreepsCard("wielder");
		var valor = scn.GetFreepsCard("valor");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, scn.GetFreepsCard("swiftest"));
		scn.MoveCardsToHand(haste, haste2, wielder, valor);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.MANEUVER);

		var twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DEFENDER));
		assertTrue(scn.FreepsPlayAvailable(haste));
		assertTrue(scn.FreepsPlayAvailable(haste2));

		scn.FreepsPlayCard(haste);
		// One exertion, and no question put to the player about it: the next thing asked is which event.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		// The second copy could itself be played (Gandalf can exert again), and is not another event to
		// reveal: a card of this title is never offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(wielder, valor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(haste2));
		scn.FreepsChooseCard(wielder);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowHasCardChoiceAvailable(wielder, false));
		scn.ShadowDismissRevealedCards();

		// Each play of Wielder of the Flame may be prevented by the Shadow player, and names its own companion.
		scn.ShadowChooseNo();
		scn.FreepsChooseCard(gandalf);
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DEFENDER));
		scn.ShadowChooseNo();
		scn.FreepsChooseCard(gandalf);
		scn.ShadowChooseNo();
		scn.FreepsChooseCard(gandalf);

		assertEquals(3, scn.GetKeywordCount(gandalf, Keyword.DEFENDER));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		// 3 for this event + 1 for each play of that one
		assertEquals(twilight + 3 + 3, scn.GetTwilight());
		assertInZone(Zone.REMOVED, wielder);
		assertInZone(Zone.DISCARD, haste);
		assertInZone(Zone.HAND, haste2, valor);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithoutShadowfaxItCannotBePlayedOnceGandalfHasFewerThanThreeExertionsLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haste = scn.GetFreepsCard("haste");
		var terrible = scn.GetFreepsCard("terrible");
		var valor = scn.GetFreepsCard("valor");
		var gandalf = scn.GetFreepsCard("gandalf");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, scn.GetFreepsCard("aragorn"));
		// Gandalf is mounted, but not on Shadowfax.
		scn.AttachCardsTo(gandalf, scn.GetFreepsCard("horse"));
		scn.MoveCardsToHand(haste, terrible, valor);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(haste));

		// Gandalf exerts once for another event, by legal play.
		scn.FreepsPlayCard(terrible);
		scn.FreepsChoose("1");
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(troop));

		scn.ShadowPass();
		// Vitality 4 with 1 wound: two exertions left, and three are needed.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(haste));
		// Another event that could be revealed is still there to be played.
		assertTrue(scn.FreepsPlayAvailable(valor));
	}

	@Test
	public void SpottingShadowfaxItCanBePlayedWithOneExertionLeftAndNotOnceGandalfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haste = scn.GetFreepsCard("haste");
		var haste2 = scn.GetFreepsCard("haste2");
		var valor = scn.GetFreepsCard("valor");
		var wielder = scn.GetFreepsCard("wielder");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, scn.GetFreepsCard("shadowfax"));
		scn.AddWoundsToChar(gandalf, 2);
		scn.MoveCardsToHand(haste, haste2, valor, wielder);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.FreepsPlayAvailable(haste));

		scn.FreepsPlayCard(haste);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsHasCardChoiceAvailable(valor, wielder));
		scn.FreepsChooseCard(valor);
		scn.ShadowDismissRevealedCards();

		// Elendil's Valor three times; Aragorn is the only [gondor] companion, so nobody is asked whom.
		assertEquals(3, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertInZone(Zone.REMOVED, valor);

		scn.ShadowPass();
		// Gandalf is exhausted: even the single exertion cannot be paid for the second copy.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(haste2));
		assertTrue(scn.FreepsPlayAvailable(wielder));
	}

	@Test
	public void MayBePlayedInTheManeuverAndSkirmishPhasesAndNotInTheArcheryOrAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haste = scn.GetFreepsCard("haste");
		var valor = scn.GetFreepsCard("valor");
		var doubleshot = scn.GetFreepsCard("doubleshot");
		var swordarm = scn.GetFreepsCard("swordarm");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		// An event for each phase, so that in each there is another event that could be revealed and played.
		scn.MoveCardsToHand(haste, valor, doubleshot, swordarm);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(valor));
		assertTrue(scn.FreepsPlayAvailable(haste));

		// Legolas, whom Double Shot needs, is put down only now: an archer earlier would have been in the way.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("legolas"));
		scn.BothPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(doubleshot));
		assertFalse(scn.FreepsPlayAvailable(haste));

		scn.BothPass();
		// The fellowship's one archery wound goes on the only minion.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(haste));

		scn.BothPass();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(swordarm));
		assertTrue(scn.FreepsPlayAvailable(haste));
	}

	@Test
	public void AnEventWhoseCostCanBePaidOnlyTwiceIsPlayedTwiceAndStillRemovedFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haste = scn.GetFreepsCard("haste");
		var terrible = scn.GetFreepsCard("terrible");
		var gandalf = scn.GetFreepsCard("gandalf");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, scn.GetFreepsCard("shadowfax"));
		scn.MoveCardsToHand(haste, terrible);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		var twilight = scn.GetTwilight();
		assertTrue(scn.FreepsPlayAvailable(terrible));
		assertTrue(scn.FreepsPlayAvailable(haste));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.FreepsPlayCard(haste);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		// Terrible and Evil is the only other event, so it is revealed without a choice.
		scn.ShadowDismissRevealedCards();

		// First play: Gandalf, with one wound on a vitality of 4, could exert once or twice; X = 1.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		// Second play: X can only be 1 now, so the game takes it without asking, and Gandalf is exhausted.
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(troop));

		// There is no third play: its cost cannot be paid. Nothing more is asked, added or wounded.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		// 3 for this event + 3 for each of the two plays of that one
		assertEquals(twilight + 3 + 6, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(troop));
		assertInZone(Zone.REMOVED, terrible);
		assertInZone(Zone.DISCARD, haste);
	}

	@Test
	public void InASkirmishAnEventThatRemovesItselfFromTheGameIsStillPlayedThreeTimes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haste = scn.GetFreepsCard("haste");
		var forfrodo = scn.GetFreepsCard("forfrodo");
		var sam = scn.GetFreepsCard("sam");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gandalf, sam);
		scn.AttachCardsTo(gandalf, scn.GetFreepsCard("shadowfax"));
		scn.MoveCardsToHand(haste, forfrodo);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		// One burden on Frodo: For Frodo is worth strength +1 each time.
		scn.AddBurdens(1);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, runner);

		var twilight = scn.GetTwilight();
		assertTrue(scn.FreepsPlayAvailable(forfrodo));
		assertTrue(scn.FreepsPlayAvailable(haste));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(3, scn.GetStrength(sam));

		scn.FreepsPlayCard(haste);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		scn.ShadowDismissRevealedCards();

		// Sam is the only unique [gondor] or [shire] companion who is not Frodo, so each play exerts him unasked.
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(6, scn.GetStrength(sam));
		// 3 for this event + 2 for each play of that one
		assertEquals(twilight + 3 + 6, scn.GetTwilight());
		assertInZone(Zone.REMOVED, forfrodo);
		assertInZone(Zone.DISCARD, haste);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoOtherFreePeoplesEventInHandThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haste = scn.GetFreepsCard("haste");
		var swordarm = scn.GetFreepsCard("swordarm");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("gandalf"), aragorn);
		// A skirmish event, and a maneuver event that needs a Dwarf and an Elf spotted.
		scn.MoveCardsToHand(haste, swordarm, scn.GetFreepsCard("quick"));

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(swordarm));
		assertFalse(scn.FreepsPlayAvailable(haste));

		// In a skirmish the skirmish event can be played, and so this one can.
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, troop);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(swordarm));
		assertTrue(scn.FreepsPlayAvailable(haste));
	}
}
