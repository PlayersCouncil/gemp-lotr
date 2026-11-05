package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_008_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The maneuver phase of
	// turn 1 is at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("roac", "32_8");

					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] Dwarf companion, vitality 4; a fellowship ability only
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion who is no Thorin, strength 7; a fellowship ability only

					// [dwarven] allies of two titles
					put("dain", "32_2");        // Dain Ironfoot: [dwarven] Dwarf ally, strength 7, vitality 3; his regroup trigger needs a [dwarven] follower
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] Dwarf ally; a fellowship ability only
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] Dwarf ally; a maneuver ability not used
					// Cards that are no [dwarven] ally
					put("fili", "30_10");       // Fili, Brother of Kili: a [dwarven] companion; a response to a wound
					put("bain", "32_15");       // Bain, Son of Bard: an [esgaroth] Man ally; a start-of-turn heal of a wounded [esgaroth] Man

					put("runner", "31_30");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; a "when you play" trigger, cheated in
					put("rider", "12_161");     // Black Rider: [wraith] Nazgul, fierce, strength 8 +1 for each companion, vitality 3
					// Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
					put("troop", "1_177");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void RoacStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Roäc, Son of Carc
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Follower
		 * Subtype: Bird
		 * Strength: +1
		 * Aid: Exert Thorin.
		 * Game Text: <b>Aid</b> – Exert Thorin.
		 * 	<b>Maneuver:</b> If Roac is attached, discard him to play a [dwarven] ally from your draw deck, or to allow a [dwarven] ally to participate in skirmishes until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("roac");

		assertEquals("Roäc", card.getBlueprint().getTitle());
		assertEquals("Son of Carc", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(Race.BIRD, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void AidExertsThorinToTransferRoacToACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var roac = scn.GetFreepsCard("roac");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveCardsToSupportArea(roac);
		assertInZone(Zone.SUPPORT, roac);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SUPPORT, roac);
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(roac, "Aid"));

		scn.FreepsAcceptOptionalTrigger();
		// Thorin is the one who can pay: he exerts unasked, and Balin, another [dwarven] companion, does not.
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));

		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, thorin, balin));
		scn.FreepsChooseCard(balin);
		assertAttachedTo(roac, balin);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AidIsNotOfferedWhileThorinIsExhaustedAndRoacUnattachedHasNoManeuverAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var roac = scn.GetFreepsCard("roac");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var grimir = scn.GetFreepsCard("grimir");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(roac);
		// Vitality 4 with 3 wounds: Thorin cannot exert.
		scn.AddWoundsToChar(thorin, 3);
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertInZone(Zone.SUPPORT, roac, grimir);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		// Something for either branch, were he attached: a [dwarven] ally in the deck and one in play.
		assertInZone(Zone.DECK, dain);

		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(roac, "Aid"));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInZone(Zone.SUPPORT, roac);
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		// In the support area he is not attached.
		assertFalse(scn.FreepsActionAvailable(roac));
	}

	@Test
	public void DiscardedHePlaysADwarvenAllyFromTheDrawDeckAndTheDeckIsShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var roac = scn.GetFreepsCard("roac");
		var balin = scn.GetFreepsCard("balin");
		var grimir = scn.GetFreepsCard("grimir");
		var dain = scn.GetFreepsCard("dain");
		var thrarin = scn.GetFreepsCard("thrarin");
		var fili = scn.GetFreepsCard("fili");
		var bain = scn.GetFreepsCard("bain");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, roac);
		// A [dwarven] ally in play, so that either branch can be taken.
		scn.MoveAlliesToTable(grimir);
		assertAttachedTo(roac, balin);
		assertInZone(Zone.SUPPORT, grimir);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		assertInZone(Zone.DECK, dain, thrarin, fili, bain);

		scn.SkipToPhase(Phase.MANEUVER);
		// With no Thorin in play the Aid is not offered.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(roac));
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsUseCardAction(roac);
		assertInDiscard(roac);
		assertTrue(scn.FreepsChoiceAvailable("Play a"));
		assertTrue(scn.FreepsChoiceAvailable("participate"));
		scn.FreepsChoose("Play a");

		scn.FreepsDismissRevealedCards();
		// Not a [dwarven] companion, nor an ally of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(dain, thrarin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(fili, bain));
		// A play from a draw deck is 0 to 1 (Q-0019).
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(dain);

		assertInZone(Zone.SUPPORT, dain, grimir);
		assertInZone(Zone.DECK, thrarin, fili, bain);
		// As the ally left the deck looked through, and the shuffle after the play; the other player's is not.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DiscardedHeAllowsADwarvenAllyToParticipateInSkirmishesUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var roac = scn.GetFreepsCard("roac");
		var balin = scn.GetFreepsCard("balin");
		var fili = scn.GetFreepsCard("fili");
		var grimir = scn.GetFreepsCard("grimir");
		var dain = scn.GetFreepsCard("dain");
		var bain = scn.GetFreepsCard("bain");
		var runner = scn.GetShadowCard("runner");
		var rider = scn.GetShadowCard("rider");
		scn.MoveCompanionsToTable(balin, fili);
		scn.AttachCardsTo(balin, roac);
		scn.MoveAlliesToTable(grimir, dain, bain);
		assertAttachedTo(roac, balin);
		assertInZone(Zone.SUPPORT, grimir, dain, bain);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, rider);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsCanAssign(dain));
		assertFalse(scn.FreepsCanAssign(grimir));
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsUseCardAction(roac);
		assertInDiscard(roac);
		scn.FreepsChoose("participate");
		// Not an ally of another culture, nor a [dwarven] companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(dain, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bain, balin, fili));
		scn.FreepsChooseCard(dain);

		assertTrue(scn.FreepsCanAssign(dain));
		assertFalse(scn.FreepsCanAssign(grimir));
		assertFalse(scn.FreepsCanAssign(bain));
		// No draw deck was looked through.
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// The fierce Black Rider is left out of the first assignment; the regroup phase is still ahead of the fierce one.
		scn.SkipToAssignments();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(dain));
		scn.FreepsAssignAndResolve(balin, runner);
		scn.PassSkirmishActions();

		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsCanAssign(dain));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(dain));
		scn.FreepsAssignAndResolve(dain, rider);
		assertTrue(scn.IsCharSkirmishingAgainst(dain, rider));
		scn.PassSkirmishActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsCanAssign(dain));
	}

	@Test
	public void WithNoDwarvenAllyInPlayTheAllyIsPlayedFromTheDeckUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var roac = scn.GetFreepsCard("roac");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		var bain = scn.GetFreepsCard("bain");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, roac);
		// An ally in play, of another culture.
		scn.MoveAlliesToTable(bain);
		assertAttachedTo(roac, balin);
		assertInZone(Zone.SUPPORT, bain);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		assertInZone(Zone.DECK, dain);

		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(roac);
		assertInDiscard(roac);
		// No question of which branch: the deck is shown at once.
		assertFalse(scn.FreepsChoiceAvailable("participate"));
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(dain);
		assertInZone(Zone.SUPPORT, dain);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoDwarvenAllyInTheDeckTheAllowingIsChosenUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var roac = scn.GetFreepsCard("roac");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		var grimir = scn.GetFreepsCard("grimir");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, roac);
		scn.MoveAlliesToTable(dain, grimir);
		scn.MoveCardsToDiscard(thrarin);
		assertAttachedTo(roac, balin);
		assertInZone(Zone.SUPPORT, dain, grimir);
		assertInDiscard(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		assertTrue(scn.FreepsActionAvailable(roac));
		scn.FreepsUseCardAction(roac);
		assertInDiscard(roac);
		// No [dwarven] ally in the deck: the play from it is not doable in full (Q-0019; the orchestrator on Q-0406
		// and Q-0418), so the other branch is carried out with no question (F-0310).
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(dain, grimir));
		scn.FreepsChooseCard(dain);

		assertTrue(scn.FreepsCanAssign(dain));
		assertFalse(scn.FreepsCanAssign(grimir));
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoDwarvenAllyAnywhereTheManeuverActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var roac = scn.GetFreepsCard("roac");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		var grimir = scn.GetFreepsCard("grimir");
		var thrarin = scn.GetFreepsCard("thrarin");
		var fili = scn.GetFreepsCard("fili");
		var bain = scn.GetFreepsCard("bain");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, roac);
		scn.MoveAlliesToTable(bain);
		scn.MoveCardsToDiscard(dain, grimir, thrarin);
		assertAttachedTo(roac, balin);
		assertInZone(Zone.SUPPORT, bain);
		assertInDiscard(dain, grimir, thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		assertInZone(Zone.DECK, fili);

		scn.SkipToPhase(Phase.MANEUVER);
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		// No [dwarven] ally in play to allow, and none in the deck to play.  A Hobbit-set play from the draw deck with nothing it could play is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418),
		// so neither branch can do anything and Roäc is not discarded for nothing (F-0310).
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(roac));
		assertAttachedTo(roac, balin);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
	}
}
