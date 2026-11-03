package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_110_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("teeth", "5_110");
					put("teeth2", "5_110");
					put("soldier", "1_271");     // Orc Soldier: [sauron] Orc, cost 2, site 6; a skirmish ability never used
					put("trooper", "3_99");      // Orc Trooper: [sauron] Orc, cost 4, site 6; strength +1 per card in hand only
					// Orc Inquisitor: [sauron] Orc, cost 3, site 6. "When you play this minion, you may make the Free
					// Peoples player discard a card at random from hand."
					put("inquisitor", "1_268");
					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc, no text
					put("guard", "10_81");       // Cirith Ungol Guard: [sauron] Uruk-hai, no Orc; damage +1 only

					put("sam", "1_311");         // Sam, Son of Hamfast: a card for the Free Peoples hand; used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TeethofMordorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Teeth of Mordor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br><b>Regroup:</b> Spot a [sauron] Orc and remove (3) to stack that minion here.<br><b>Shadow:</b> Play a [sauron] Orc stacked here as if played from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("teeth");

		assertEquals("Teeth of Mordor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RegroupAbilityRemovesThreeToStackASpottedSauronOrcHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var teeth = scn.GetShadowCard("teeth");
		var teeth2 = scn.GetShadowCard("teeth2");
		var soldier = scn.GetShadowCard("soldier");
		var trooper = scn.GetShadowCard("trooper");
		var troop = scn.GetShadowCard("troop");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToSupportArea(teeth, teeth2);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, trooper, troop, guard);

		// Nobody is assigned, so every minion is still in play in the regroup phase.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsPass();
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetStackedCards(teeth).size());
		assertEquals(0, scn.GetStackedCards(teeth2).size());
		assertTrue(scn.ShadowActionAvailable(teeth));
		scn.ShadowUseCardAction(teeth);

		assertEquals(1, scn.ShadowGetChoiceMin());
		// Two [sauron] Orcs of different titles.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, trooper));
		// A [moria] Orc; a [sauron] Uruk-hai, no Orc; the other Teeth, a [sauron] card that is no minion.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, guard, teeth2));
		scn.ShadowChooseCard(trooper);

		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.STACKED, trooper);
		assertTrue(scn.GetStackedCards(teeth).contains(trooper));
		assertEquals(1, scn.GetStackedCards(teeth).size());
		assertEquals(0, scn.GetStackedCards(teeth2).size());
		assertInZone(Zone.SHADOW_CHARACTERS, soldier, troop, guard);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedUntilThreeTwilightCanBeRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var teeth = scn.GetShadowCard("teeth");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(teeth);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsPass();
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(teeth));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(teeth));
		scn.ShadowUseCardAction(teeth);

		// The Soldier is the only one that can be chosen.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.GetStackedCards(teeth).contains(soldier));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithNoSauronOrcToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var teeth = scn.GetShadowCard("teeth");
		var troop = scn.GetShadowCard("troop");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToSupportArea(teeth);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, guard);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.SHADOW_CHARACTERS, troop, guard);
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(teeth));
	}

	@Test
	public void ShadowAbilityPlaysASauronOrcStackedHereAsIfFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var teeth = scn.GetShadowCard("teeth");
		var teeth2 = scn.GetShadowCard("teeth2");
		var inquisitor = scn.GetShadowCard("inquisitor");
		var soldier = scn.GetShadowCard("soldier");
		var trooper = scn.GetShadowCard("trooper");
		scn.MoveCardsToSupportArea(teeth, teeth2);
		scn.StackCardsOn(teeth, inquisitor, soldier);
		scn.StackCardsOn(teeth2, trooper);
		// A card in the Free Peoples hand, so that the Inquisitor's own trigger has something to do.
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToHand(sam);

		scn.StartGame();
		assertInZone(Zone.STACKED, inquisitor, soldier, trooper);
		assertInZone(Zone.HAND, sam);
		scn.FreepsPass();
		// Enough for any of them: the Trooper is 4 + 2 roaming at Ettenmoors.
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertEquals(2, scn.GetStackedCards(teeth).size());
		assertTrue(scn.GetStackedCards(teeth2).contains(trooper));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(teeth));
		scn.ShadowUseCardAction(teeth);

		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(inquisitor, soldier));
		// The Orc stacked on the other Teeth is not stacked here.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(trooper));
		scn.ShadowChooseCard(inquisitor);

		assertInZone(Zone.SHADOW_CHARACTERS, inquisitor);
		// Played as if from hand, so his own "when you play" trigger answers.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(inquisitor));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.STACKED, soldier, trooper);
		assertEquals(1, scn.GetStackedCards(teeth).size());
		assertTrue(scn.GetStackedCards(teeth2).contains(trooper));
		assertInZone(Zone.HAND, sam);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedUntilThePoolCanPayForASauronOrcStackedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var teeth = scn.GetShadowCard("teeth");
		var inquisitor = scn.GetShadowCard("inquisitor");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(teeth, inquisitor, soldier);

		scn.StartGame();
		assertInZone(Zone.STACKED, inquisitor, soldier);
		scn.FreepsPass();

		// The Soldier is 2 + 2 roaming at Ettenmoors; the Inquisitor 3 + 2.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetStackedCards(teeth).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(teeth));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(teeth));
		scn.ShadowUseCardAction(teeth);

		// The Soldier is the only one the pool can pay for, and is played.
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertInZone(Zone.STACKED, inquisitor);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
