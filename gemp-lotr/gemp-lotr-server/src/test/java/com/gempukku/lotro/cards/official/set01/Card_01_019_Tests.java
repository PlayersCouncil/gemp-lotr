package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;


public class Card_01_019_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("balin", "1_19");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, vitality 3; his one ability is a fellowship one
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf; "Maneuver: Exert Aragorn ..."

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, vitality 2; answers a weapon played on him
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, vitality 1
					put("runner2", "1_178");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3: a minion who is no Orc
					put("halftroll", "14_12");  // Half-troll of Far Harad: [orc] Half-troll, vitality 2: of the Orc culture
					                            // and no Orc; his one text is asked at the start of an assignment phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HereLiesBalinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Here Lies Balin, Son of Fundin
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Maneuver: Exert a Dwarf to wound 2 Orcs or to wound 1 Orc twice.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("balin");

		assertEquals("Here Lies Balin, Son of Fundin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsADwarfToWoundTwoOrcsOfThePlayersChoosingOnceEach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		var chieftain = scn.GetShadowCard("chieftain");
		var savage = scn.GetShadowCard("savage");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, soldier, chieftain, savage, halftroll);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(balin));
		assertEquals(0, scn.GetWoundsOn(grimir));

		scn.FreepsPlayCard(balin);
		// A Dwarf companion and a Dwarf ally may pay; Aragorn, who could exert, is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(grimir);
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// Three Orcs, each able to take two wounds: either half of the text can be done in full.
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("Wound 2 Orcs");

		// Orcs of the [moria] and [sauron] cultures; not the Uruk-hai, and not the Half-troll of the [orc] culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, soldier, chieftain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage, halftroll));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCards(troop, chieftain);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertInZone(Zone.DISCARD, balin);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WoundOneOrcTwiceIsAimedAtAnOrcAbleToTakeBothWoundsAndPlacesTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		var runner = scn.GetShadowCard("runner1");
		var savage = scn.GetShadowCard("savage");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, soldier, runner, savage, halftroll);
		scn.SkipToPhase(Phase.MANEUVER);

		// Gimli is the only Dwarf, so he is the one to exert.
		scn.FreepsPlayCard(balin);
		assertEquals(1, scn.GetWoundsOn(gimli));

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("Wound 1 Orc twice");

		// The half chosen has to be done in full: an Orc of 3 vitality and one of 2, and not the Runner, who
		// has 1.  Nor the Uruk-hai or the Half-troll, who could each take two wounds.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, soldier));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, savage, halftroll));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(troop);
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInZone(Zone.DISCARD, balin);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ALoneOrcOfTwoVitalityIsWoundedTwiceWithNoQuestionAndDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var soldier = scn.GetShadowCard("soldier");
		var savage = scn.GetShadowCard("savage");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, savage, halftroll);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(soldier));
		scn.FreepsPlayCard(balin);

		// With one Orc there are not two to wound, so wounding the one twice is all that can be done in full.
		// Two wounds kill an Orc of 2 vitality, which two exertions could not.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInZone(Zone.DISCARD, soldier);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertInZone(Zone.DISCARD, balin);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TwoOrcsOfOneVitalityAreEachWoundedOnceWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SHADOW_CHARACTERS, runner1, runner2);
		scn.FreepsPlayCard(balin);

		// Neither Orc can take two wounds, so wounding the two of them is all that can be done in full.  A
		// wound falls on a minion of 1 vitality, who could not exert.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInZone(Zone.DISCARD, runner1, runner2);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.DISCARD, balin);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOneOrcOfOneVitalityBothHalvesAreOfferedAndTheOneChosenDoesWhatItCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(balin);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// There are not two Orcs, and the one cannot take two wounds: neither half can be done in full, so the
		// player has both to choose from, and as much of the one chosen as can be done is done.
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertInZone(Zone.SHADOW_CHARACTERS, runner);

		scn.FreepsChooseOption("Wound 1 Orc twice");
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.DISCARD, balin);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoOrcInPlayTheDwarfExertsAndNothingIsAskedOrWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, halftroll);
		scn.SkipToPhase(Phase.MANEUVER);

		// The Orcs are what the event acts on, not something it needs: it may be played with none.
		assertTrue(scn.FreepsPlayAvailable(balin));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.FreepsPlayCard(balin);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertInZone(Zone.DISCARD, balin);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyDwarfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		// Gimli has 1 vitality left.
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveCardsToHand(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, soldier);
		scn.SkipToPhase(Phase.MANEUVER);

		// Aragorn's own maneuver ability shows he could exert; he is no Dwarf.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(aragorn));
		assertFalse(scn.FreepsPlayAvailable(balin));
		assertInZone(Zone.HAND, balin);
	}
}
