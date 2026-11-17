package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_021_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer; the skirmishes of
	// turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("loses1", "31_21");
					put("loses2", "31_21");

					put("balin", "30_2");       // Balin, Brother of Dwalin: Dwarf companion, strength 7; a fellowship ability not used
					put("nori", "30_15");       // Nori, Of the Blue Mountains: Dwarf companion, strength 7; a maneuver ability not used

					// Gollum, Small Slimy Creature: the Hobbit sets' Gollum, [gollum] minion, vitality 4.  He only pays here
					// and never wins a skirmish, so his own "wins a skirmish" offer never comes.
					put("gollum", "31_20");
					// Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum, vitality 8; enduring, fierce
					put("shelob", "8_26");
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, strength 6; an assignment ability not used
					put("runner", "31_30");     // Goblin Runner: [moria] Orc, strength 5; a play trigger only (cheated in)
					put("bats", "32_30");       // Dark Bats: [moria] creature, strength 4; a Shadow ability not used
					// The Great Goblin, Chieftain of the Misty Mountains: [moria] Orc, strength 10, fierce; a maneuver
					// ability not used
					put("goblin", "31_36");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void IfHeLosesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: If He Loses
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: You may exert Gollum twice to play this event from your discard pile.
		 * Make a minion strength +2. If this minion wins this skirmish, you may add a doubt.
		 */

		var scn = GetScenario();

		var card = scn.GetShadowCard("loses1");

		assertEquals("If He Loses", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FromHandItMakesAnyMinionPlusTwoForThatSkirmishAndItsWinMayAddADoubt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loses = scn.GetShadowCard("loses1");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var gollum = scn.GetShadowCard("gollum");
		var aggressor = scn.GetShadowCard("aggressor");
		var runner = scn.GetShadowCard("runner");
		var bats = scn.GetShadowCard("bats");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin, nori);
		scn.MoveCardsToHand(loses);

		scn.StartGame();
		assertInZone(Zone.HAND, loses);
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(gollum, aggressor, runner, bats);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, aggressor}, new PhysicalCardImpl[]{nori, bats});
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(loses));
		scn.ShadowPlayCard(loses);
		// Any minion, in this skirmish, waiting for another or in none; no Free Peoples character.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, bats, runner, gollum));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(balin, nori, bilbo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(6, scn.GetStrength(aggressor));
		scn.ShadowChooseCard(aggressor);
		// 6 + 2; played from hand, it exerts no Gollum.
		assertEquals(8, scn.GetStrength(aggressor));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertInZone(Zone.DISCARD, loses);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		assertEquals(0, scn.GetBurdens());
		scn.PassSkirmishActions();
		// 8 against Balin's 7: the Aggressor wins.
		assertEquals(1, scn.GetWoundsOn(balin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(loses));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());

		// Nori's skirmish is another skirmish: the bonus is gone.
		scn.FreepsResolveSkirmish(nori);
		assertEquals(6, scn.GetStrength(aggressor));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheDoubtMayBeDeclinedAndAFierceSkirmishWonIsAnsweredByNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loses = scn.GetShadowCard("loses1");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCompanionsToTable(balin, nori);
		scn.MoveCardsToHand(loses);

		scn.StartGame();
		assertInZone(Zone.HAND, loses);
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(goblin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, goblin);
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();

		// The Great Goblin is the only minion, and is made stronger with nothing asked.
		scn.ShadowPlayCard(loses);
		assertEquals(12, scn.GetStrength(goblin));

		assertEquals(0, scn.GetBurdens());
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(balin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(loses));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetBurdens());

		// The Great Goblin is fierce: his fierce skirmish is another skirmish, and his win there adds nothing.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(nori, goblin);
		scn.FreepsResolveSkirmish(nori);
		assertEquals(10, scn.GetStrength(goblin));
		scn.PassFierceSkirmishActions();
		// 10 against 7: he wins again, and nothing is offered.
		assertEquals(1, scn.GetWoundsOn(nori));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenTheMinionMadeStrongerLosesOrIsNotTheOneThatWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loses1 = scn.GetShadowCard("loses1");
		var loses2 = scn.GetShadowCard("loses2");
		var balin = scn.GetFreepsCard("balin");
		var aggressor = scn.GetShadowCard("aggressor");
		var runner = scn.GetShadowCard("runner");
		var bats = scn.GetShadowCard("bats");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToHand(loses1, loses2);

		scn.StartGame();
		assertInZone(Zone.HAND, loses1, loses2);
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(aggressor, runner, bats);
		// Balin at 10, to beat the Aggressor's 6 + 2.
		scn.ApplyAdHocModifier(new StrengthModifier(null, balin, null, 3));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bilbo, bats}, new PhysicalCardImpl[]{balin, aggressor});
		scn.ShadowDeclineAssignments();

		// Bilbo's skirmish: the Goblin Runner, in no skirmish, is made stronger, and the Dark Bats win.
		scn.FreepsResolveSkirmish(bilbo);
		scn.FreepsPass();
		scn.ShadowPlayCard(loses1);
		scn.ShadowChooseCard(runner);
		assertEquals(7, scn.GetStrength(runner));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(bilbo));
		scn.PassSkirmishActions();
		// 4 against 3.
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());

		// Balin's skirmish: the Aggressor is made stronger and loses.
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();
		scn.ShadowPlayCard(loses2);
		scn.ShadowChooseCard(aggressor);
		assertEquals(8, scn.GetStrength(aggressor));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		scn.PassSkirmishActions();
		// 8 against 10.
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void FromTheDiscardPileItExertsGollumTwiceInASkirmishAndNotInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loses1 = scn.GetShadowCard("loses1");
		var loses2 = scn.GetShadowCard("loses2");
		var balin = scn.GetFreepsCard("balin");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToShadowDiscard("loses1");
		scn.MoveCardsToHand(loses2);

		scn.StartGame();
		assertInZone(Zone.DISCARD, loses1);
		assertInZone(Zone.HAND, loses2);
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(gollum, shelob, aggressor);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		// The Shadow player's assignment actions: neither copy may be played.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(loses1));
		assertFalse(scn.ShadowPlayAvailable(loses2));
		scn.ShadowPass();
		scn.FreepsAssignToMinions(balin, aggressor);
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertTrue(scn.ShadowActionAvailable(loses1));
		scn.ShadowUseCardAction(loses1);
		// Gollum is the only Gollum and exerts with nothing asked; Shelob is no Gollum.
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, gollum, shelob));
		scn.ShadowChooseCard(aggressor);
		// 6 + 2
		assertEquals(8, scn.GetStrength(aggressor));
		assertInZone(Zone.DISCARD, loses1);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void FromTheDiscardPileItIsNotOfferedWhenGollumCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loses1 = scn.GetShadowCard("loses1");
		var loses2 = scn.GetShadowCard("loses2");
		var balin = scn.GetFreepsCard("balin");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToShadowDiscard("loses1");
		scn.MoveCardsToHand(loses2);

		scn.StartGame();
		assertInZone(Zone.DISCARD, loses1);
		assertInZone(Zone.HAND, loses2);
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(gollum, shelob, aggressor);
		// Vitality 4 with 2 wounds: Gollum can exert once more, not twice.  Shelob, fresh, is no Gollum.
		scn.AddWoundsToChar(gollum, 2);
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, aggressor);
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(loses1));
		// The copy in hand is still offered.
		assertTrue(scn.ShadowPlayAvailable(loses2));
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
	}
}
