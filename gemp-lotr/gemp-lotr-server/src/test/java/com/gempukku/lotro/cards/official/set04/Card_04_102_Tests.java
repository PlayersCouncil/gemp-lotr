package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_102_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("task", "4_102");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: strength 7, vitality 4; a fellowship ability used only on purpose
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf, strength 6, vitality 4

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, vitality 2; +3 and fierce only while 2 burdens can be spotted
					put("rider", "12_161");     // Black Rider: [wraith] Nazgul, fierce, vitality 3; strength +1 for each companion
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TaskWasNotDoneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Task Was Not Done
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Spell</b>.<br><b>Regroup:</b> Exert Gandalf to discard up to 2 wounded minions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("task");

		assertEquals("Task Was Not Done", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsGandalfToDiscardUpTo2WoundedMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var task = scn.GetFreepsCard("task");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(task);

		var savage = scn.GetShadowCard("savage");
		var axeman = scn.GetShadowCard("axeman");
		var rider = scn.GetShadowCard("rider");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddWoundsToChar(avenger, 1);
		assertEquals(1, scn.GetWoundsOn(avenger));
		assertInHand(task);
		scn.FreepsPass();
		// Laid in the Shadow phase and left unassigned, they live on into the regroup phase.
		scn.MoveMinionsToTable(savage, axeman, rider, troop);
		scn.AddWoundsToChar(savage, 1);
		scn.AddWoundsToChar(axeman, 1);
		scn.AddWoundsToChar(rider, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(avenger));
		assertInHand(task);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(task));

		// Gandalf, the one Gandalf, exerts unasked.
		scn.FreepsPlayCard(task);
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// Wounded minions of three cultures and races; not an unwounded minion, nor a wounded companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, axeman, rider));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop, avenger, gandalf));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(savage, rider);

		assertInDiscard(savage);
		assertInDiscard(rider);
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.FREE_CHARACTERS, avenger);
		assertInDiscard(task);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoWoundedMinionItIsPlayedAndOnlyGandalfExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var task = scn.GetFreepsCard("task");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(task);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddWoundsToChar(avenger, 1);
		assertEquals(1, scn.GetWoundsOn(avenger));
		assertInHand(task);
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(task));

		scn.FreepsPlayCard(task);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.FREE_CHARACTERS, avenger);
		assertInDiscard(task);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileGandalfIsExhaustedThoughAnotherGandalfCompanionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var task = scn.GetFreepsCard("task");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(task);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.AddWoundsToChar(gandalf, 3);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertInHand(task);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(savage, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.SkipToPhase(Phase.REGROUP);

		// Vitality 4 with 3 wounds: Gandalf cannot exert. The Ent Avenger, a [gandalf] companion, could, but he is
		// no Gandalf; a wounded minion is there to discard.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsPlayAvailable(task));
		assertInHand(task);
	}
}
