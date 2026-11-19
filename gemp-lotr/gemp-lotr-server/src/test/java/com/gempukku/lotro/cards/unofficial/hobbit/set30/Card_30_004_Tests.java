package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_004_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("battle", "30_4");

					put("nori", "30_15");       // Nori, Of the Blue Mountains: Dwarf companion, vitality 3; his one ability
					                            // needs a [dwarven] follower to discard
					put("dain", "32_2");        // Dain Ironfoot, Lord of the Iron Hills: Dwarf ally, vitality 3

					put("pursuer", "32_42");    // Orkish Pursuer: [sauron] Orc, vitality 3; a win trigger and a regroup ability
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, vitality 2; an assignment ability
					put("goblin", "32_27");     // Assaulting Goblin: [moria] Orc, vitality 2; its response is at a battleground
					                            // only, and the Hobbit path's only battleground is site 9
					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, vitality 2; texts for site 9 and the regroup phase
					put("runner1", "31_30");    // Goblin Runner: [moria] Orc, vitality 1; its text is about being played
					put("runner2", "31_30");
					put("bats", "32_30");       // Dark Bats: a [moria] Creature of vitality 2, of an Orc culture and no Orc;
					                            // a Shadow ability only
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BattleofAzanulbizarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Battle of Azanulbizar
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert a Dwarf character to wound 2 Orcs or to wound 1 Orc twice.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("battle");

		assertEquals("Battle of Azanulbizar", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsADwarfCharacterToWoundTwoOrcsOfThePlayersChoosingOnceEach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battle = scn.GetFreepsCard("battle");
		var nori = scn.GetFreepsCard("nori");
		var dain = scn.GetFreepsCard("dain");
		var bilbo = scn.GetRingBearer();
		var pursuer = scn.GetShadowCard("pursuer");
		var aggressor = scn.GetShadowCard("aggressor");
		var goblin = scn.GetShadowCard("goblin");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(nori);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToHand(battle);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, aggressor, goblin, bats);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(battle));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertEquals(0, scn.GetWoundsOn(nori));

		scn.FreepsPlayCard(battle);
		// A Dwarf companion and a Dwarf ally may pay; Bilbo, who could exert, is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(nori, dain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(dain);
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(0, scn.GetWoundsOn(nori));

		// Three Orcs, each able to take two wounds: either half of the text can be done in full.
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("Wound 2 Orcs");

		// Orcs of the [sauron], [gundabad] and [moria] cultures; not the Dark Bats, a [moria] minion who is no Orc.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pursuer, aggressor, goblin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bats));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCards(pursuer, goblin);
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(1, scn.GetWoundsOn(goblin));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(bats));
		assertInZone(Zone.DISCARD, battle);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WoundOneOrcTwiceIsAimedAtAnOrcAbleToTakeBothWoundsAndPlacesTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battle = scn.GetFreepsCard("battle");
		var nori = scn.GetFreepsCard("nori");
		var pursuer = scn.GetShadowCard("pursuer");
		var veteran = scn.GetShadowCard("veteran");
		var runner = scn.GetShadowCard("runner1");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(nori);
		scn.MoveCardsToHand(battle);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, veteran, runner, bats);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(nori));
		// Nori is the only Dwarf, so he is the one to exert.
		scn.FreepsPlayCard(battle);
		assertEquals(1, scn.GetWoundsOn(nori));

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("Wound 1 Orc twice");

		// The Goblin Runner, with 1 vitality, could not take both wounds and is not offered (Q-0209); the Dark Bats
		// are no Orc.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pursuer, veteran));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, bats));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(pursuer);
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(veteran));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(bats));
		assertInZone(Zone.DISCARD, battle);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ALoneOrcOfTwoVitalityIsWoundedTwiceWithNoQuestionAndDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battle = scn.GetFreepsCard("battle");
		var nori = scn.GetFreepsCard("nori");
		var veteran = scn.GetShadowCard("veteran");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(nori);
		scn.MoveCardsToHand(battle);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran, bats);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetWoundsOn(nori));
		scn.FreepsPlayCard(battle);

		// With one Orc there are not two to wound, so wounding the one twice is all that can be done in full.
		// Two wounds kill an Orc of 2 vitality, which two exertions could not.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetWoundsOn(nori));
		assertInZone(Zone.DISCARD, veteran);
		assertEquals(0, scn.GetWoundsOn(bats));
		assertInZone(Zone.DISCARD, battle);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TwoOrcsOfOneVitalityAreEachWoundedOnceWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battle = scn.GetFreepsCard("battle");
		var nori = scn.GetFreepsCard("nori");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(nori);
		scn.MoveCardsToHand(battle);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, bats);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SHADOW_CHARACTERS, runner1, runner2);
		assertEquals(0, scn.GetWoundsOn(nori));
		scn.FreepsPlayCard(battle);

		// Neither Orc can take two wounds, so wounding the two of them is all that can be done in full.  A
		// wound falls on a minion of 1 vitality, who could not exert.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetWoundsOn(nori));
		assertInZone(Zone.DISCARD, runner1, runner2);
		assertEquals(0, scn.GetWoundsOn(bats));
		assertInZone(Zone.DISCARD, battle);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOneOrcOfOneVitalityBothHalvesAreOfferedAndTheOneChosenDoesWhatItCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battle = scn.GetFreepsCard("battle");
		var nori = scn.GetFreepsCard("nori");
		var runner = scn.GetShadowCard("runner1");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(nori);
		scn.MoveCardsToHand(battle);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, bats);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(nori));
		scn.FreepsPlayCard(battle);
		assertEquals(1, scn.GetWoundsOn(nori));

		// There are not two Orcs, and the one cannot take two wounds: neither half can be done in full, so the
		// player has both to choose from, and as much of the one chosen as can be done is done.
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertInZone(Zone.SHADOW_CHARACTERS, runner);

		scn.FreepsChooseOption("Wound 1 Orc twice");
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(bats));
		assertInZone(Zone.DISCARD, battle);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoOrcInPlayTheDwarfExertsAndNothingIsAskedOrWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battle = scn.GetFreepsCard("battle");
		var nori = scn.GetFreepsCard("nori");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(nori);
		scn.MoveCardsToHand(battle);

		scn.StartGame();
		scn.MoveMinionsToTable(bats);
		scn.SkipToPhase(Phase.MANEUVER);

		// The Orcs are what the event acts on, not something it needs: it may be played with none.
		assertTrue(scn.FreepsPlayAvailable(battle));
		assertEquals(0, scn.GetWoundsOn(nori));

		scn.FreepsPlayCard(battle);
		assertEquals(1, scn.GetWoundsOn(nori));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(0, scn.GetWoundsOn(bats));
		assertInZone(Zone.DISCARD, battle);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyDwarfCharacterIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battle = scn.GetFreepsCard("battle");
		var nori = scn.GetFreepsCard("nori");
		var bilbo = scn.GetRingBearer();
		var pursuer = scn.GetShadowCard("pursuer");
		var aggressor = scn.GetShadowCard("aggressor");
		// Nori has 1 vitality left.
		scn.MoveCompanionsToTable(nori);
		scn.AddWoundsToChar(nori, 2);
		scn.MoveCardsToHand(battle);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, aggressor);
		scn.SkipToPhase(Phase.MANEUVER);

		// Bilbo, unwounded, could exert; he is no Dwarf.
		assertEquals(2, scn.GetWoundsOn(nori));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(battle));
		assertInZone(Zone.HAND, battle);
	}
}
