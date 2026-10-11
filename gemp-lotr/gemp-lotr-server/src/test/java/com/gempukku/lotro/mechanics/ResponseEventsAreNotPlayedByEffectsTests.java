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
 * discard."
 *
 * The check made before an ability's costs are paid always refused a response event; the choice made once they
 * were paid did not, so a response event was listed beside a phase event and, chosen, stopped the game: a
 * response event has no play action for an effect to carry out (F-0186).  Race Text 92_29 (Race to Mount Doom),
 * which plays a card from an opened booster pack "ignoring all costs", stopped the game the same way, and is the
 * card these tests are on.
 *
 * A printed card's "play a [culture] event from your discard pile" is read in that card's own file, with a
 * response event of the culture in the pile: Now for Ruin 102_52 (Card_V2_052_Tests) beside Unyielding 10_74.
 * Its filter names no phase and excepts nothing, so the refusal read there is the game's own.  (Grimir, Dwarven
 * Elder 51_17's file lays one too, where the card's own words "skirmish event" would refuse it as well.)  This
 * class used to read the same of two invented cards, "play an event from your discard pile" and "play an event
 * stacked here" with no culture and no phase in their filters, and of them alone that an event which is a
 * response and a phase event at once (Eye of Barad-dur 5_96, Skirmish and Response) is played by such an effect
 * in its own phase.  No printed card has either shape, and none can reach that event
 * (it is a [sauron] event, and no card plays a [sauron] event or "a Shadow event" from a pile, a stack or a hand
 * in a skirmish phase), so those tests went with the cards (Ketura, 2026-10-11, Q-0241: the campaign tests
 * cards).  That reading of Filters.playable stands in the engine with no test.
 *
 * Race Text 92_29 plays no event of another phase either.  Ketura, 2026-10-07, on F-0193: "Given the specifics of
 * that card, we'll say we restrict it to shadow-phase events only."  The phase an event is played in is no cost,
 * so "ignoring all costs" does not ignore it: of the events in the pack only those of the Shadow phase can be
 * chosen, and the one chosen is played for nothing.
 */
public class ResponseEventsAreNotPlayedByEffectsTests
{
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
