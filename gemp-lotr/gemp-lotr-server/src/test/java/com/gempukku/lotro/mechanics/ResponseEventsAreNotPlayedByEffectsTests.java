package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A card that plays "an event" from somewhere does not offer a response event.
 *
 * Comprehensive Rules 4.2, "response": "A response action is not a phase action (because there is no 'response
 * phase')", and a response event may be performed "whenever the trigger described in its game text happens": an
 * effect that plays an event is not that trigger.  Ketura, 2026-10-07, on F-0186: "Response events should not
 * show in such generic cases when asking for a phase event, yeah, and especially when playing an event from
 * discard."  An event that is a response and a phase event at once (Eye of Barad-dur 5_96, Skirmish and
 * Response) is played by such a card in its phase and in no other.
 *
 * The check made before an ability's costs are paid always refused a response event; the choice made once they
 * were paid did not, so a response event was listed beside a phase event and, chosen, stopped the game: a
 * response event has no play action for an effect to carry out (F-0186).  The two test cards here play "an
 * event" from a discard pile and from a stack with nothing else in their filters.  Race Text 92_29 (Race to
 * Mount Doom), which plays a card from an opened booster pack "ignoring all costs", stopped the game the same
 * way.
 *
 * That card plays no event of another phase either.  Ketura, 2026-10-07, on F-0193: "Given the specifics of that
 * card, we'll say we restrict it to shadow-phase events only."  The phase an event is played in is no cost, so
 * "ignoring all costs" does not ignore it: of the events in the pack only those of the Shadow phase can be
 * chosen, and the one chosen is played for nothing.
 */
public class ResponseEventsAreNotPlayedByEffectsTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Test cards (testCards.hjson), Shadow conditions in the support area:
					// "Shadow or Skirmish: Play an event from your discard pile."
					put("archive", "0_8011");
					// "Shadow or Skirmish: Play an event stacked here."
					put("lectern", "0_8012");

					put("threat", "1_197");     // Threat of the Unknown: Shadow event, cost 0: exert your [moria] Orc to draw 2 cards
					put("forces", "1_248");     // Forces of Mordor: Shadow event, cost 0: spot X [sauron] minions to add (X)
					put("evil", "1_246");       // Enduring Evil: Skirmish event, cost 0: spot X burdens to make a character skirmishing a [sauron] Orc strength -X
					put("foe", "1_247");        // Enheartened Foe: Response event, cost 0, nothing to spot: if a [sauron] Orc wins a skirmish
					put("eye", "5_96");         // Eye of Barad-dur: Skirmish and Response event, cost 0: a companion skirmishing a [sauron] Orc is strength -1 for each Ring-bound companion

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7; a skirmish ability that is never used here
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability that is never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Race to Mount Doom, with the Shadow player holding Race Text 92_29: "Shadow: Open and reveal a sealed booster
	// pack (limit once per turn). You may play one shadow card revealed, ignoring all costs."
	protected VirtualTableScenario GetBoosterScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, site 4, no text
					put("runner", "1_178");     // Goblin Runner: so that the decks are not empty; never played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_29"
		);
	}

	@Test
	public void PlayingAnEventFromTheDiscardPileOffersNoResponseEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archive = scn.GetShadowCard("archive");
		var threat = scn.GetShadowCard("threat");
		var forces = scn.GetShadowCard("forces");
		var foe = scn.GetShadowCard("foe");
		var eye = scn.GetShadowCard("eye");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(archive);
		// A response event, and one that is a response and a skirmish event, and nothing else.
		scn.MoveCardsToDiscard(foe, eye);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.FreepsPass();

		// With nothing but those two to play, the ability is not offered in the Shadow phase.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(archive));

		// Two Shadow events beside them.
		scn.MoveCardsToDiscard(threat, forces);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(archive));

		var hand = scn.GetShadowHandCount();
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.ShadowUseCardAction(archive);
		assertTrue(scn.ShadowHasCardChoiceAvailable(threat, forces));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(foe, eye));
		assertEquals(2, scn.ShadowGetCardChoiceCount());

		// Threat of the Unknown is played: the only [moria] Orc exerts and 2 cards are drawn.
		scn.ShadowChooseCardBPFromSelection(threat);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, threat, forces, foe, eye);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnEventThatIsAlsoAResponseIsPlayedFromTheDiscardPileInItsOwnPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archive = scn.GetShadowCard("archive");
		var evil = scn.GetShadowCard("evil");
		var foe = scn.GetShadowCard("foe");
		var eye = scn.GetShadowCard("eye");
		var soldier = scn.GetShadowCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(archive);
		scn.MoveCardsToDiscard(evil, foe, eye);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, soldier);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowActionAvailable(archive));

		scn.ShadowUseCardAction(archive);
		assertTrue(scn.ShadowHasCardChoiceAvailable(evil, eye));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(foe));
		assertEquals(2, scn.ShadowGetCardChoiceCount());

		// Eye of Barad-dur is played as the skirmish event it is: Aragorn is the only character skirmishing a
		// [sauron] Orc and Frodo the only Ring-bound companion.
		scn.ShadowChooseCardBPFromSelection(eye);
		// 8 - 1
		assertEquals(7, scn.GetStrength(aragorn));
		assertInZone(Zone.DISCARD, evil, foe, eye);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void PlayingAnEventFromAStackOffersNoResponseEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lectern = scn.GetShadowCard("lectern");
		var threat = scn.GetShadowCard("threat");
		var forces = scn.GetShadowCard("forces");
		var foe = scn.GetShadowCard("foe");
		var eye = scn.GetShadowCard("eye");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(lectern);
		scn.StackCardsOn(lectern, foe, eye);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(lectern));

		scn.StackCardsOn(lectern, threat, forces);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(lectern));

		var hand = scn.GetShadowHandCount();
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.ShadowUseCardAction(lectern);
		assertTrue(scn.ShadowHasCardChoiceAvailable(threat, forces));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(foe, eye));
		assertEquals(2, scn.ShadowGetCardChoiceCount());

		scn.ShadowChooseCard(threat);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, threat);
		assertInZone(Zone.STACKED, forces, foe, eye);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnEventThatIsAlsoAResponseIsPlayedFromAStackInItsOwnPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lectern = scn.GetShadowCard("lectern");
		var evil = scn.GetShadowCard("evil");
		var foe = scn.GetShadowCard("foe");
		var eye = scn.GetShadowCard("eye");
		var soldier = scn.GetShadowCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(lectern);
		scn.StackCardsOn(lectern, evil, foe, eye);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, soldier);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowActionAvailable(lectern));

		scn.ShadowUseCardAction(lectern);
		assertTrue(scn.ShadowHasCardChoiceAvailable(evil, eye));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(foe));
		assertEquals(2, scn.ShadowGetCardChoiceCount());

		scn.ShadowChooseCard(eye);
		// 8 - 1
		assertEquals(7, scn.GetStrength(aragorn));
		assertInZone(Zone.DISCARD, eye);
		assertInZone(Zone.STACKED, evil, foe);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AResponseEventRevealedFromABoosterPackIsShownAndCannotBeChosenToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBoosterScenario();

		var mod = scn.GetShadowCard("mod");

		// The test pack (product/Test-Booster.hjson) holds Enheartened Foe 1_247, a Shadow response event that
		// costs 0 and needs nothing spotted, beside Goblin Backstabber 1_174, a Shadow minion.
		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();

		// Shown with the rest of the pack, and not to be chosen; the minion beside it is.
		assertTrue(scn.ShadowHasBPChoiceAvailable("1_247", false));
		assertTrue(scn.ShadowHasBPChoiceAvailable("1_174", true));

		// Nothing is chosen.
		scn.ShadowPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnEventOfAnotherPhaseRevealedFromABoosterPackIsShownAndCannotBeChosenAndAMinionIsPlayedAsBefore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBoosterScenario();

		var mod = scn.GetShadowCard("mod");

		// The test pack holds Drums in the Deep 1_168, a Shadow event of the skirmish phase, Foul Things 2_59, a
		// Shadow event of the Shadow phase, and Goblin Backstabber 1_174, a Shadow minion that costs 1.
		scn.UseTestBoosters();
		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for the site
		assertEquals(13, scn.GetTwilight());

		scn.StartCardCapture();
		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();

		// The skirmish event is shown with the rest of the pack and is not to be chosen in the Shadow phase;
		// the Shadow event and the minion are.
		assertTrue(scn.ShadowHasBPChoiceAvailable("1_168", false));
		assertTrue(scn.ShadowHasBPChoiceAvailable("2_59", true));
		assertTrue(scn.ShadowHasBPChoiceAvailable("1_174", true));

		scn.ShadowChooseCardBPFromSelection("1_174");
		var backstabber = scn.GetLastCreatedCard();
		scn.StopCardCapture();
		assertEquals("Goblin Backstabber", backstabber.getBlueprint().getTitle());
		assertInZone(Zone.SHADOW_CHARACTERS, backstabber);
		// Neither its cost of 1 nor the 2 for roaming at site 2 was paid.
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AShadowEventRevealedFromABoosterPackIsChosenAndPlayedWithoutItsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBoosterScenario();

		var mod = scn.GetShadowCard("mod");
		// Foul Things: "Shadow: Play a [moria] minion from your discard pile."  The Troop is the one there.
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToDiscard(troop);

		scn.UseTestBoosters();
		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for the site
		assertEquals(13, scn.GetTwilight());
		assertInZone(Zone.DISCARD, troop);

		scn.StartCardCapture();
		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.ShadowHasBPChoiceAvailable("2_59", true));

		scn.ShadowChooseCardBPFromSelection("2_59");
		var foulThings = scn.GetLastCreatedCard();
		scn.StopCardCapture();
		assertEquals("Foul Things", foulThings.getBlueprint().getTitle());
		// The event was played: it did what it says and went to the discard pile.
		assertInZone(Zone.DISCARD, foulThings);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		// 13 - 6 for the Troop - 2 for its roaming at site 2: the Troop is paid for as the event's effect has
		// it, and the event's own cost of 2 is not paid.
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
