package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_183_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swarms", "1_183");
					put("swarms2", "1_183");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, strength 13, vitality 3, site 4; no text
					put("scout", "1_191");      // Moria Scout: [moria] Orc, strength 6, vitality 2; text only as it is played
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, cost 1, strength 5, vitality 1; adds (2), unasked, when played
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, cost 3; no other text
					put("troll", "1_165");      // Cave Troll of Moria: [moria] Troll, no Orc; strength 15, damage +1, fierce
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc, strength 5; +4 and fierce only at 2 wounds on the Ring-bearer
					put("spear", "1_182");      // Goblin Spear: [moria] hand weapon, strength +2, on a [moria] minion
					put("axe", "1_190");        // Moria Axe: [moria] hand weapon, strength +2, damage +1, on a [moria] Orc
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, vitality 3; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetStackedOrcsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swarms", "1_183");
					// Goblin Runner: [moria] Orc, cost 1, site 4.  "When you play this minion, you may add (2)",
					// which the game adds without asking: something that happens only to a card that is played.
					put("runner", "1_178");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, site 4, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinSwarmsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Goblin Swarms
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.<br><b>Response:</b> If your [moria] Orc wins a skirmish, discard cards and wounds on that Orc and stack that Orc on this condition.<br><b>Shadow:</b> Play an Orc stacked here as if played from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("swarms");

		assertEquals("Goblin Swarms", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowAbilityPlaysAnOrcStackedHereAsIfFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackedOrcsScenario();

		var swarms = scn.GetShadowCard("swarms");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(swarms);
		scn.StackCardsOn(swarms, runner, troop);

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 set + 1 companion + 2 for Ettenmoors: enough for either Orc (the Troop is 6 + 2 roaming)
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.STACKED, runner, troop);
		assertTrue(scn.ShadowActionAvailable(swarms));

		scn.ShadowUseCardAction(swarms);
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInZone(Zone.STACKED, troop);
		assertEquals(1, scn.GetStackedCards(swarms).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenAMoriaOrcWinsItsCardsGoToTheDiscardPileAndItIsStackedOnTheCopyWhoseResponseIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swarms = scn.GetShadowCard("swarms");
		var swarms2 = scn.GetShadowCard("swarms2");
		var troop = scn.GetShadowCard("troop");
		var scout = scn.GetShadowCard("scout");
		var spear = scn.GetShadowCard("spear");
		var axe = scn.GetShadowCard("axe");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(swarms, swarms2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, scout);
		scn.AttachCardsTo(troop, spear);
		scn.AttachCardsTo(scout, axe);
		scn.AddWoundsToChar(troop, 1);
		scn.AddWoundsToChar(scout, 1);

		// The Troop fights Aragorn; the Scout, with a weapon and a wound of its own, stays out of every skirmish.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// 13 + 2 against 8: the Troop wins, and each copy of Goblin Swarms offers its response.
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(swarms));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(swarms2));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertAttachedTo(spear, troop);

		scn.ShadowChooseOptionalTrigger(swarms2);
		assertInZone(Zone.STACKED, troop);
		assertTrue(scn.GetStackedCards(swarms2).contains(troop));
		assertEquals(0, scn.GetStackedCards(swarms).size());
		assertInZone(Zone.DISCARD, spear);
		assertEquals(0, scn.GetWoundsOn(troop));
		// The Scout, not in the skirmish, keeps its Axe and its wound.
		assertAttachedTo(axe, scout);
		assertEquals(1, scn.GetWoundsOn(scout));

		// With the Troop gone, the other copy has nothing left to do and is not offered again.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(swarms));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenAMoriaOrcLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swarms = scn.GetShadowCard("swarms");
		var scout = scn.GetShadowCard("scout");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(swarms);

		scn.StartGame();
		scn.MoveMinionsToTable(scout);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, scout);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// 6 against 8: the Scout loses and takes a wound, and Goblin Swarms is not offered.
		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SHADOW_CHARACTERS, scout);
		assertEquals(0, scn.GetStackedCards(swarms).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyAMoriaOrcThatWonThatSkirmishCanBeStacked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swarms = scn.GetShadowCard("swarms");
		var troop = scn.GetShadowCard("troop");
		var scout = scn.GetShadowCard("scout");
		var runner = scn.GetShadowCard("runner");
		var troll = scn.GetShadowCard("troll");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(swarms);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, scout, runner, troll, hound);

		// Aragorn takes the Troop and the Shadow player gives him the Scout, the Cave Troll and the Morgul Hound
		// too, so that a [moria] Troll and a [wraith] Orc win beside the two [moria] Orcs.  The Runner is a third
		// [moria] Orc, assigned to Gimli, whose skirmish has not begun.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{gimli, runner});
		scn.ShadowAssignToMinions(aragorn, scout, troll, hound);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Engine pass 54: what answers the win comes before the loser loses.
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(swarms));
		scn.ShadowChooseOptionalTrigger(swarms);

		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, scout));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll, hound, runner));
		scn.ShadowChooseCard(scout);

		assertTrue(scn.GetStackedCards(swarms).contains(scout));
		assertEquals(1, scn.GetStackedCards(swarms).size());
		assertInZone(Zone.SHADOW_CHARACTERS, troop, troll, hound, runner);

		// The Troop won as well, and the response may be used again for it; declined, Gimli's skirmish is next.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(swarms));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void TheShadowAbilityOffersOnlyTheOrcsStackedOnThisCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swarms = scn.GetShadowCard("swarms");
		var swarms2 = scn.GetShadowCard("swarms2");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToSupportArea(swarms, swarms2);
		scn.StackCardsOn(swarms, runner, troop);
		scn.StackCardsOn(swarms2, marksman);

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 set + 1 companion + 2 for Ettenmoors
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(swarms));
		scn.ShadowUseCardAction(swarms);

		// The Marksman, stacked on the other copy and affordable (3 + 2 roaming), is not offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(marksman));
		scn.ShadowChooseCard(troop);

		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.STACKED, runner, marksman);
		assertTrue(scn.GetStackedCards(swarms2).contains(marksman));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShadowAbilityIsNotOfferedUntilThePoolCanPayForAnOrcStackedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swarms = scn.GetShadowCard("swarms");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(swarms);
		scn.StackCardsOn(swarms, runner, troop);

		scn.StartGame();
		scn.FreepsPass();

		// The Runner is the cheaper: 1 + 2 roaming at Ettenmoors.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(swarms));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(swarms));
		scn.ShadowUseCardAction(swarms);

		// Only the Runner can be paid for, so it is played; the Troop stays where it is.
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInZone(Zone.STACKED, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
