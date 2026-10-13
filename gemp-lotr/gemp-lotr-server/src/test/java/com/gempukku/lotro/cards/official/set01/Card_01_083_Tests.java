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

public class Card_01_083_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("servant", "1_83");
					put("gandalf", "1_364");      // Gandalf, The Grey Wizard: the Gandalf to spot; a fellowship ability used only on purpose
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a companion who is no Gandalf; a maneuver ability used only on purpose
					put("barliman", "1_70");      // Barliman Butterbur, Prancing Pony Proprietor: [gandalf] ally who is not Gandalf; a fellowship ability used only on purpose
					put("avenger", "6_27");       // Ent Avenger: a [gandalf] companion who is not Gandalf; its text needs 3 Ents spotted

					put("party", "1_158");        // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, damage +1 and nothing else
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("worker", "3_62");        // Isengard Worker: [isengard] Orc, strength 5; a regroup ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ServantoftheSecretFireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Servant of the Secret Fire
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Spell</b>.<br><b>Skirmish:</b> Spot Gandalf to make a minion strength -3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("servant");

		assertEquals("Servant of the Secret Fire", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingGandalfMakesAnyMinionStrengthMinus3ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servant = scn.GetFreepsCard("servant");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(servant);

		var party = scn.GetShadowCard("party");
		var troop = scn.GetShadowCard("troop");
		var worker = scn.GetShadowCard("worker");

		scn.StartGame();
		scn.MoveMinionsToTable(party, troop, worker);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, party}, new PhysicalCardImpl[]{aragorn, troop});
		// The Isengard Worker is left over, and stays unassigned.
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(9, scn.GetStrength(party));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(worker));
		assertTrue(scn.FreepsPlayAvailable(servant));

		scn.FreepsPlayCard(servant);
		// Any minion: the one in this skirmish, one assigned to another companion, and one assigned to nobody.
		// No companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(party, troop, worker));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertInZone(Zone.DISCARD, servant);
		// 13 - 3
		assertEquals(10, scn.GetStrength(troop));
		assertEquals(9, scn.GetStrength(party));
		assertEquals(5, scn.GetStrength(worker));

		// Still in force as the skirmish draws to its end.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();
		assertEquals(10, scn.GetStrength(troop));
		scn.FreepsPass();

		// Gandalf's skirmish is over; in the next, the Troop's own, the effect is gone.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutGandalfThoughAnotherGandalfCardIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servant = scn.GetFreepsCard("servant");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(aragorn, avenger);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(servant);

		var party = scn.GetShadowCard("party");

		scn.StartGame();
		scn.MoveMinionsToTable(party);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, party);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(servant));
		assertInZone(Zone.HAND, servant);
	}
}
