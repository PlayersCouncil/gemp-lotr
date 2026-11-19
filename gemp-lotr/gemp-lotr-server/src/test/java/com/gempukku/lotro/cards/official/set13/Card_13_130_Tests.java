package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_130_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("barrows", "13_130");
					put("barrows2", "13_130");
					put("guard1", "5_83");      // Household Guard: [rohan] Man companion, strength 6; Valiant is its whole text
					put("mount1", "4_287");     // Rider's Mount: a [rohan] mount, no bonus; its minions must exert, which vitality 1 cannot
					put("gamling", "5_82");     // Gamling, Warrior of Rohan: [rohan] Man companion, strength 6; a skirmish ability used only on purpose
					put("horse1", "4_283");     // Horse of Rohan: a mount any Man may bear, no bonus; the same exertion
					put("guard2", "5_83");      // a [rohan] companion bearing a possession that is no mount
					put("sword1", "4_291");     // Sword of Rohan: a hand weapon, strength +2
					put("faramir", "12_47");    // Faramir, Dúnadan of Gondor: a [gondor] Man companion, strength 7, mounted on the second Horse
					put("horse2", "4_283");
					put("herdsman1", "7_247");  // Rohirrim Herdsman: a [rohan] Man ally, mounted on the second Rider's Mount
					put("mount2", "4_287");
					// [rohan] cards of five kinds for the discard pile
					put("guard3", "5_83");      // a companion
					put("sword2", "4_291");     // a possession
					put("doom", "8_86");        // Doom Drove Them: a condition
					put("villagers", "4_273");  // Fight for the Villagers: an event
					put("herdsman2", "7_247");  // an ally
					// A card of another culture for the discard pile
					put("shield", "1_107");     // Great Shield, [gondor]
					// A [rohan] card of a title only the Shadow player's discard pile holds
					put("chain", "4_278");      // Heavy Chain
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HurriedBarrowsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Hurried Barrows
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: You may remove from the game 4 other [rohan] cards in your discard pile to play this event from your discard pile. Then remove this event from the game.<br>Make your mounted [rohan] companion strength +3.
		*/

		var scn = GetScenario();

		var barrows = scn.GetFreepsCard("barrows");

		assertEquals("Hurried Barrows", barrows.getBlueprint().getTitle());
		assertNull(barrows.getBlueprint().getSubtitle());
		assertFalse(barrows.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, barrows.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, barrows.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, barrows.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(barrows, Timeword.SKIRMISH));
		assertEquals(2, barrows.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedFromTheDiscardPileByRemovingFourOtherRohanCardsItMakesAMountedRohanCompanionStrengthPlusThreeAndIsRemovedFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barrows = scn.GetFreepsCard("barrows");
		var barrows2 = scn.GetFreepsCard("barrows2");
		var guard1 = scn.GetFreepsCard("guard1");
		var mount1 = scn.GetFreepsCard("mount1");
		var gamling = scn.GetFreepsCard("gamling");
		var horse1 = scn.GetFreepsCard("horse1");
		var guard2 = scn.GetFreepsCard("guard2");
		var sword1 = scn.GetFreepsCard("sword1");
		var faramir = scn.GetFreepsCard("faramir");
		var horse2 = scn.GetFreepsCard("horse2");
		var herdsman1 = scn.GetFreepsCard("herdsman1");
		var mount2 = scn.GetFreepsCard("mount2");
		var guard3 = scn.GetFreepsCard("guard3");
		var sword2 = scn.GetFreepsCard("sword2");
		var doom = scn.GetFreepsCard("doom");
		var villagers = scn.GetFreepsCard("villagers");
		var herdsman2 = scn.GetFreepsCard("herdsman2");
		var shield = scn.GetFreepsCard("shield");
		var chain = scn.GetShadowCard("chain");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard1, gamling, guard2, faramir);
		scn.MoveAlliesToTable(herdsman1);
		scn.AttachCardsTo(guard1, mount1);
		scn.AttachCardsTo(gamling, horse1);
		scn.AttachCardsTo(guard2, sword1);
		scn.AttachCardsTo(faramir, horse2);
		scn.AttachCardsTo(herdsman1, mount2);
		scn.MoveCardsToDiscard(barrows, guard3, sword2, doom, villagers, herdsman2, shield);
		scn.MoveCardsToDiscard(chain);
		scn.MoveCardsToHand(barrows2);

		assertAttachedTo(mount1, guard1);
		assertAttachedTo(horse1, gamling);
		assertAttachedTo(sword1, guard2);
		assertAttachedTo(horse2, faramir);
		assertAttachedTo(mount2, herdsman1);
		scn.StartGame();
		assertInZone(Zone.DISCARD, barrows, guard3, sword2, doom, villagers, herdsman2, shield, chain);
		assertInZone(Zone.HAND, barrows2);
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveMinionsToTable(runner1, runner2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard1, runner1},
				new PhysicalCardImpl[]{gamling, runner2});
		scn.FreepsResolveSkirmish(guard1);

		assertInZone(Zone.DISCARD, barrows, guard3, sword2, doom, villagers, herdsman2, shield, chain);
		assertInZone(Zone.HAND, barrows2);
		assertEquals(6, scn.GetStrength(guard1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(barrows));
		scn.FreepsUseCardAction(barrows);
		// Five other [rohan] cards, of five kinds, for the four.  Not the event itself, a card of another culture,
		// or the [rohan] card in the other player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard3, sword2, doom, villagers, herdsman2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barrows, shield, chain));
		assertEquals(4, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(guard3, sword2, doom, villagers);
		assertInZone(Zone.REMOVED, guard3, sword2, doom, villagers);
		assertInZone(Zone.DISCARD, herdsman2, shield, chain);

		// Your mounted [rohan] companion: the Guard in this skirmish, Gamling assigned and waiting.  Not the Guard
		// bearing a sword and no mount, the mounted [gondor] Faramir, the mounted [rohan] ally, or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard1, gamling));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard2, faramir, herdsman1, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard1);
		assertEquals(9, scn.GetStrength(guard1));
		// The copy played is removed from the game; the copy in hand stays there.
		assertInZone(Zone.REMOVED, barrows);
		assertInZone(Zone.HAND, barrows2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The next skirmish is a phase of its own: the Guard is 6 again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gamling);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(guard1));
	}

	@Test
	public void NotPlayableFromTheDiscardPileWithOnlyThreeOtherRohanCardsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barrows = scn.GetFreepsCard("barrows");
		var guard1 = scn.GetFreepsCard("guard1");
		var mount1 = scn.GetFreepsCard("mount1");
		var sword2 = scn.GetFreepsCard("sword2");
		var doom = scn.GetFreepsCard("doom");
		var villagers = scn.GetFreepsCard("villagers");
		var herdsman2 = scn.GetFreepsCard("herdsman2");
		var shield = scn.GetFreepsCard("shield");
		scn.MoveCompanionsToTable(guard1);
		scn.AttachCardsTo(guard1, mount1);
		// Four [rohan] cards with the event itself, and a fifth card of another culture.
		scn.MoveCardsToDiscard(barrows, sword2, doom, villagers, shield);

		assertAttachedTo(mount1, guard1);
		scn.StartGame();
		assertInZone(Zone.DISCARD, barrows, sword2, doom, villagers, shield);
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard1, runner1);
		scn.FreepsResolveSkirmish(guard1);

		assertInZone(Zone.DISCARD, barrows, sword2, doom, villagers, shield);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(barrows));

		// A fourth other [rohan] card.
		scn.MoveCardsToDiscard(herdsman2);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(barrows));
	}

	@Test
	public void NotPlayableInTheAssignmentPhaseAndFromHandWithNoMountedRohanCompanionItGoesToTheDiscardPileAndDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barrows = scn.GetFreepsCard("barrows");
		var barrows2 = scn.GetFreepsCard("barrows2");
		var guard2 = scn.GetFreepsCard("guard2");
		var sword1 = scn.GetFreepsCard("sword1");
		var faramir = scn.GetFreepsCard("faramir");
		var horse2 = scn.GetFreepsCard("horse2");
		var herdsman1 = scn.GetFreepsCard("herdsman1");
		var mount2 = scn.GetFreepsCard("mount2");
		var sword2 = scn.GetFreepsCard("sword2");
		var doom = scn.GetFreepsCard("doom");
		var villagers = scn.GetFreepsCard("villagers");
		var herdsman2 = scn.GetFreepsCard("herdsman2");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard2, faramir);
		scn.MoveAlliesToTable(herdsman1);
		scn.AttachCardsTo(guard2, sword1);
		scn.AttachCardsTo(faramir, horse2);
		scn.AttachCardsTo(herdsman1, mount2);
		scn.MoveCardsToHand(barrows);
		// The second copy lies in the discard pile with four other [rohan] cards.
		scn.MoveCardsToDiscard(barrows2, sword2, doom, villagers, herdsman2);

		assertAttachedTo(sword1, guard2);
		assertAttachedTo(horse2, faramir);
		assertAttachedTo(mount2, herdsman1);
		scn.StartGame();
		assertInZone(Zone.HAND, barrows);
		assertInZone(Zone.DISCARD, barrows2, sword2, doom, villagers, herdsman2);
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveMinionsToTable(runner1);

		// A Skirmish event: neither copy at the assignment-phase decision.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(barrows));
		assertFalse(scn.FreepsActionAvailable(barrows2));
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(guard2, runner1);
		scn.FreepsResolveSkirmish(guard2);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(barrows));
		assertTrue(scn.FreepsActionAvailable(barrows2));
		// 6 and the Sword's +2
		assertEquals(8, scn.GetStrength(guard2));
		assertEquals(7, scn.GetStrength(faramir));
		assertEquals(4, scn.GetStrength(herdsman1));
		assertEquals(4, scn.GetStrength(frodo));
		// No mounted [rohan] companion: the event is played for nothing, and from hand it goes to the discard pile
		// with nothing removed from there.
		scn.FreepsPlayCard(barrows);
		assertEquals(8, scn.GetStrength(guard2));
		assertEquals(7, scn.GetStrength(faramir));
		assertEquals(4, scn.GetStrength(herdsman1));
		assertEquals(4, scn.GetStrength(frodo));
		assertInZone(Zone.DISCARD, barrows, barrows2, sword2, doom, villagers, herdsman2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
